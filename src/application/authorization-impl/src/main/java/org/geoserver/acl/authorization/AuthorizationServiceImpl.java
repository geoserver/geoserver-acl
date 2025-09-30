/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static org.geoserver.acl.domain.adminrules.AdminGrantType.ADMIN;
import static org.geoserver.acl.domain.adminrules.AdminGrantType.USER;
import static org.geoserver.acl.domain.rules.CatalogMode.CHALLENGE;
import static org.geoserver.acl.domain.rules.CatalogMode.HIDE;
import static org.geoserver.acl.domain.rules.CatalogMode.MIXED;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;
import static org.geoserver.acl.domain.rules.GrantType.LIMIT;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READONLY;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READWRITE;
import static org.geoserver.acl.domain.rules.SpatialFilterType.CLIP;
import static org.geoserver.acl.domain.rules.SpatialFilterType.INTERSECT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
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

/**
 * <B>Note:</B> <TT>service</TT> and <TT>request</TT> params are usually set by the client, and by
 * OGC specs they are not case sensitive, so we're going to turn all of them uppercase. See also
 * {@link RuleAdminService}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Slf4j(topic = "org.geoserver.acl.authorization")
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AdminRuleAdminService adminRuleService;
    private final RuleAdminService ruleService;

    /**
     * @param filter
     * @return a plain List of the grouped matching Rules.
     */
    @Override
    public List<Rule> getMatchingRules(@NonNull AccessRequest request) {
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
    public AccessInfo getAccessInfo(@NonNull AccessRequest request) {
        request = request.validate();

        Map<String, List<Rule>> groupedRules = getMatchingRulesByRole(request);

        AccessInfo ret = null;

        for (Entry<String, List<Rule>> ruleGroup : groupedRules.entrySet()) {
            List<Rule> rules = ruleGroup.getValue();
            AccessInfo accessInfo = resolveRuleset(rules);
            ret = enlargeAccessInfo(ret, accessInfo);
        }

        if (null == ret) ret = AccessInfo.DENY_ALL;

        List<String> matchingIds =
                flatten(groupedRules).stream().map(Rule::getId).collect(Collectors.toList());
        ret = ret.withMatchingRules(matchingIds);
        log.debug("Request: {}, response: {}", request, ret);
        return ret;
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(@NonNull AdminAccessRequest request) {
        Optional<AdminRule> adminAuth = getAdminAuth(request);
        boolean adminRigths = isAdminAuth(adminAuth);
        String adminRuleId = adminAuth.map(AdminRule::getId).orElse(null);
        return AdminAccessInfo.builder()
                .workspace(request.getWorkspace())
                .admin(adminRigths)
                .matchingAdminRule(adminRuleId)
                .build();
    }

    @Override
    public AccessSummary getUserAccessSummary(AccessSummaryRequest request) {
        String user = request.getUser();
        Set<String> roles = request.getRoles();

        Map<String, List<AdminRule>> wsAdminRules = getAdminRulesByWorkspace(user, roles);
        Map<String, List<Rule>> wsRules = getRulesByWorkspace(user, roles);

        Set<String> workspaces = union(wsAdminRules.keySet(), wsRules.keySet());

        List<WorkspaceAccessSummary> summaries = workspaces.stream()
                .map(ws -> conflateViewables(ws, wsAdminRules, wsRules))
                .collect(Collectors.toList());

        return AccessSummary.of(summaries);
    }

    private Set<String> union(Set<String> s1, Set<String> s2) {
        return Stream.concat(s1.stream(), s2.stream()).collect(Collectors.toSet());
    }

    private WorkspaceAccessSummary conflateViewables(
            String workspace,
            Map<String, List<AdminRule>> adminRulesByWorkspace,
            Map<String, List<Rule>> rulesByWorkspace) {

        var builder = WorkspaceAccessSummary.builder();
        builder.workspace(workspace);
        conflateAdminRules(builder, adminRulesByWorkspace.getOrDefault(workspace, List.of()));
        conflateRules(builder, rulesByWorkspace.getOrDefault(workspace, List.of()));
        return builder.build();
    }

    void conflateAdminRules(WorkspaceAccessSummary.Builder builder, List<AdminRule> rules) {

        AdminRule rule = rules.stream()
                .sorted(Comparator.comparingLong(AdminRule::getPriority))
                .findFirst()
                .orElse(null);
        if (rule != null) {
            AdminGrantType adminAccess = rule.getAccess();
            builder.adminAccess(adminAccess);
        }
    }

    void conflateRules(WorkspaceAccessSummary.Builder builder, List<Rule> rules) {

        // LIMIT rules don't provide access level
        Predicate<Rule> notLimitRule = r -> r.getIdentifier().getAccess() != GrantType.LIMIT;
        // reverse priority sort so the most important ones are added the latest and the
        // builder creates the allowed/forbidden sets correctly
        Comparator<Rule> reversePriority =
                Comparator.comparing(Rule::getPriority).reversed();

        // add deny rules first, and allow rules after, so allow rules prevail (i.e.
        // their layer names get removed from the summary's "forbidden" list, since this
        // is a summary of somehow visible layers, we give preference to allow rules
        // regardless of the priority
        assert GrantType.ALLOW.compareTo(GrantType.DENY) < 0;
        Comparator<Rule> comparator =
                Comparator.comparing(Rule::access).reversed().thenComparing(reversePriority);

        rules.stream().filter(notLimitRule).sorted(comparator).forEach(r -> {
            GrantType access = r.getIdentifier().getAccess();
            String layer = r.getIdentifier().getLayer();
            if (null == layer) layer = WorkspaceAccessSummary.ANY;
            switch (access) {
                case ALLOW:
                    builder.addAllowed(layer);
                    break;
                case DENY:
                    {
                        // only add forbidden layers if they're so for all services,
                        // to comply with the "somehow can see" motto of the summary
                        String service = r.getIdentifier().getService();
                        if (null == service) {
                            builder.addForbidden(layer);
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });
    }

    Map<String, List<AdminRule>> getAdminRulesByWorkspace(String user, Set<String> roles) {

        // Filter is SpecialFilterType.ANY to return all rules
        AdminRuleFilter filter = new AdminRuleFilter(SpecialFilterType.ANY);
        filter.getUser().setHeuristically(user);
        filter.getRole().setHeuristically(roles);

        Function<AdminRule, String> workspaceMapper =
                workspaceMapper(r -> r.getIdentifier().getWorkspace());
        Comparator<AdminRule> workspaceComparator = workspaceComparator(workspaceMapper);
        RuleQuery<AdminRuleFilter> query = RuleQuery.of(filter);
        BinaryOperator<List<AdminRule>> mergeFunction = mergeFunction();
        try (Stream<AdminRule> all = this.adminRuleService.getAll(query)) {
            return all.sorted(workspaceComparator)
                    .distinct()
                    .collect(Collectors.toMap(workspaceMapper, List::of, mergeFunction));
        }
    }

    Map<String, List<Rule>> getRulesByWorkspace(String user, Set<String> roles) {

        // Filter is SpecialFilterType.ANY to return all rules
        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY);
        filter.getUser().setHeuristically(user);
        filter.getRole().setHeuristically(roles);
        List<Rule> rules = getRulesByRoleIncludeDefault(filter);

        Function<Rule, String> workspaceMapper =
                workspaceMapper(r -> r.getIdentifier().getWorkspace());
        Comparator<Rule> workspaceComparator = workspaceComparator(workspaceMapper);

        Function<Rule, List<Rule>> valueMapper = List::of;
        BinaryOperator<List<Rule>> mergeFunction = mergeFunction();
        return rules.stream()
                .sorted(workspaceComparator)
                .distinct()
                .sorted(Comparator.comparing(Rule::getPriority))
                .collect(Collectors.toMap(workspaceMapper, valueMapper, mergeFunction));
    }

    private <R> Comparator<R> workspaceComparator(Function<R, String> workspaceMapper) {
        return comparing(workspaceMapper, naturalOrder());
    }

    private <R> Function<R, String> workspaceMapper(Function<R, String> workspaceExtractor) {
        return workspaceExtractor.andThen(ws -> null == ws ? WorkspaceAccessSummary.ANY : ws);
    }

    private <T> BinaryOperator<List<T>> mergeFunction() {
        return (l1, l2) -> {
            if (l1 instanceof ArrayList) {
                l1.addAll(l2);
                return l1;
            }
            if (l2 instanceof ArrayList) {
                l2.addAll(l1);
                return l2;
            }
            List<T> ret = new ArrayList<>();
            ret.addAll(l1);
            ret.addAll(l2);
            return ret;
        };
    }

    private AccessInfo enlargeAccessInfo(AccessInfo baseAccess, AccessInfo moreAccess) {
        if (baseAccess == null) {
            if (moreAccess == null) return null;
            else if (moreAccess.getGrant() == ALLOW) return moreAccess;
            else return null;
        } else {
            if (moreAccess == null) return baseAccess;
            else if (moreAccess.getGrant() == DENY) return baseAccess;
            else {
                // ok: extending grants
                AccessInfo.Builder ret = AccessInfo.builder().grant(ALLOW);

                String cqlRead = unionCQL(baseAccess.getCqlFilterRead(), moreAccess.getCqlFilterRead());
                ret.cqlFilterRead(cqlRead);
                String cqlWrite = unionCQL(baseAccess.getCqlFilterWrite(), moreAccess.getCqlFilterWrite());
                ret.cqlFilterWrite(cqlWrite);

                CatalogMode catalogMode = getLarger(baseAccess.getCatalogMode(), moreAccess.getCatalogMode());
                ret.catalogMode(catalogMode);

                if (baseAccess.getDefaultStyle() == null || moreAccess.getDefaultStyle() == null) {
                    ret.defaultStyle(null);
                } else {
                    ret.defaultStyle(baseAccess.getDefaultStyle()); // just pick one
                }

                Set<String> allowedStyles = unionAllowedStyles(baseAccess, moreAccess);
                ret.allowedStyles(allowedStyles);

                Set<LayerAttribute> attributes = unionAttributes(baseAccess, moreAccess);
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
    private void setAllowedAreas(AccessInfo baseAccess, AccessInfo moreAccess, AccessInfo.Builder ret) {
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
        return "(%s) OR (%s)".formatted(c1, c2);
    }

    private Geometry unionGeometry(Geometry g1, Geometry g2) {
        if (g1 == null || g2 == null) return null;

        int targetSRID = g1.getSRID();
        Geometry result = g1.union(reprojectGeometry(targetSRID, g2));
        result.setSRID(targetSRID);
        return result;
    }

    private static Set<LayerAttribute> unionAttributes(AccessInfo a0, AccessInfo a1) {
        return unionAttributes(a0.getAttributes(), a1.getAttributes());
    }

    private static Set<LayerAttribute> unionAttributes(Set<LayerAttribute> a0, Set<LayerAttribute> a1) {
        if (null == a0) a0 = Set.of();
        if (null == a1) a1 = Set.of();
        // if at least one of the two set is empty, the result will be an empty set,
        // that means attributes are not restricted
        if (a0.isEmpty() || a1.isEmpty()) return Set.of();

        Set<LayerAttribute> ret = new HashSet<>();
        // add both attributes only in a0, and enlarge common attributes
        for (LayerAttribute attr0 : a0) {
            getAttribute(attr0.getName(), a1)
                    .ifPresentOrElse(attr1 -> ret.add(enlargeAccess(attr0, attr1)), () -> ret.add(attr0));
        }
        // now add attributes that are only in a1
        for (LayerAttribute attr1 : a1) {
            getAttribute(attr1.getName(), a0)
                    .ifPresentOrElse(attr0 -> log.trace("ignoring att {}", attr0.getName()), () -> ret.add(attr1));
        }

        return ret;
    }

    private static LayerAttribute enlargeAccess(LayerAttribute attr0, LayerAttribute attr1) {
        LayerAttribute attr = attr0;
        if (attr0.getAccess() == READWRITE || attr1.getAccess() == READWRITE) attr = attr.withAccess(READWRITE);
        else if (attr0.getAccess() == READONLY || attr1.getAccess() == READONLY) attr = attr.withAccess(READONLY);
        return attr;
    }

    private static Optional<LayerAttribute> getAttribute(String name, Set<LayerAttribute> set) {
        return set.stream().filter(la -> name.equals(la.getName())).findFirst();
    }

    private static Set<String> unionAllowedStyles(AccessInfo a0, AccessInfo a1) {
        return unionAllowedStyles(a0.getAllowedStyles(), a1.getAllowedStyles());
    }

    private static Set<String> unionAllowedStyles(Set<String> a0, Set<String> a1) {
        if (null == a0) a0 = Set.of();
        if (null == a1) a1 = Set.of();

        // if at least one of the two set is empty, the result will be an empty set,
        // that means styles are not restricted
        if (a0.isEmpty() || a1.isEmpty()) return Set.of();

        Set<String> allowedStyles = new HashSet<>();
        allowedStyles.addAll(a0);
        allowedStyles.addAll(a1);
        return allowedStyles;
    }

    private AccessInfo resolveRuleset(List<Rule> ruleList) {

        List<RuleLimits> limits = new ArrayList<>();
        AccessInfo ret = null;
        for (Rule rule : ruleList) {
            final GrantType access = rule.getIdentifier().getAccess();
            switch (access) {
                case ALLOW:
                    return buildAllowAccessInfo(rule, limits);
                case DENY:
                    return AccessInfo.DENY_ALL;
                case LIMIT:
                    if (null != rule.getRuleLimits()) limits.add(rule.getRuleLimits());
                    break;
                default:
                    throw new IllegalStateException("Unknown GrantType " + access);
            }
        }
        return ret;
    }

    private AccessInfo buildAllowAccessInfo(Rule rule, List<RuleLimits> limits) {
        AccessInfo.Builder accessInfo = AccessInfo.builder().grant(ALLOW);

        // first intersects geometry of same type
        Geometry area = intersect(limits);
        boolean atLeastOneClip =
                limits.stream().map(RuleLimits::getSpatialFilterType).anyMatch(CLIP::equals);
        CatalogMode cmode = resolveCatalogMode(limits);
        final LayerDetails details = getLayerDetails(rule);
        if (null != details) {
            // intersect the allowed area of the rule to the proper type
            SpatialFilterType spatialFilterType = getSpatialFilterType(rule, details);
            atLeastOneClip = spatialFilterType.equals(CLIP);

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
            // if we have a clip area we apply clip type since is more restrictive,
            // otherwise we
            // keep the intersect
            org.geolatte.geom.Geometry<?> finalArea = org.geolatte.geom.jts.JTS.from(area);
            if (atLeastOneClip) {
                accessInfo.clipArea(finalArea);
            } else {
                accessInfo.area(finalArea);
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
        if (LIMIT.equals(rule.getIdentifier().getAccess()) && null != rule.getRuleLimits()) {
            spatialFilterType = rule.getRuleLimits().getSpatialFilterType();
        } else if (null != details) {
            spatialFilterType = details.getSpatialFilterType();
        }
        if (null == spatialFilterType) spatialFilterType = INTERSECT;

        return spatialFilterType;
    }

    private Geometry intersect(List<RuleLimits> limits) {
        List<Geometry> geoms = limits.stream()
                .map(RuleLimits::getAllowedArea)
                .filter(Objects::nonNull)
                .map(this::toJTS)
                .collect(Collectors.toList());
        if (geoms.isEmpty()) return null;
        if (1 == geoms.size()) return geoms.get(0);

        org.locationtech.jts.geom.Geometry intersection = geoms.get(0);
        for (int i = 1; i < geoms.size(); i++) {
            intersection = intersect(intersection, geoms.get(i));
        }
        return intersection;
    }

    private Geometry intersect(Geometry g1, Geometry g2) {
        if (g1 == null) return g2;
        if (g2 == null) return g1;

        int targetSRID = g1.getSRID();
        Geometry result = g1.intersection(reprojectGeometry(targetSRID, g2));
        result.setSRID(targetSRID);
        return result;
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

        if (HIDE == m1 || HIDE == m2) return HIDE;

        if (MIXED == m1 || MIXED == m2) return MIXED;

        return CHALLENGE;
    }

    protected static CatalogMode getLarger(CatalogMode m1, CatalogMode m2) {

        if (m1 == null) return m2;
        if (m2 == null) return m1;

        if (CHALLENGE == m1 || CHALLENGE == m2) return CHALLENGE;

        if (MIXED == m1 || MIXED == m2) return MIXED;

        return HIDE;
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
    @SuppressWarnings("java:S125")
    protected Map<String, List<Rule>> getMatchingRulesByRole(AccessRequest request) throws IllegalArgumentException {

        RuleFilter filter = new RuleFilter(SpecialFilterType.DEFAULT);
        filter.getUser().setHeuristically(request.getUser());

        Set<String> userRoles = request.getRoles();
        filter.getRole().setHeuristically(userRoles);

        filter.getSourceAddress().setHeuristically(request.getSourceAddress());
        filter.getService().setHeuristically(request.getService());
        filter.getRequest().setHeuristically(request.getRequest());
        filter.getSubfield().setHeuristically(request.getSubfield());
        filter.getWorkspace().setHeuristically(request.getWorkspace());
        filter.getLayer().setHeuristically(request.getLayer());

        Map<String, List<Rule>> ret = new HashMap<>();

        final Set<String> finalRoleFilter = filter.getRole().getValues();
        if (finalRoleFilter.isEmpty()) {
            if (filter.getRole().getType() != FilterType.ANY) {
                filter = filter.clone();
                filter.getRole().setType(SpecialFilterType.DEFAULT);
            }
            List<Rule> found = ruleService.getAll(RuleQuery.of(filter)).collect(Collectors.toList());
            ret.put(null, found);
        } else {
            // used to be: for(role: finalRoleFilter) getRulesByRole(filter, role);,
            // conflated to a single query with all roles here
            List<Rule> rules = getRulesByRoleIncludeDefault(filter);
            finalRoleFilter.forEach(r -> ret.put(r, new ArrayList<>()));
            for (Rule rule : rules) {
                String rolename = rule.getIdentifier().getRolename();
                boolean isdefault = null == rolename;
                if (isdefault) {
                    finalRoleFilter.forEach(role -> ret.get(role).add(rule));
                } else {
                    ret.get(rolename).add(rule);
                }
            }
        }
        return ret;
    }

    private List<Rule> getRulesByRoleIncludeDefault(RuleFilter filter) {
        filter = filter.clone();
        filter.getRole().setIncludeDefault(true);
        return ruleService.getAll(RuleQuery.of(filter)).collect(Collectors.toList());
    }

    private boolean isAdminAuth(Optional<AdminRule> rule) {
        return rule.map(AdminRule::getAccess).orElse(USER) == ADMIN;
    }

    private Optional<AdminRule> getAdminAuth(AdminAccessRequest request) {
        request = request.validate();
        AdminRuleFilter adminRuleFilter = new AdminRuleFilter();
        adminRuleFilter.getSourceAddress().setHeuristically(request.getSourceAddress());
        adminRuleFilter.getUser().setHeuristically(request.getUser());
        adminRuleFilter.getRole().setHeuristically(request.getRoles());
        adminRuleFilter.getWorkspace().setHeuristically(request.getWorkspace());

        Set<String> finalRoleFilter = adminRuleFilter.getRole().getValues();

        if (finalRoleFilter.isEmpty()) {
            return adminRuleService.getFirstMatch(adminRuleFilter);
        }

        adminRuleFilter.getRole().setIncludeDefault(true);
        return adminRuleService.getFirstMatch(adminRuleFilter);
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
            throw new IllegalStateException(
                    "Unable to find transformation for SRIDs: " + geom.getSRID() + " to " + targetSRID);
        } catch (TransformException e) {
            throw new IllegalStateException(
                    "Unable to reproject geometry from " + geom.getSRID() + " to " + targetSRID);
        }
    }
}
