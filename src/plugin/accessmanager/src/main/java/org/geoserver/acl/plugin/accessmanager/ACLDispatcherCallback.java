/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.LocalWorkspaceCatalog;
import org.geoserver.ows.AbstractDispatcherCallback;
import org.geoserver.ows.Request;
import org.geoserver.ows.util.KvpUtils;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.wms.GetFeatureInfoRequest;
import org.geoserver.wms.GetLegendGraphicRequest;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.MapLayerInfo;
import org.geoserver.wms.WMS;
import org.geoserver.wms.map.GetMapKvpRequestReader;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.style.Style;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * {@link ResourceAccessManager} to make GeoServer use the ACL service to assess data access rules
 *
 * @author Andrea Aime - GeoSolutions - Originally as part of GeoFence's GeoServer extension
 * @author Emanuele Tajariol- GeoSolutions - Originally as part of GeoFence's GeoServer extension
 */
public class ACLDispatcherCallback extends AbstractDispatcherCallback {

    private static final Logger LOGGER = Logging.getLogger(ACLDispatcherCallback.class);

    static final FilterFactory FF = CommonFactoryFinder.getFilterFactory(null);

    private AuthorizationService aclService;

    private Catalog catalog;

    private final AccessManagerConfigProvider configProvider;

    public ACLDispatcherCallback(
            AuthorizationService aclService,
            LocalWorkspaceCatalog catalog,
            AccessManagerConfigProvider configProvider) {

        this.aclService = aclService;
        this.catalog = catalog;
        this.configProvider = configProvider;
    }

    @Override
    public Operation operationDispatched(Request gsRequest, Operation operation) {
        // service and request
        String service = gsRequest.getService();
        String request = gsRequest.getRequest();

        // get the user
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        // shortcut, if the user is the admin, he can do everything
        if (ACLResourceAccessManager.isAdmin(user)) {
            LOGGER.finer("Admin level access, not applying default style for this request");
            return operation;
        }

        if ((request != null)
                && "WMS".equalsIgnoreCase(service)
                && ("GetMap".equalsIgnoreCase(request) || "GetFeatureInfo".equalsIgnoreCase(request))) {
            // extract the getmap part
            Object ro = operation.getParameters()[0];
            GetMapRequest getMap;
            if (ro instanceof GetMapRequest) {
                getMap = (GetMapRequest) ro;
            } else if (ro instanceof GetFeatureInfoRequest) {
                getMap = ((GetFeatureInfoRequest) ro).getGetMapRequest();
            } else {
                throw new ServiceException("Unrecognized request object: " + ro);
            }

            overrideGetMapRequest(gsRequest, service, request, user, getMap);
        } else if ((request != null)
                && "WMS".equalsIgnoreCase(service)
                && "GetLegendGraphic".equalsIgnoreCase(request)) {
            overrideGetLegendGraphicRequest(gsRequest, operation, service, request, user);
        }

        return operation;
    }

    void overrideGetLegendGraphicRequest(
            Request gsRequest, Operation operation, String service, String request, Authentication user) {
        // get the layer
        String layerName = (String) gsRequest.getKvp().get("LAYER");
        String reqStyle = (String) gsRequest.getKvp().get("STYLE");
        List<String> styles = new ArrayList<>();
        List<LayerInfo> layers = new ArrayList<>();
        LayerInfo candidateLayer = catalog.getLayerByName(layerName);
        if (candidateLayer == null) {
            LayerGroupInfo layerGroup = catalog.getLayerGroupByName(layerName);
            if (layerGroup != null) {
                boolean emptyStyleName = reqStyle == null || "".equals(reqStyle);
                layers.addAll(emptyStyleName ? layerGroup.layers() : layerGroup.layers(reqStyle));
                addGroupStyles(layerGroup, styles, reqStyle);
            }
        } else {
            layers.add(candidateLayer);
            styles.add(reqStyle);
        }

        // get the request object
        GetLegendGraphicRequest getLegend = (GetLegendGraphicRequest) operation.getParameters()[0];
        for (int i = 0; i < layers.size(); i++) {
            LayerInfo layer = layers.get(i);
            ResourceInfo resource = layer.getResource();

            // get the rule, it contains default and allowed styles
            AccessRequest ruleFilter = new AccessRequestBuilder(configProvider.get())
                    .user(user)
                    .service(service)
                    .request(request)
                    .workspace(resource.getStore().getWorkspace().getName())
                    .layer(resource.getName())
                    .build();

            LOGGER.log(Level.FINEST, "Getting access limits for getLegendGraphic: {0}", ruleFilter);
            AccessInfo grant = aclService.getAccessInfo(ruleFilter);

            // get the requested style
            String styleName = styles.get(i);
            if (styleName == null) {
                if (grant.getDefaultStyle() != null) {
                    try {
                        StyleInfo si = catalog.getStyleByName(grant.getDefaultStyle());
                        if (si == null) {
                            throw new ServiceException("Could not find default style suggested "
                                    + "by GeoRepository: "
                                    + grant.getDefaultStyle());
                        }
                        getLegend.setStyle(si.getStyle());
                    } catch (IOException e) {
                        throw new ServiceException(
                                "Unable to load the style suggested by GeoRepository: " + grant.getDefaultStyle(), e);
                    }
                }
            } else {
                checkStyleAllowed(grant, styleName);
            }
        }
    }

    void overrideGetMapRequest(
            Request gsRequest, String service, String request, Authentication user, GetMapRequest getMap) {

        if (gsRequest.getKvp().get("layers") == null
                && gsRequest.getKvp().get("sld") == null
                && gsRequest.getKvp().get("sld_body") == null) {
            throw new ServiceException("GetMap POST requests are forbidden");
        }

        // parse the styles param like the kvp parser would (since we have no way,
        // to know if a certain style was requested explicitly or defaulted, and
        // we need to tell apart the default case from the explicit request case
        List<String> styleNameList = getRequestedStyles(gsRequest, getMap);

        // apply the override/security check for each layer in the request
        List<MapLayerInfo> layers = getMap.getLayers();
        for (int i = 0; i < layers.size(); i++) {
            MapLayerInfo layer = layers.get(i);
            ResourceInfo info = null;
            if (layer.getType() == MapLayerInfo.TYPE_VECTOR || layer.getType() == MapLayerInfo.TYPE_RASTER) {
                info = layer.getResource();
            } else if (!configProvider.get().isAllowRemoteAndInlineLayers()) {
                throw new ServiceException("Remote layers are not allowed");
            }

            // get the rule, it contains default and allowed styles
            AccessRequest ruleFilter;
            {
                String workspace =
                        info == null ? null : info.getStore().getWorkspace().getName();
                String layerName = info == null ? null : info.getName();
                ruleFilter = new AccessRequestBuilder(configProvider.get())
                        .user(user)
                        .service(service)
                        .request(request)
                        .workspace(workspace)
                        .layer(layerName)
                        .build();
            }
            LOGGER.log(Level.FINEST, "Getting access limits for getMap {0}:", ruleFilter);

            AccessInfo rule = aclService.getAccessInfo(ruleFilter);

            // get the requested style name
            String styleName = styleNameList.get(i);

            // if default use ACL's default
            if (styleName != null) {
                checkStyleAllowed(rule, styleName);
            } else if ((rule.getDefaultStyle() != null)) {
                try {
                    StyleInfo si = catalog.getStyleByName(rule.getDefaultStyle());
                    if (si == null) {
                        throw new ServiceException(
                                "Could not find default style suggested by ACL: " + rule.getDefaultStyle());
                    }

                    Style style = si.getStyle();
                    getMap.getStyles().set(i, style);
                } catch (IOException e) {
                    throw new ServiceException(
                            "Unable to load the style suggested by ACL: " + rule.getDefaultStyle(), e);
                }
            }
        }
    }

    private void checkStyleAllowed(AccessInfo accessInfo, String styleName) {
        // otherwise check if the requested style is allowed
        Set<String> allowedStyles = new HashSet<>();
        if (accessInfo.getDefaultStyle() != null) {
            allowedStyles.add(accessInfo.getDefaultStyle());
        }
        if (accessInfo.getAllowedStyles() != null) {
            allowedStyles.addAll(accessInfo.getAllowedStyles());
        }

        if ((!allowedStyles.isEmpty()) && !allowedStyles.contains(styleName)) {
            throw new ServiceException("The '" + styleName + "' style is not available on this layer");
        }
    }

    /**
     * Returns a list that contains the request styles that will correspond to the
     * GetMap.getLayers().
     */
    private List<String> getRequestedStyles(Request gsRequest, GetMapRequest getMap) {
        List<String> requestedStyles = new ArrayList<>();
        int styleIndex = 0;
        List<String> parsedStyles = parseStylesParameter(gsRequest);
        for (Object layer : parseLayersParameter(gsRequest, getMap)) {
            boolean outOfBound = styleIndex >= parsedStyles.size();
            if (layer instanceof LayerGroupInfo) {
                String styleName = outOfBound ? null : parsedStyles.get(styleIndex);
                addGroupStyles((LayerGroupInfo) layer, requestedStyles, styleName);
            } else {
                // the layer is a LayerInfo or MapLayerInfo (if it is a remote layer)
                if (outOfBound) {
                    requestedStyles.add(null);
                } else {
                    requestedStyles.add(parsedStyles.get(styleIndex));
                }
            }
            styleIndex++;
        }
        return requestedStyles;
    }

    private void addGroupStyles(LayerGroupInfo groupInfo, List<String> requestedStyles, String styleName) {
        List<StyleInfo> groupStyles;
        if (styleName != null && !"".equals(styleName)) groupStyles = groupInfo.styles(styleName);
        else groupStyles = groupInfo.styles();

        requestedStyles.addAll(groupStyles.stream()
                .map(s -> s != null ? s.prefixedName() : null)
                .collect(Collectors.toList()));
    }

    private List<Object> parseLayersParameter(Request gsRequest, GetMapRequest getMap) {
        String rawLayersParameter = (String) gsRequest.getRawKvp().get("LAYERS");
        if (rawLayersParameter != null) {
            List<String> layersNames = KvpUtils.readFlat(rawLayersParameter);
            return LayersKvpParser.getInstance()
                    .parseLayers(layersNames, getMap.getRemoteOwsURL(), getMap.getRemoteOwsType());
        }
        return new ArrayList<>();
    }

    private List<String> parseStylesParameter(Request gsRequest) {
        String rawStylesParameter = (String) gsRequest.getRawKvp().get("STYLES");
        if (rawStylesParameter != null) {
            return KvpUtils.readFlat(rawStylesParameter);
        }
        return new ArrayList<>();
    }

    /** helper that avoids duplicating the code to parse the layers parameter */
    static final class LayersKvpParser extends GetMapKvpRequestReader {

        private static LayersKvpParser singleton = null;

        public static LayersKvpParser getInstance() {
            if (singleton == null) singleton = new LayersKvpParser();
            return singleton;
        }

        private LayersKvpParser() {
            super(WMS.get());
        }

        @Override
        public List<Object> parseLayers(List<String> requestedLayerNames, URL remoteOwsUrl, String remoteOwsType) {
            try {
                return super.parseLayers(requestedLayerNames, remoteOwsUrl, remoteOwsType);
            } catch (Exception exception) {
                throw new ServiceException("Error parsing requested layers.", exception);
            }
        }
    }
}
