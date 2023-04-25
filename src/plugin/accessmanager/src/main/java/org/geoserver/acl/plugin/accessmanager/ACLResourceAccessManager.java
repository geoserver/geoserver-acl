/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
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
import org.geoserver.acl.plugin.accessmanager.wps.WPSAccessInfo;
import org.geoserver.acl.plugin.accessmanager.wps.WPSHelper;
import org.geoserver.acl.plugin.support.GeomHelper;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.Predicates;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.LocalWorkspaceCatalog;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.platform.ExtensionPriority;
import org.geoserver.security.AccessLimits;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.CoverageAccessLimits;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.LayerGroupAccessLimits;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.StyleAccessLimits;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WMSAccessLimits;
import org.geoserver.security.WMTSAccessLimits;
import org.geoserver.security.WorkspaceAccessLimits;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.LayerGroupContainmentCache;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link ResourceAccessManager} to make GeoServer use the ACL {@link AuthorizationService} to
 * assess data access rules
 *
 * @author Andrea Aime - GeoSolutions - Originally as part of GeoFence's GeoServer extension
 * @author Emanuele Tajariol- GeoSolutions - Originally as part of GeoFence's GeoServer extension
 */
public class ACLResourceAccessManager implements ResourceAccessManager, ExtensionPriority {

    private static final Logger LOGGER = Logging.getLogger(ACLResourceAccessManager.class);

    static final FilterFactory2 FF = CommonFactoryFinder.getFilterFactory2(null);

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
            Catalog catalog,
            AccessManagerConfigProvider configurationManager,
            WPSHelper wpsHelper) {

        this.aclService = aclService;
        this.configProvider = configurationManager;
        this.groupsCache = new LayerGroupContainmentCache(new LocalWorkspaceCatalog(catalog));
        this.wpsHelper = wpsHelper;
    }

    public AccessManagerConfig getConfig() {
        return this.configProvider.get();
    }

    /**
     * sets the layer group cache
     *
     * @param groupsCache
     */
    public void setGroupsCache(LayerGroupContainmentCache groupsCache) {
        this.groupsCache = groupsCache;
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
        log(FINE, "Getting access limits for workspace {0}", workspace.getName());

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
        log(FINE, "Getting admin auth for Workspace {0}", workspaceName);

        final String sourceAddress = retrieveCallerIpAddress();

        AdminAccessRequest request =
                new AdminAccessRequestBuilder(configProvider.get())
                        .user(user)
                        .workspace(workspaceName)
                        .ipAddress(sourceAddress)
                        .build();

        AdminAccessInfo grant = aclService.getAdminAuthorization(request);

        log(
                FINE,
                "Admin auth for User:{0} Workspace:{1}: {2}",
                user.getName(),
                workspaceName,
                grant.isAdmin());

        return grant.isAdmin();
    }

    String getSourceAddress(HttpServletRequest http) {
        if (http == null) {
            log(WARNING, "No HTTP request available.");
            return null;
        }

        String sourceAddress = null;
        try {
            final String forwardedFor = http.getHeader("X-Forwarded-For");
            final String remoteAddr = http.getRemoteAddr();
            if (forwardedFor != null) {
                String[] ips = forwardedFor.split(", ");
                sourceAddress = InetAddress.getByName(ips[0]).getHostAddress();
            } else if (remoteAddr != null) {
                // Returns an IP address, removes surrounding brackets present in case of IPV6
                // addresses
                sourceAddress = remoteAddr.replaceAll("[\\[\\]]", "");
            }
        } catch (Exception e) {
            log(INFO, "Failed to get remote address", e);
        }
        return sourceAddress;
    }

    private String retrieveCallerIpAddress() {

        String reqSource = "Dispatcher.REQUEST";
        final HttpServletRequest request;

        // is this an OWS request
        Request owsRequest = Dispatcher.REQUEST.get();
        if (owsRequest != null) {
            request = owsRequest.getHttpRequest();
        } else {
            reqSource = "Spring Request";
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            request =
                    requestAttributes == null
                            ? null
                            : ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        try {
            String sourceAddress = getSourceAddress(request);
            if (sourceAddress == null) {
                log(WARNING, "Could not retrieve source address from {0}", reqSource);
            }
            return sourceAddress;
        } catch (RuntimeException ex) {
            log(
                    WARNING,
                    "Error retrieving source address with {0}: {1}",
                    reqSource,
                    ex.getMessage());
            return null;
        }
    }

    @Override
    public StyleAccessLimits getAccessLimits(Authentication user, StyleInfo style) {
        // return getAccessLimits(user, style.getResource());
        LOGGER.fine("Not limiting styles");
        return null;
        // TODO
    }

    @Override
    public LayerGroupAccessLimits getAccessLimits(Authentication user, LayerGroupInfo layerInfo) {
        return getAccessLimits(user, layerInfo, Collections.emptyList());
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, LayerInfo layer) {
        LOGGER.log(Level.FINE, "Getting access limits for Layer {0}", layer.getName());
        return getAccessLimits(user, layer, Collections.emptyList());
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication user, ResourceInfo resource) {
        LOGGER.log(Level.FINE, "Getting access limits for Resource {0}", resource.getName());
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
            log(FINE, "Admin level access, returning full rights for layer {0}", layer);
            return buildAdminAccessLimits(info);
        }

        final String ipAddress = retrieveCallerIpAddress();

        AccessRequest accessRequest = buildAccessRequest(workspace, layer, user, ipAddress);
        Stopwatch sw = Stopwatch.createStarted();
        AccessInfo accessInfo = aclService.getAccessInfo(accessRequest);
        sw.stop();
        log(FINE, "ACL auth run in {0}: {0}. response ({1}): {2}", sw, accessRequest, accessInfo);

        if (accessInfo == null) {
            accessInfo = AccessInfo.DENY_ALL;
            log(WARNING, "ACL returning null AccessInfo for {0}", accessRequest);
        }

        Request req = Dispatcher.REQUEST.get();
        String service = req != null ? req.getService() : null;
        boolean isWms = "WMS".equalsIgnoreCase(service);
        boolean noLayerGroups = CollectionUtils.isEmpty(containers);

        ContainerLimitResolver.ProcessingResult processingResult = null;
        if (noLayerGroups && isWms) {
            // is direct access we need to retrieve eventually present groups.
            Collection<LayerGroupContainmentCache.LayerGroupSummary> summaries =
                    getGroupSummary(info);
            if (summaries != null && !summaries.isEmpty()) {
                boolean allOpaque = allOpaque(summaries);
                // all opaque we deny and don't perform any resolution of group limits.
                if (allOpaque) accessInfo = accessInfo.withGrant(GrantType.DENY);
                boolean anySingle =
                        summaries.stream()
                                .anyMatch(gs -> gs.getMode().equals(LayerGroupInfo.Mode.SINGLE));
                // if a single group is present we don't apply any limit from containers.
                if (!anySingle && !allOpaque)
                    processingResult =
                            getContainerResolverResult(
                                    info,
                                    layer,
                                    workspace,
                                    configProvider.get(),
                                    ipAddress,
                                    user,
                                    null,
                                    summaries);
            }
        } else if (!noLayerGroups) {
            // layer is requested in context of a layer group.
            // we need to process the containers limits.
            processingResult =
                    getContainerResolverResult(
                            info,
                            layer,
                            workspace,
                            configProvider.get(),
                            ipAddress,
                            user,
                            containers,
                            null);
        }

        if ("WPS".equalsIgnoreCase(service)) {
            if (!noLayerGroups) {
                log(
                        WARNING,
                        "Don't know how to deal with WPS requests for group data. Won't dive into single process limits.");
            } else {
                WPSAccessInfo resolvedAccessInfo =
                        wpsHelper.resolveWPSAccess(req, accessRequest, accessInfo);
                if (resolvedAccessInfo != null) {
                    accessInfo = resolvedAccessInfo.getAccessInfo();
                    processingResult =
                            new ContainerLimitResolver.ProcessingResult(
                                    resolvedAccessInfo.getArea(),
                                    resolvedAccessInfo.getClip(),
                                    accessInfo.getCatalogMode());

                    log(
                            FINE,
                            "Got WPS access {0} for layer {1} and user {2}",
                            accessInfo,
                            layer,
                            getUserNameFromAuth(user));
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

    private void log(Level level, String msg, Object... params) {
        if (LOGGER.isLoggable(level)) {
            LOGGER.log(level, msg, params);
        }
    }

    private boolean allOpaque(Collection<LayerGroupContainmentCache.LayerGroupSummary> summaries) {
        LayerGroupInfo.Mode opaque = LayerGroupInfo.Mode.OPAQUE_CONTAINER;
        return summaries.stream().allMatch(gs -> gs.getMode().equals(opaque));
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

    private Collection<LayerGroupContainmentCache.LayerGroupSummary> getGroupSummary(
            Object resource) {
        Collection<LayerGroupContainmentCache.LayerGroupSummary> summaries;
        if (resource instanceof ResourceInfo)
            summaries = groupsCache.getContainerGroupsFor((ResourceInfo) resource);
        else if (resource instanceof LayerInfo)
            summaries = groupsCache.getContainerGroupsFor(((LayerInfo) resource).getResource());
        else summaries = groupsCache.getContainerGroupsFor((LayerGroupInfo) resource);
        return summaries;
    }

    /**
     * Build the access info for a Resource, taking into account the containerRule if any exists
     *
     * @param info the ResourceInfo object for which the AccessLimits are requested
     * @param accessInfo the AccessInfo associated to the resource need to be reprojected due the
     *     possible difference between container and resource CRS
     * @return the AccessLimits of the Resource
     */
    AccessLimits buildResourceAccessLimits(
            ResourceInfo info,
            AccessInfo accessInfo,
            ContainerLimitResolver.ProcessingResult resultLimits) {

        GrantType actualGrant = accessInfo.getGrant();
        boolean includeFilter = actualGrant == GrantType.ALLOW || actualGrant == GrantType.LIMIT;
        Filter readFilter = includeFilter ? Filter.INCLUDE : Filter.EXCLUDE;
        Filter writeFilter = includeFilter ? Filter.INCLUDE : Filter.EXCLUDE;
        try {
            if (accessInfo.getCqlFilterRead() != null) {
                readFilter = ECQL.toFilter(accessInfo.getCqlFilterRead());
            }
            if (accessInfo.getCqlFilterWrite() != null) {
                writeFilter = ECQL.toFilter(accessInfo.getCqlFilterWrite());
            }
        } catch (CQLException e) {
            throw new IllegalArgumentException("Invalid cql filter found: " + e.getMessage(), e);
        }

        // get the attributes
        List<PropertyName> readAttributes =
                toPropertyNames(accessInfo.getAttributes(), PropertyAccessMode.READ);
        List<PropertyName> writeAttributes =
                toPropertyNames(accessInfo.getAttributes(), PropertyAccessMode.WRITE);

        Geometry intersectsArea;
        Geometry clipArea;
        if (resultLimits != null) {
            intersectsArea = resultLimits.getIntersectArea();
            clipArea = resultLimits.getClipArea();
        } else {
            CoordinateReferenceSystem crs = GeomHelper.getCRSFromInfo(info);

            intersectsArea = GeomHelper.toJTS(accessInfo.getArea());
            intersectsArea = GeomHelper.reprojectGeometry(intersectsArea, crs);

            clipArea = GeomHelper.toJTS(accessInfo.getClipArea());
            clipArea = GeomHelper.reprojectGeometry(clipArea, crs);
        }
        CatalogMode catalogMode = getCatalogMode(accessInfo, resultLimits);
        log(FINE, "Returning mode {0} for resource {1}", catalogMode, info);

        AccessLimits accessLimits = null;
        if (info instanceof FeatureTypeInfo) {
            // merge the area among the filters
            if (intersectsArea != null) {
                Filter areaFilter = FF.intersects(FF.property(""), FF.literal(intersectsArea));
                if (clipArea != null) {
                    Filter intersectClipArea = FF.intersects(FF.property(""), FF.literal(clipArea));
                    areaFilter = FF.or(areaFilter, intersectClipArea);
                }
                readFilter = mergeFilter(readFilter, areaFilter);
                writeFilter = mergeFilter(writeFilter, areaFilter);
            }

            accessLimits =
                    new VectorAccessLimits(
                            catalogMode, readAttributes, readFilter, writeAttributes, writeFilter);

            if (clipArea != null) {
                ((VectorAccessLimits) accessLimits).setClipVectorFilter(clipArea);
            }
            if (intersectsArea != null)
                ((VectorAccessLimits) accessLimits).setIntersectVectorFilter(intersectsArea);

        } else if (info instanceof CoverageInfo) {

            Geometry finalArea = null;
            if (clipArea != null && intersectsArea != null)
                finalArea = clipArea.union(intersectsArea);
            else if (intersectsArea != null) finalArea = intersectsArea;
            else if (clipArea != null) finalArea = clipArea;

            accessLimits =
                    new CoverageAccessLimits(catalogMode, readFilter, toMultiPoly(finalArea), null);

        } else if (info instanceof WMSLayerInfo) {
            accessLimits =
                    new WMSAccessLimits(catalogMode, readFilter, toMultiPoly(intersectsArea), true);

        } else if (info instanceof WMTSLayerInfo) {
            accessLimits =
                    new WMTSAccessLimits(catalogMode, readFilter, toMultiPoly(intersectsArea));
        } else {
            throw new IllegalArgumentException("Don't know how to handle resource " + info);
        }

        return accessLimits;
    }

    /**
     * @param accessInfo the AccessInfo associated to the LayerGroup
     * @return the AccessLimits of the LayerGroup
     */
    AccessLimits buildLayerGroupAccessLimits(AccessInfo accessInfo) {
        GrantType grant = accessInfo.getGrant();
        // the SecureCatalog will grant access to the layerGroup
        // if AccessLimits are null
        if (grant.equals(GrantType.ALLOW) || grant.equals(GrantType.LIMIT)) {
            return null; // null == no-limits
        }
        return new LayerGroupAccessLimits(convert(accessInfo.getCatalogMode()));
    }

    private ContainerLimitResolver.ProcessingResult getContainerResolverResult(
            CatalogInfo resourceInfo,
            String layer,
            String workspace,
            AccessManagerConfig configuration,
            String callerIp,
            Authentication user,
            List<LayerGroupInfo> containers,
            Collection<LayerGroupContainmentCache.LayerGroupSummary> summaries) {
        ContainerLimitResolver resolver;
        if (summaries != null)
            resolver =
                    new ContainerLimitResolver(
                            summaries, aclService, user, layer, workspace, callerIp, configuration);
        else
            resolver =
                    new ContainerLimitResolver(
                            containers,
                            aclService,
                            user,
                            layer,
                            workspace,
                            callerIp,
                            configuration);

        ContainerLimitResolver.ProcessingResult result = resolver.resolveResourceInGroupLimits();
        Geometry intersect = result.getIntersectArea();
        Geometry clip = result.getClipArea();
        // areas might be in a srid different from the one of the resource
        // being requested.
        CoordinateReferenceSystem crs = GeomHelper.getCRSFromInfo(resourceInfo);
        if (intersect != null) {
            intersect = GeomHelper.reprojectGeometry(intersect, crs);
            result.setIntersectArea(intersect);
        }
        if (clip != null) {
            clip = GeomHelper.reprojectGeometry(clip, crs);
            result.setClipArea(clip);
        }
        return result;
    }

    // get the catalogMode for the resource privileging the container one if passed
    private CatalogMode getCatalogMode(
            AccessInfo accessInfo, ContainerLimitResolver.ProcessingResult resultLimits) {
        org.geoserver.acl.domain.rules.CatalogMode ruleCatalogMode;
        if (resultLimits != null) {
            ruleCatalogMode = resultLimits.getCatalogModeDTO();
        } else {
            ruleCatalogMode = accessInfo.getCatalogMode();
        }
        CatalogMode catalogMode = DEFAULT_CATALOG_MODE;
        if (ruleCatalogMode != null) {
            switch (ruleCatalogMode) {
                case CHALLENGE:
                    catalogMode = CatalogMode.CHALLENGE;
                    break;
                case HIDE:
                    catalogMode = CatalogMode.HIDE;
                    break;
                case MIXED:
                    catalogMode = CatalogMode.MIXED;
                    break;
            }
        }
        return catalogMode;
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
            }
        }
        return DEFAULT_CATALOG_MODE;
    }

    // Builds a rule filter to retrieve the AccessInfo for the resource
    private AccessRequest buildAccessRequest(
            String workspace, String layer, Authentication user, String ipAddress) {

        AccessManagerConfig configuration = configProvider.get();
        return new AccessRequestBuilder(configuration)
                .request(Dispatcher.REQUEST.get())
                .ipAddress(ipAddress)
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
                throw new RuntimeException(
                        "Error applying security rules, cannot convert "
                                + "the ACL area restriction "
                                + reprojArea.toText()
                                + " to a multi-polygon");
            }
        }

        return rasterFilter;
    }

    /** Merges the two filters into one by AND */
    private Filter mergeFilter(Filter filter, Filter areaFilter) {
        if ((filter == null) || (filter == Filter.INCLUDE)) {
            return areaFilter;
        } else if (filter == Filter.EXCLUDE) {
            return filter;
        }
        return FF.and(filter, areaFilter);
    }

    /** Builds the equivalent {@link PropertyName} list for the specified access mode */
    private List<PropertyName> toPropertyNames(
            Set<LayerAttribute> attributes, PropertyAccessMode mode) {
        // handle simple case
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }

        // filter and translate
        List<PropertyName> result = new ArrayList<>();
        for (LayerAttribute attribute : attributes) {
            if ((attribute.getAccess() == AccessType.READWRITE)
                    || ((mode == PropertyAccessMode.READ)
                            && (attribute.getAccess() == AccessType.READONLY))) {
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
