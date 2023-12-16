/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.WARNING;

import com.google.common.base.Stopwatch;

import org.apache.commons.collections4.CollectionUtils;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.domain.rules.LayerAttribute.AccessType;
import org.geoserver.acl.plugin.accessmanager.ContainerLimitResolver.ProcessingResult;
import org.geoserver.acl.plugin.accessmanager.wps.WPSAccessInfo;
import org.geoserver.acl.plugin.accessmanager.wps.WPSHelper;
import org.geoserver.acl.plugin.support.GeomHelper;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerGroupInfo.Mode;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.platform.ExtensionPriority;
import org.geoserver.security.AccessLimits;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.LayerGroupAccessLimits;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.ResourceAccessManagerWrapper;
import org.geoserver.security.StyleAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WMSAccessLimits;
import org.geoserver.security.WMTSAccessLimits;
import org.geoserver.security.WorkspaceAccessLimits;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.LayerGroupContainmentCache;
import org.geoserver.security.impl.LayerGroupContainmentCache.LayerGroupSummary;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ResourceAccessManager} to make GeoServer use the ACL {@link AuthorizationService} to
 * assess data access rules
 *
 * @author Andrea Aime - GeoSolutions - Originally as part of GeoFence's GeoServer extension
 * @author Emanuele Tajariol- GeoSolutions - Originally as part of GeoFence's GeoServer extension
 */
public class ACLResourceAccessManager implements ResourceAccessManager, ExtensionPriority {

    private static final Logger LOGGER = Logging.getLogger(ACLResourceAccessManager.class);

    static final FilterFactory FF = CommonFactoryFinder.getFilterFactory();

    enum PropertyAccessMode {
        READ,
        WRITE
    }

    static final CatalogMode DEFAULT_CATALOG_MODE = CatalogMode.HIDE;

    private AuthorizationService aclService;

    private final AccessManagerConfigProvider configProvider;

    private LayerGroupContainmentCache groupsCache;

    private WPSHelper wpsHelper;

    public ACLResourceAccessManager(
            AuthorizationService aclService,
            LayerGroupContainmentCache groupsCache,
            AccessManagerConfigProvider configurationManager,
            WPSHelper wpsHelper) {

        this.aclService = aclService;
        this.configProvider = configurationManager;
        this.groupsCache = groupsCache;
        this.wpsHelper = wpsHelper;
    }

    public AccessManagerConfig getConfig() {
        return this.configProvider.get();
    }

    static boolean isAuthenticated(Authentication user) {
        return (user != null) && !(user instanceof AnonymousAuthenticationToken);
    }

    static boolean isAdmin(Authentication user) {
        if (isAuthenticated(user)) {
            return user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(GeoServerRole.ADMIN_ROLE.getAuthority()::equals);
        }
        return false;
    }

    @Override
    public WorkspaceAccessLimits getAccessLimits(Authentication user, WorkspaceInfo workspace) {
        CatalogMode catalogMode = DEFAULT_CATALOG_MODE;
        boolean canRead;
        boolean canWrite;
        boolean canAdmin;
        if (isAuthenticated(user)) {
            // shortcut, if the user is the admin, he can do everything
            if (isAdmin(user)) {
                canRead = canWrite = canAdmin = true;
            } else {
                canRead = true;
                canWrite = configProvider.get().isGrantWriteToWorkspacesToAuthenticatedUsers();
                String workspaceName = workspace.getName();
                canAdmin = isWorkspaceAdmin(user, workspaceName);
            }
        } else {
            // further logic disabled because of
            // https://github.com/geosolutions-it/geofence/issues/6 (gone)
            canRead = true;
            canWrite = false;
            canAdmin = false;
        }
        return new WorkspaceAccessLimits(catalogMode, canRead, canWrite, canAdmin);
    }

    /** We expect the user not to be null and not to be admin */
    private boolean isWorkspaceAdmin(Authentication user, String workspaceName) {
        log(
                FINE,
                "Getting admin auth for user {0} on workspace {1}",
                user.getName(),
                workspaceName);

        AdminAccessRequest request =
                new AdminAccessRequestBuilder(configProvider.get())
                        .user(user)
                        .workspace(workspaceName)
                        .build();

        AdminAccessInfo grant = aclService.getAdminAuthorization(request);

        log(
                FINE,
                "Admin auth for user {0} on workspace {1}: {2}",
                user.getName(),
                workspaceName,
                grant.isAdmin());

        return grant.isAdmin();
    }

    @Override
    public StyleAccessLimits getAccessLimits(Authentication user, StyleInfo style) {
        LOGGER.fine("Not limiting styles");
        return null;
    }

    @Override
    public LayerGroupAccessLimits getAccessLimits(Authentication user, LayerGroupInfo layerInfo) {
        return getAccessLimits(user, layerInfo, Collections.emptyList());
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, LayerInfo layer) {
        log(Level.FINE, "Getting access limits for Layer {0}", layer.getName());
        return getAccessLimits(user, layer, Collections.emptyList());
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, ResourceInfo resource) {
        log(Level.FINE, "Getting access limits for Resource {0}", resource.getName());
        // extract the user name
        String workspace = resource.getStore().getWorkspace().getName();
        String layer = resource.getName();
        return (DataAccessLimits)
                getAccessLimits(user, resource, layer, workspace, Collections.emptyList());
    }

    @Override
    public DataAccessLimits getAccessLimits(
            Authentication user, LayerInfo layer, List<LayerGroupInfo> containers) {
        String workspace = layer.getResource().getStore().getWorkspace().getName();
        String layerName = layer.getName();
        return (DataAccessLimits) getAccessLimits(user, layer, layerName, workspace, containers);
    }

    @Override
    public LayerGroupAccessLimits getAccessLimits(
            Authentication user, LayerGroupInfo layerGroup, List<LayerGroupInfo> containers) {
        WorkspaceInfo ws = layerGroup.getWorkspace();
        String workspace = ws != null ? ws.getName() : null;
        String layer = layerGroup.getName();
        return (LayerGroupAccessLimits)
                getAccessLimits(user, layerGroup, layer, workspace, containers);
    }

    private AccessLimits getAccessLimits(
            Authentication user,
            CatalogInfo info,
            String layer,
            String workspace,
            List<LayerGroupInfo> containers) {
        // shortcut, if the user is the admin, he can do everything
        if (isAdmin(user)) {
            log(FINER, "Admin level access, returning full rights for layer {0}", layer);
            return buildAdminAccessLimits(info);
        }

        AccessRequest accessRequest = buildAccessRequest(workspace, layer, user);
        Stopwatch sw = Stopwatch.createStarted();
        AccessInfo accessInfo = aclService.getAccessInfo(accessRequest);
        sw.stop();
        log(FINE, "ACL auth run in {0}: {0}. response ({1}): {2}", sw, accessRequest, accessInfo);

        if (accessInfo == null) {
            accessInfo = AccessInfo.DENY_ALL;
            log(WARNING, "ACL returning null AccessInfo for {0}", accessRequest);
        }

        final Request req = Dispatcher.REQUEST.get();
        final String service = req != null ? req.getService() : null;
        final boolean isWms = "WMS".equalsIgnoreCase(service);
        final boolean layerGroupsRequested = CollectionUtils.isNotEmpty(containers);

        ProcessingResult processingResult = null;
        if (isWms && !layerGroupsRequested) {
            // is direct access we need to retrieve eventually present groups.
            Collection<LayerGroupSummary> summaries = getGroupSummary(info);
            if (!summaries.isEmpty()) {
                boolean allOpaque = allOpaque(summaries);
                boolean noneSingle = noneSingle(summaries);
                // all opaque we deny and don't perform any resolution of group limits.
                if (allOpaque) {
                    accessInfo = accessInfo.withGrant(GrantType.DENY);
                } else if (noneSingle) {
                    // if a single group is present we don't apply any limit from containers.
                    processingResult =
                            getContainerResolverResult(
                                    info, layer, workspace, user, null, summaries);
                }
            }
        } else if (layerGroupsRequested) {
            // layer is requested in context of a layer group.
            // we need to process the containers limits.
            processingResult =
                    getContainerResolverResult(info, layer, workspace, user, containers, List.of());
        }

        if ("WPS".equalsIgnoreCase(service)) {
            if (layerGroupsRequested) {
                log(
                        WARNING,
                        "Don't know how to deal with WPS requests for group data. Won't dive into single process limits.");
            } else {
                WPSAccessInfo resolvedAccessInfo =
                        wpsHelper.resolveWPSAccess(req, accessRequest, accessInfo);
                if (resolvedAccessInfo != null) {
                    accessInfo = resolvedAccessInfo.getAccessInfo();
                    processingResult =
                            new ProcessingResult(
                                    resolvedAccessInfo.getArea(),
                                    resolvedAccessInfo.getClip(),
                                    accessInfo.getCatalogMode());

                    String userNameFromAuth = getUserNameFromAuth(user);
                    log(
                            FINE,
                            "Got WPS access {0} for layer {1} and user {2}",
                            accessInfo,
                            layer,
                            userNameFromAuth);
                }
            }
        }

        AccessLimits limits;
        if (info instanceof LayerGroupInfo) {
            limits = buildLayerGroupAccessLimits(accessInfo);
        } else if (info instanceof ResourceInfo) {
            limits = buildResourceAccessLimits((ResourceInfo) info, accessInfo, processingResult);
        } else {
            limits =
                    buildResourceAccessLimits(
                            ((LayerInfo) info).getResource(), accessInfo, processingResult);
        }

        log(
                FINE,
                "Returning {0} for layer {1} and user {2}",
                limits,
                layer,
                getUserNameFromAuth(user));

        return limits;
    }

    private static void log(Level level, String msg, Object... params) {
        if (LOGGER.isLoggable(level)) {
            LOGGER.log(level, msg, params);
        }
    }

    private boolean allOpaque(Collection<LayerGroupSummary> summaries) {
        LayerGroupInfo.Mode opaque = LayerGroupInfo.Mode.OPAQUE_CONTAINER;
        return summaries.stream().map(LayerGroupSummary::getMode).allMatch(opaque::equals);
    }

    private boolean noneSingle(Collection<LayerGroupSummary> summaries) {
        Mode single = LayerGroupInfo.Mode.SINGLE;
        return summaries.stream().map(LayerGroupSummary::getMode).noneMatch(single::equals);
    }

    // build the accessLimits for an admin user
    private AccessLimits buildAdminAccessLimits(CatalogInfo info) {
        AccessLimits accessLimits;
        if (info instanceof LayerGroupInfo)
            accessLimits = buildLayerGroupAccessLimits(AccessInfo.ALLOW_ALL);
        else if (info instanceof ResourceInfo)
            accessLimits =
                    buildResourceAccessLimits((ResourceInfo) info, AccessInfo.ALLOW_ALL, null);
        else
            accessLimits =
                    buildResourceAccessLimits(
                            ((LayerInfo) info).getResource(), AccessInfo.ALLOW_ALL, null);
        return accessLimits;
    }

    private String getUserNameFromAuth(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        if (username != null && username.isEmpty()) {
            username = null;
        }
        return username;
    }

    private Collection<LayerGroupSummary> getGroupSummary(Object resource) {
        Collection<LayerGroupSummary> summaries;
        if (resource instanceof ResourceInfo)
            summaries = groupsCache.getContainerGroupsFor((ResourceInfo) resource);
        else if (resource instanceof LayerInfo)
            summaries = groupsCache.getContainerGroupsFor(((LayerInfo) resource).getResource());
        else summaries = groupsCache.getContainerGroupsFor((LayerGroupInfo) resource);
        return summaries == null ? List.of() : summaries;
    }

    /**
     * Build the access info for a Resource, taking into account the containerRule if any exists
     *
     * @param info the ResourceInfo object for which the AccessLimits are requested
     * @param accessInfo the AccessInfo associated to the resource need to be reprojected due the
     *     possible difference between container and resource CRS
     * @return the AccessLimits of the Resource
     */
    DataAccessLimits buildResourceAccessLimits(
            ResourceInfo info, AccessInfo accessInfo, ProcessingResult resultLimits) {

        final CatalogMode catalogMode = getCatalogMode(accessInfo, resultLimits);

        if (info instanceof FeatureTypeInfo) {

            return buildVectorAccessLimits(info, accessInfo, resultLimits, catalogMode);

        } else if (info instanceof CoverageInfo) {

            return buildCoverageAccessLimits(info, accessInfo, resultLimits, catalogMode);

        } else if (info instanceof WMSLayerInfo) {

            return buildWMSAccessLimits(info, accessInfo, resultLimits, catalogMode);

        } else if (info instanceof WMTSLayerInfo) {

            return buildWMTSAccessLimits(info, accessInfo, resultLimits, catalogMode);
        }
        throw new IllegalArgumentException("Don't know how to handle resource " + info);
    }

    private WMTSAccessLimits buildWMTSAccessLimits(
            ResourceInfo info,
            AccessInfo accessInfo,
            ProcessingResult resultLimits,
            final CatalogMode catalogMode) {

        final Geometry intersectsArea = resolveIntersectsArea(info, accessInfo, resultLimits);
        Filter readFilter = toFilter(accessInfo.getGrant(), accessInfo.getCqlFilterRead());
        MultiPolygon multiPoly = toMultiPoly(intersectsArea);
        return new WMTSAccessLimits(catalogMode, readFilter, multiPoly);
    }

    private WMSAccessLimits buildWMSAccessLimits(
            ResourceInfo info,
            AccessInfo accessInfo,
            ProcessingResult resultLimits,
            final CatalogMode catalogMode) {

        final Geometry intersectsArea = resolveIntersectsArea(info, accessInfo, resultLimits);
        Filter readFilter = toFilter(accessInfo.getGrant(), accessInfo.getCqlFilterRead());
        boolean allowFeatureInfo = true;
        MultiPolygon multiPoly = toMultiPoly(intersectsArea);
        return new WMSAccessLimits(catalogMode, readFilter, multiPoly, allowFeatureInfo);
    }

    private CoverageAccessLimits buildCoverageAccessLimits(
            ResourceInfo info,
            AccessInfo accessInfo,
            ProcessingResult resultLimits,
            final CatalogMode catalogMode) {

        final Geometry intersectsArea = resolveIntersectsArea(info, accessInfo, resultLimits);
        final Geometry clipArea = resolveClipArea(info, accessInfo, resultLimits);
        Filter readFilter = toFilter(accessInfo.getGrant(), accessInfo.getCqlFilterRead());
        Geometry finalArea = null;
        if (clipArea != null && intersectsArea != null) {
            finalArea = clipArea.union(intersectsArea);
        } else if (intersectsArea != null) {
            finalArea = intersectsArea;
        } else if (clipArea != null) {
            finalArea = clipArea;
        }

        MultiPolygon multiPoly = toMultiPoly(finalArea);
        return new CoverageAccessLimits(catalogMode, readFilter, multiPoly, null);
    }

    private VectorAccessLimits buildVectorAccessLimits(
            ResourceInfo info,
            AccessInfo accessInfo,
            ProcessingResult resultLimits,
            final CatalogMode catalogMode) {

        // merge the area among the filters
        final Geometry intersectsArea = resolveIntersectsArea(info, accessInfo, resultLimits);
        final Geometry clipArea = resolveClipArea(info, accessInfo, resultLimits);
        Filter readFilter = toFilter(accessInfo.getGrant(), accessInfo.getCqlFilterRead());
        Filter writeFilter = toFilter(accessInfo.getGrant(), accessInfo.getCqlFilterWrite());
        if (intersectsArea != null) {
            Filter areaFilter = FF.intersects(FF.property(""), FF.literal(intersectsArea));
            if (clipArea != null) {
                Filter intersectClipArea = FF.intersects(FF.property(""), FF.literal(clipArea));
                areaFilter = FF.or(areaFilter, intersectClipArea);
            }
            readFilter = mergeFilter(readFilter, areaFilter);

            writeFilter = mergeFilter(writeFilter, areaFilter);
        }

        // get the attributes
        final List<PropertyName> readAttributes =
                toPropertyNames(accessInfo.getAttributes(), PropertyAccessMode.READ);
        final List<PropertyName> writeAttributes =
                toPropertyNames(accessInfo.getAttributes(), PropertyAccessMode.WRITE);

        VectorAccessLimits accessLimits =
                new VectorAccessLimits(
                        catalogMode, readAttributes, readFilter, writeAttributes, writeFilter);

        if (clipArea != null) {
            accessLimits.setClipVectorFilter(clipArea);
        }
        if (intersectsArea != null) accessLimits.setIntersectVectorFilter(intersectsArea);

        return accessLimits;
    }

    private Geometry resolveIntersectsArea(
            ResourceInfo info, AccessInfo accessInfo, ProcessingResult resultLimits) {

        if (resultLimits == null) {
            CoordinateReferenceSystem crs = GeomHelper.getCRSFromInfo(info);
            return adaptAndReproject(accessInfo.getArea(), crs);
        }
        return resultLimits.getIntersectArea();
    }

    private Geometry resolveClipArea(
            ResourceInfo info, AccessInfo accessInfo, ProcessingResult resultLimits) {

        if (resultLimits == null) {
            CoordinateReferenceSystem crs = GeomHelper.getCRSFromInfo(info);
            return adaptAndReproject(accessInfo.getClipArea(), crs);
        }
        return resultLimits.getClipArea();
    }

    private Geometry adaptAndReproject(
            org.geolatte.geom.Geometry<?> area, CoordinateReferenceSystem crs) {
        Geometry jtsArea = GeomHelper.toJTS(area);
        return GeomHelper.reprojectGeometry(jtsArea, crs);
    }

    private Filter toFilter(GrantType actualGrant, @Nullable String cqlFilter) {
        if (cqlFilter != null) {
            try {
                return ECQL.toFilter(cqlFilter);
            } catch (CQLException e) {
                throw new IllegalArgumentException(
                        "Invalid cql filter found: " + e.getMessage(), e);
            }
        }
        boolean includeFilter = actualGrant == GrantType.ALLOW || actualGrant == GrantType.LIMIT;
        return includeFilter ? Filter.INCLUDE : Filter.EXCLUDE;
    }

    /**
     * @param accessInfo the AccessInfo associated to the LayerGroup
     * @return the AccessLimits of the LayerGroup
     */
    LayerGroupAccessLimits buildLayerGroupAccessLimits(AccessInfo accessInfo) {
        GrantType grant = accessInfo.getGrant();
        // the SecureCatalog will grant access to the layerGroup
        // if AccessLimits are null
        if (grant.equals(GrantType.ALLOW) || grant.equals(GrantType.LIMIT)) {
            return null; // null == no-limits
        }
        CatalogMode catalogMode = convert(accessInfo.getCatalogMode());
        return new LayerGroupAccessLimits(catalogMode);
    }

    private ProcessingResult getContainerResolverResult(
            CatalogInfo resourceInfo,
            String layer,
            String workspace,
            Authentication user,
            List<LayerGroupInfo> containers,
            Collection<LayerGroupSummary> summaries) {

        AccessManagerConfig configuration = configProvider.get();
        ContainerLimitResolver resolver =
                ContainerLimitResolver.of(
                        containers, summaries, aclService, user, layer, workspace, configuration);

        ProcessingResult result = resolver.resolveResourceInGroupLimits();
        Geometry intersect = result.getIntersectArea();
        Geometry clip = result.getClipArea();
        // areas might be in a srid different from the one of the resource
        // being requested.
        if (intersect != null || clip != null) {
            CoordinateReferenceSystem crs = GeomHelper.getCRSFromInfo(resourceInfo);
            intersect = GeomHelper.reprojectGeometry(intersect, crs);
            result.setIntersectArea(intersect);

            clip = GeomHelper.reprojectGeometry(clip, crs);
            result.setClipArea(clip);
        }

        return result;
    }

    // get the catalogMode for the resource privileging the container one if passed
    private CatalogMode getCatalogMode(AccessInfo accessInfo, ProcessingResult resultLimits) {

        org.geoserver.acl.domain.rules.CatalogMode ruleCatalogMode;
        if (resultLimits == null) {
            ruleCatalogMode = accessInfo.getCatalogMode();
        } else {
            ruleCatalogMode = resultLimits.getCatalogModeDTO();
        }
        return convert(ruleCatalogMode);
    }

    private CatalogMode convert(org.geoserver.acl.domain.rules.CatalogMode ruleCatalogMode) {
        if (ruleCatalogMode != null) {
            switch (ruleCatalogMode) {
                case CHALLENGE:
                    return CatalogMode.CHALLENGE;
                case HIDE:
                    return CatalogMode.HIDE;
                case MIXED:
                    return CatalogMode.MIXED;
                default:
                    return DEFAULT_CATALOG_MODE;
            }
        }
        return DEFAULT_CATALOG_MODE;
    }

    // Builds a rule filter to retrieve the AccessInfo for the resource
    private AccessRequest buildAccessRequest(String workspace, String layer, Authentication user) {

        AccessManagerConfig configuration = configProvider.get();
        return new AccessRequestBuilder(configuration)
                .request(Dispatcher.REQUEST.get())
                .workspace(workspace)
                .layer(layer)
                .user(user)
                .build();
    }

    private MultiPolygon toMultiPoly(Geometry reprojArea) {
        MultiPolygon rasterFilter = null;
        if (reprojArea != null) {
            rasterFilter = Converters.convert(reprojArea, MultiPolygon.class);
            if (rasterFilter == null) {
                throw new IllegalArgumentException(
                        "Error applying security rules, cannot convert the ACL area restriction to a multi-polygon: "
                                + reprojArea.toText());
            }
        }

        return rasterFilter;
    }

    /** Merges the two filters into one by AND */
    private Filter mergeFilter(Filter filter, Filter areaFilter) {
        if (filter == null || filter == Filter.INCLUDE) {
            return areaFilter;
        }
        if (filter == Filter.EXCLUDE) {
            return filter;
        }
        return FF.and(filter, areaFilter);
    }

    /**
     * Builds the equivalent {@link PropertyName} list for the specified access mode
     *
     * @return {@code null} if attributes is empty, note {@link ResourceAccessManagerWrapper}
     *     depends on {@code null}. The mapped property names otherwise.
     */
    private List<PropertyName> toPropertyNames(
            Set<LayerAttribute> attributes, PropertyAccessMode mode) {
        // handle simple case
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }

        // filter and translate
        List<PropertyName> result = new ArrayList<>();
        for (LayerAttribute attribute : attributes) {
            AccessType access = attribute.getAccess();
            boolean alwaysVisible = access == AccessType.READWRITE;
            if (alwaysVisible
                    || (mode == PropertyAccessMode.READ && access == AccessType.READONLY)) {
                PropertyName property = FF.property(attribute.getName());
                result.add(property);
            }
        }

        return result;
    }

    @Override
    public Filter getSecurityFilter(Authentication user, Class<? extends CatalogInfo> clazz) {
        return Predicates.acceptAll();
    }

    @Override
    public int getPriority() {
        return ExtensionPriority.LOWEST;
    }
}
