/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleFilter;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.filter.predicate.FilterType;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.domain.rules.LayerAttribute.AccessType;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.SpatialFilterType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <B>Note:</B> <TT>service</TT> and <TT>request</TT> params are usually set by the client, and by
 * OGC specs they are not case sensitive, so we're going to turn all of them uppercase. See also
 * {@link RuleAdminService}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Slf4j
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AdminRuleAdminService adminRuleService;
    private final RuleAdminService ruleService;

    /**
     * @param filter
     * @return a plain List of the grouped matching Rules.
     */
    @Override
    public List<Rule> getMatchingRules(AccessRequest request) {
        request = request.validate();
        Map<String, List<Rule>> found = getMatchingRulesByRole(request);
        return flatten(found);
    }

    private List<Rule> flatten(Map<String, List<Rule>> found) {
        return found.values().stream()
                .flatMap(List::stream)
                .sorted((r1, r2) -> Long.compare(r1.getPriority(), r2.getPriority()))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public AccessInfo getAccessInfo(AccessRequest request) {
        request = request.validate();
        log.debug("Requesting access for {}", request);
        Map<String, List<Rule>> groupedRules = getMatchingRulesByRole(request);

        AccessInfo currAccessInfo = null;

        //        List<Rule> flattened = flatten(groupedRules);
        //        currAccessInfo = resolveRuleset(flattened);

        for (Entry<String, List<Rule>> ruleGroup : groupedRules.entrySet()) {
            String role = ruleGroup.getKey();
            List<Rule> rules = ruleGroup.getValue();

            AccessInfo accessInfo = resolveRuleset(rules);
            if (log.isDebugEnabled()) {
                log.debug("Filter {} on role {} has access {}", request, role, accessInfo);
            }

            currAccessInfo = enlargeAccessInfo(currAccessInfo, accessInfo);
        }

        AccessInfo ret;

        if (currAccessInfo == null) {
            log.debug("No access for filter " + request);
            // Denying by default
            ret = AccessInfo.DENY_ALL;
        } else {
            ret = currAccessInfo;
        }

        log.debug("Returning {} for {}", ret, request);
        List<String> matchingIds =
                flatten(groupedRules).stream().map(Rule::getId).collect(Collectors.toList());
        return ret.withMatchingRules(matchingIds);
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(AdminAccessRequest request) {
        Optional<AdminRule> adminAuth = getAdminAuth(request);
        boolean adminRigths = isAdminAuth(adminAuth);
        String adminRuleId = adminAuth.map(AdminRule::getId).orElse(null);
        return AdminAccessInfo.builder()
                .workspace(request.getWorkspace())
                .admin(adminRigths)
                .matchingAdminRule(adminRuleId)
                .build();
    }

    private AccessInfo enlargeAccessInfo(AccessInfo baseAccess, AccessInfo moreAccess) {
        if (baseAccess == null) {
            if (moreAccess == null) return null;
            else if (moreAccess.getGrant() == GrantType.ALLOW) return moreAccess;
            else return null;
        } else {
            if (moreAccess == null) return baseAccess;
            else if (moreAccess.getGrant() == GrantType.DENY) return baseAccess;
            else {
                // ok: extending grants
                AccessInfo.Builder ret = AccessInfo.builder().grant(GrantType.ALLOW);

                String cqlRead =
                        unionCQL(baseAccess.getCqlFilterRead(), moreAccess.getCqlFilterRead());
                ret.cqlFilterRead(cqlRead);
                String cqlWrite =
                        unionCQL(baseAccess.getCqlFilterWrite(), moreAccess.getCqlFilterWrite());
                ret.cqlFilterWrite(cqlWrite);

                CatalogMode catalogMode =
                        getLarger(baseAccess.getCatalogMode(), moreAccess.getCatalogMode());
                ret.catalogMode(catalogMode);

                if (baseAccess.getDefaultStyle() == null || moreAccess.getDefaultStyle() == null) {
                    ret.defaultStyle(null);
                } else {
                    ret.defaultStyle(baseAccess.getDefaultStyle()); // just pick one
                }

                Set<String> allowedStyles =
                        unionAllowedStyles(
                                baseAccess.getAllowedStyles(), moreAccess.getAllowedStyles());
                ret.allowedStyles(allowedStyles);

                Set<LayerAttribute> attributes =
                        unionAttributes(baseAccess.getAttributes(), moreAccess.getAttributes());
                ret.attributes(attributes);

                setAllowedAreas(baseAccess, moreAccess, ret);
                return ret.build();
            }
        }
    }

    // takes care of properly setting the allowedAreas to returned accessInfo
    // if the union results is null check if the other allowedArea exists
    // if yes set both, to make sure user doesn't acquire visibility
    // on not allowed geometries
    private void setAllowedAreas(
            AccessInfo baseAccess, AccessInfo moreAccess, AccessInfo.Builder ret) {
        final Geometry baseIntersects = toJTS(baseAccess.getArea());
        final Geometry baseClip = toJTS(baseAccess.getClipArea());
        final Geometry moreIntersects = toJTS(moreAccess.getArea());
        final Geometry moreClip = toJTS(moreAccess.getClipArea());
        final Geometry unionIntersects = unionGeometry(baseIntersects, moreIntersects);
        final Geometry unionClip = unionGeometry(baseClip, moreClip);
        if (unionIntersects == null) {
            if (baseIntersects != null && moreClip != null) {
                ret.area(baseAccess.getArea());
            } else if (moreIntersects != null && baseClip != null) {
                ret.area(moreAccess.getArea());
            }
        } else {
            ret.area(org.geolatte.geom.jts.JTS.from(unionIntersects));
        }
        if (unionClip == null) {
            if (baseClip != null && moreIntersects != null) {
                ret.clipArea(baseAccess.getClipArea());
            } else if (moreClip != null && baseIntersects != null) {
                ret.clipArea(moreAccess.getClipArea());
            }
        } else {
            ret.clipArea(org.geolatte.geom.jts.JTS.from(unionClip));
        }
    }

    private Geometry toJTS(org.geolatte.geom.Geometry<?> geom) {
        return geom == null ? null : org.geolatte.geom.jts.JTS.to(geom);
    }

    private String unionCQL(String c1, String c2) {
        if (c1 == null || c2 == null) return null;

        return "(" + c1 + ") OR (" + c2 + ")";
    }

    private Geometry unionGeometry(Geometry g1, Geometry g2) {
        if (g1 == null || g2 == null) return null;

        return union(g1, g2);
    }

    private static Set<LayerAttribute> unionAttributes(
            Set<LayerAttribute> a0, Set<LayerAttribute> a1) {
        // TODO: check how geoserver deals with empty set

        if (a0 == null || a0.isEmpty()) return Set.of();
        // return a1;
        if (a1 == null || a1.isEmpty()) return Set.of();
        // return a0;

        Set<LayerAttribute> ret = new HashSet<LayerAttribute>();
        // add both attributes only in a0, and enlarge common attributes
        for (LayerAttribute attr0 : a0) {
            LayerAttribute attr1 = getAttribute(attr0.getName(), a1);
            if (attr1 == null) {
                ret.add(attr0);
            } else {
                LayerAttribute attr = attr0;
                if (attr0.getAccess() == AccessType.READWRITE
                        || attr1.getAccess() == AccessType.READWRITE)
                    attr = attr.withAccess(AccessType.READWRITE);
                else if (attr0.getAccess() == AccessType.READONLY
                        || attr1.getAccess() == AccessType.READONLY)
                    attr = attr.withAccess(AccessType.READONLY);
                ret.add(attr);
            }
        }
        // now add attributes that are only in a1
        for (LayerAttribute attr1 : a1) {
            LayerAttribute attr0 = getAttribute(attr1.getName(), a0);
            if (attr0 == null) {
                ret.add(attr1);
            }
        }

        return ret;
    }

    private static LayerAttribute getAttribute(String name, Set<LayerAttribute> set) {
        for (LayerAttribute layerAttribute : set) {
            if (layerAttribute.getName().equals(name)) return layerAttribute;
        }
        return null;
    }

    private static Set<String> unionAllowedStyles(Set<String> a0, Set<String> a1) {

        // if at least one of the two set is empty, the result will be an empty set,
        // that means styles are not restricted
        if (a0 == null || a0.isEmpty()) return Set.of();

        if (a1 == null || a1.isEmpty()) return Set.of();

        Set<String> allowedStyles = new HashSet<String>();
        allowedStyles.addAll(a0);
        allowedStyles.addAll(a1);
        return allowedStyles;
    }

    private AccessInfo resolveRuleset(List<Rule> ruleList) {

        List<RuleLimits> limits = new ArrayList<>();
        AccessInfo ret = null;
        for (Rule rule : ruleList) {
            if (ret != null) break;

            switch (rule.getIdentifier().getAccess()) {
                case LIMIT:
                    RuleLimits rl = rule.getRuleLimits();
                    if (rl != null) {
                        log.trace("Collecting limits: {}", rl);
                        limits.add(rl);
                    } else
                        log.trace(
                                "Rule has no associated limits (id: {}, priority: {})",
                                rule.getId(),
                                rule.getPriority());
                    break;

                case DENY:
                    ret = AccessInfo.DENY_ALL;
                    break;

                case ALLOW:
                    ret = buildAllowAccessInfo(rule, limits);
                    break;

                default:
                    throw new IllegalStateException(
                            "Unknown GrantType " + rule.getIdentifier().getAccess());
            }
        }
        return ret;
    }

    private AccessInfo buildAllowAccessInfo(Rule rule, List<RuleLimits> limits) {
        AccessInfo.Builder accessInfo = AccessInfo.builder().grant(GrantType.ALLOW);

        // first intersects geometry of same type
        Geometry area = intersect(limits);
        boolean atLeastOneClip =
                limits.stream()
                        .anyMatch(l -> l.getSpatialFilterType().equals(SpatialFilterType.CLIP));
        CatalogMode cmode = resolveCatalogMode(limits);
        final LayerDetails details = getLayerDetails(rule);
        if (null != details) {
            // intersect the allowed area of the rule to the proper type
            SpatialFilterType spatialFilterType = getSpatialFilterType(rule, details);
            atLeastOneClip = spatialFilterType.equals(SpatialFilterType.CLIP);

            area = intersect(area, toJTS(details.getArea()));

            cmode = getStricter(cmode, details.getCatalogMode());

            accessInfo.attributes(details.getAttributes());
            accessInfo.cqlFilterRead(details.getCqlFilterRead());
            accessInfo.cqlFilterWrite(details.getCqlFilterWrite());
            accessInfo.defaultStyle(details.getDefaultStyle());
            accessInfo.allowedStyles(details.getAllowedStyles());
        }

        accessInfo.catalogMode(cmode);

        if (area != null) {
            // if we have a clip area we apply clip type
            // since is more restrictive, otherwise we keep
            // the intersect
            if (atLeastOneClip) {
                accessInfo.clipArea(org.geolatte.geom.jts.JTS.from(area));
            } else {
                accessInfo.area(org.geolatte.geom.jts.JTS.from(area));
            }
        }
        return accessInfo.build();
    }

    private LayerDetails getLayerDetails(Rule rule) {
        final boolean hasLayer = null != rule.getIdentifier().getLayer();
        if (hasLayer) {
            return ruleService.getLayerDetails(rule.getId()).orElse(null);
        }
        return null;
    }

    private SpatialFilterType getSpatialFilterType(Rule rule, LayerDetails details) {
        SpatialFilterType spatialFilterType = null;
        if (GrantType.LIMIT.equals(rule.getIdentifier().getAccess())
                && null != rule.getRuleLimits()) {
            spatialFilterType = rule.getRuleLimits().getSpatialFilterType();
        } else if (null != details) {
            spatialFilterType = details.getSpatialFilterType();
        }
        if (null == spatialFilterType) spatialFilterType = SpatialFilterType.INTERSECT;

        return spatialFilterType;
    }

    private Geometry intersect(List<RuleLimits> limits) {
        org.locationtech.jts.geom.Geometry g = null;
        for (RuleLimits limit : limits) {
            org.locationtech.jts.geom.MultiPolygon area =
                    (MultiPolygon) toJTS(limit.getAllowedArea());
            if (area != null) {
                if (g == null) {
                    g = area;
                } else {
                    int targetSRID = g.getSRID();
                    g = g.intersection(reprojectGeometry(targetSRID, area));
                    g.setSRID(targetSRID);
                }
            }
        }
        return g;
    }

    private Geometry intersect(Geometry g1, Geometry g2) {
        if (g1 != null) {
            if (g2 == null) {
                return g1;
            } else {
                int targetSRID = g1.getSRID();
                Geometry result = g1.intersection(reprojectGeometry(targetSRID, g2));
                result.setSRID(targetSRID);
                return result;
            }
        } else {
            return g2;
        }
    }

    private Geometry union(Geometry g1, Geometry g2) {
        if (g1 != null) {
            if (g2 == null) {
                return g1;
            } else {
                int targetSRID = g1.getSRID();
                Geometry result = g1.union(reprojectGeometry(targetSRID, g2));
                result.setSRID(targetSRID);
                return result;
            }
        } else {
            return g2;
        }
    }

    /** Returns the stricter catalog mode. */
    private CatalogMode resolveCatalogMode(List<RuleLimits> limits) {
        CatalogMode ret = null;
        for (RuleLimits limit : limits) {
            ret = getStricter(ret, limit.getCatalogMode());
        }
        return ret;
    }

    protected static CatalogMode getStricter(CatalogMode m1, CatalogMode m2) {

        if (m1 == null) return m2;
        if (m2 == null) return m1;

        if (CatalogMode.HIDE == m1 || CatalogMode.HIDE == m2) return CatalogMode.HIDE;

        if (CatalogMode.MIXED == m1 || CatalogMode.MIXED == m2) return CatalogMode.MIXED;

        return CatalogMode.CHALLENGE;
    }

    protected static CatalogMode getLarger(CatalogMode m1, CatalogMode m2) {

        if (m1 == null) return m2;
        if (m2 == null) return m1;

        if (CatalogMode.CHALLENGE == m1 || CatalogMode.CHALLENGE == m2)
            return CatalogMode.CHALLENGE;

        if (CatalogMode.MIXED == m1 || CatalogMode.MIXED == m2) return CatalogMode.MIXED;

        return CatalogMode.HIDE;
    }

    // ==========================================================================

    /**
     * Returns Rules matching a filter
     *
     * <p>Compatible filters: username assigned and rolename:ANY -> should consider all the roles
     * the user belongs to username:ANY and rolename assigned -> should consider all the users
     * belonging to the given role
     *
     * @param filter a RuleFilter for rule selection. <B>side effect</B> May be changed by the
     *     method
     * @return a Map having role names as keys, and the list of matching Rules as values. The NULL
     *     key holds the rules for the DEFAULT group.
     */
    protected Map<String, List<Rule>> getMatchingRulesByRole(AccessRequest request)
            throws IllegalArgumentException {

        //        RuleFilter filter = request.getFilter();
        RuleFilter filter = new RuleFilter(SpecialFilterType.DEFAULT);
        filter.getUser().setHeuristically(request.getUser());

        Set<String> userRoles = request.getRoles();
        filter.getRole().setHeuristically(userRoles);

        filter.getSourceAddress().setHeuristically(request.getSourceAddress());
        filter.getInstance().setHeuristically(request.getInstance());
        filter.getService().setHeuristically(request.getService());
        filter.getRequest().setHeuristically(request.getRequest());
        filter.getSubfield().setHeuristically(request.getSubfield());
        filter.getWorkspace().setHeuristically(request.getWorkspace());
        filter.getLayer().setHeuristically(request.getLayer());

        Map<String, List<Rule>> ret = new HashMap<>();

        final Set<String> finalRoleFilter =
                filter.getRole().getValues(); // validateUserRoles(request);
        if (finalRoleFilter.isEmpty()) {
            if (filter.getRole().getType() != FilterType.ANY) {
                filter = filter.clone();
                filter.getRole().setType(SpecialFilterType.DEFAULT);
            }
            List<Rule> found =
                    ruleService.getAll(RuleQuery.of(filter)).collect(Collectors.toList());
            ret.put(null, found);
        } else {
            for (String role : finalRoleFilter) {
                List<Rule> found = getRulesByRole(filter, role);
                ret.put(role, found);
            }
        }
        return ret;
    }

    private List<Rule> getRulesByRole(RuleFilter filter, String role) {
        filter = filter.clone();
        filter.setRole(role);
        filter.getRole().setIncludeDefault(true);
        return ruleService.getAll(RuleQuery.of(filter)).collect(Collectors.toList());
    }

    /**
     * Check requested user and group filter. <br>
     * The input filter <b>may be altered</b> for fixing some request inconsistencies.
     *
     * @param filter
     * @return a Set of group names, or null if provided user/group are invalid.
     * @throws IllegalArgumentException
     */
    //    protected Set<String> validateUserRoles(String username, Set<String> userRoles) throws
    // IllegalArgumentException {
    //
    //        // username can be null if the user filter asks for ANY or DEFAULT
    //        //String username = validateUsername(filter.getUser());
    //
    //        Set<String> finalRoleFilter = new HashSet<>();
    //        // If both user and group are defined in filter
    //        // if user doesn't belong to group, no rule is returned
    //        // otherwise assigned or default rules are searched for
    //
    //        switch (filter.getRole().getType()) {
    //            case NAMEVALUE:
    //                // rolename can be null if the group filter asks for ANY or DEFAULT
    //                final Set<String> requestedRoles = userRoles;//validateRolenames(userRoles);
    //                // rolenames
    //
    //                if (username != null) {
    //                    for (String role : requestedRoles) {
    //                        if (userRoles.contains(role)) {
    //                            finalRoleFilter.add(role);
    //                        } else {
    //                            log.debug(
    //                                    "User does not belong to role [User:{}] [Role:{}]
    // [ResolvedRoles:{}]",
    //                                    filter.getUser(),
    //                                    role,
    //                                    userRoles);
    //                        }
    //                    }
    //                } else {
    //                    finalRoleFilter.addAll(requestedRoles);
    //                }
    //                break;
    //
    //            case ANY:
    //                if (username != null) {
    //                    Set<String> resolvedRoles = request.userRoles();
    //                    if (!resolvedRoles.isEmpty()) {
    //                        finalRoleFilter = resolvedRoles;
    //                    } else {
    //                        filter.setRole(SpecialFilterType.DEFAULT);
    //                    }
    //                } else {
    //                    // no changes, use requested filtering
    //                }
    //                break;
    //
    //            default:
    //                // no changes
    //                break;
    //        }
    //
    //        return finalRoleFilter;
    //    }

    // ==========================================================================

    private boolean isAdminAuth(Optional<AdminRule> rule) {
        return rule.isEmpty() ? false : rule.orElseThrow().getAccess() == AdminGrantType.ADMIN;
    }

    private Optional<AdminRule> getAdminAuth(AdminAccessRequest request) {
        request = request.validate();
        // AdminRuleFilter adminRuleFilter = AdminRuleFilter.of(request.getFilter());
        AdminRuleFilter adminRuleFilter = new AdminRuleFilter();
        adminRuleFilter.getInstance().setHeuristically(request.getInstance());
        adminRuleFilter.getSourceAddress().setHeuristically(request.getSourceAddress());
        adminRuleFilter.getUser().setHeuristically(request.getUser());
        adminRuleFilter.getRole().setHeuristically(request.getRoles());
        adminRuleFilter.getWorkspace().setHeuristically(request.getWorkspace());

        Set<String> finalRoleFilter =
                adminRuleFilter.getRole().getValues(); // validateUserRoles(request);

        if (finalRoleFilter.isEmpty()) {
            return adminRuleService.getFirstMatch(adminRuleFilter);
        }

        //        adminRuleFilter.setRole(RuleFilter.asTextValue(finalRoleFilter));
        adminRuleFilter.getRole().setIncludeDefault(true);
        Optional<AdminRule> found = adminRuleService.getFirstMatch(adminRuleFilter);
        return found;
    }

    private Geometry reprojectGeometry(int targetSRID, Geometry geom) {
        if (targetSRID == geom.getSRID()) return geom;
        try {
            CoordinateReferenceSystem crs = CRS.decode("EPSG:" + geom.getSRID());
            CoordinateReferenceSystem target = CRS.decode("EPSG:" + targetSRID);
            MathTransform transformation = CRS.findMathTransform(crs, target);
            Geometry result = JTS.transform(geom, transformation);
            result.setSRID(targetSRID);
            return result;
        } catch (FactoryException e) {
            throw new RuntimeException(
                    "Unable to find transformation for SRIDs: "
                            + geom.getSRID()
                            + " to "
                            + targetSRID);
        } catch (TransformException e) {
            throw new RuntimeException(
                    "Unable to reproject geometry from " + geom.getSRID() + " to " + targetSRID);
        }
    }
}
