/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.wps;

import static org.geoserver.acl.domain.rules.GrantType.DENY;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.plugin.support.AccessInfoUtils;
import org.geoserver.acl.plugin.support.GeomHelper;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author etj (Emanuele Tajariol @ GeoSolutions) - Originally as part of GeoFence's GeoServer
 *     extension
 */
public class WPSHelper implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(WPSHelper.class);

    private AuthorizationService aclAuthService;

    ChainStatusHolder statusHolder = null;
    ExecutionIdRetriever executionIdRetriever = null;
    private boolean helperAvailable = false;

    public WPSHelper(AuthorizationService aclAuthService) {
        this.aclAuthService = aclAuthService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            statusHolder = applicationContext.getBean(ChainStatusHolder.class);
            executionIdRetriever = applicationContext.getBean(ExecutionIdRetriever.class);
            helperAvailable = true;
        } catch (NoSuchBeanDefinitionException e) {
            LOGGER.warning(
                    "ACL-WPS integration classes not available. Please include gs-acl-plugin-wps module if the WPS service is running.");
        }
    }

    /**
     * Resolve limits according to running process
     *
     * @param wpsAccessInfo Pre-computed accessInfo for default WPS access.
     * @return a WPSAccessInfo where the WKT of the AccessInfo should not be taken into
     *     consideration since the geometries are more up-to-date. Returns null if no forther
     *     resolution was computed.
     */
    public WPSAccessInfo resolveWPSAccess(final AccessRequest accessRequest, final AccessInfo wpsAccessInfo) {
        if (!helperAvailable) {
            LOGGER.warning("WPSHelper not available");
            // For more security we should deny the access, anyway let's tell
            // the caller we did nothing and it should go an as usual
            // in order not to break existing installations.
            return null;
        }

        String execId = executionIdRetriever.getCurrentExecutionId();
        final List<String> procNames = statusHolder.getCurrentStack(execId);

        List<AccessInfo> procAccessInfo = new LinkedList<>();

        for (String procName : procNames) {
            LOGGER.fine("Retrieving AccessInfo for proc " + procName);
            AccessRequest request = accessRequest.withSubfield(procName);

            AccessInfo accessInfo = aclAuthService.getAccessInfo(request);
            if (accessInfo.getGrant() == GrantType.DENY) {
                // shortcut: if at least one process is not allowed for current resource, do not
                // evaluate the other procs
                LOGGER.fine("Process " + procName + " not allowed to operate on layer");
                return new WPSAccessInfo(AccessInfo.DENY_ALL, null, null);
            }
            if (!accessInfo.equals(wpsAccessInfo)) {
                procAccessInfo.add(accessInfo);
            } else {
                // No specific rules for this proc, we're getting the generic WPS we already have
                LOGGER.fine("Skipping accessInfo for " + procName);
            }
        }

        // if we have at least one procAccessInfo, we should not consider the main  wpsAccessInfo,
        // bc the rules generating it are also considered in the more cohomprensive procAccessInfo
        if (procAccessInfo.isEmpty()) {
            return null;
        }
        return WPSHelper.intersect(procAccessInfo.toArray(new AccessInfo[0]));
    }

    /**
     * @return a WPSAccessInfo where the WKT of the AccessInfo should not be taken into
     *     consideration since the geometries are more up-to-date.
     */
    public static WPSAccessInfo intersect(AccessInfo... accessInfoArr) {

        AccessInfo ret = null;
        Geometry areaRet = null;
        Geometry clipRet = null;

        for (AccessInfo accessInfo : accessInfoArr) {
            if (accessInfo.getGrant() == DENY) {
                return new WPSAccessInfo(AccessInfo.DENY_ALL); // shortcut
            }

            Geometry area = GeomHelper.toJTS(accessInfo.getArea());
            Geometry clip = GeomHelper.toJTS(accessInfo.getClipArea());

            if (ret == null) { // get first entry as base entry
                ret = accessInfo;
                areaRet = area;
                clipRet = clip;
                continue;
            }

            areaRet = GeomHelper.reprojectAndIntersect(areaRet, area);
            clipRet = GeomHelper.reprojectAndIntersect(clipRet, clip);

            CatalogMode stricter = AccessInfoUtils.getStricter(ret.getCatalogMode(), accessInfo.getCatalogMode());

            // CQL (read + write)
            String cqlRead = AccessInfoUtils.intersectCQL(ret.getCqlFilterRead(), accessInfo.getCqlFilterRead());
            String cqlWrite = AccessInfoUtils.intersectCQL(ret.getCqlFilterWrite(), accessInfo.getCqlFilterWrite());

            // Attributes
            Set<LayerAttribute> attributes =
                    AccessInfoUtils.intersectAttributes(ret.getAttributes(), accessInfo.getAttributes());

            ret = ret.toBuilder()
                    .catalogMode(stricter)
                    .cqlFilterRead(cqlRead)
                    .cqlFilterWrite(cqlWrite)
                    .attributes(attributes)
                    .build();

            // skipping styles (only used in WMS)
        }

        return new WPSAccessInfo(ret, areaRet, clipRet);
    }
}
