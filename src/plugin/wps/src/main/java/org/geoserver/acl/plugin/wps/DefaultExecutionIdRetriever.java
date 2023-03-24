/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.wps;

import org.geoserver.acl.plugin.accessmanager.wps.ExecutionIdRetriever;
import org.geoserver.wps.resource.WPSResourceManager;

/**
 * @author etj (Emanuele Tajariol @ GeoSolutions) Originally as part of GeoFence's GeoServer
 *     extension
 */
public class DefaultExecutionIdRetriever implements ExecutionIdRetriever {

    private WPSResourceManager wpsManager;

    public DefaultExecutionIdRetriever(WPSResourceManager wpsManager) {
        this.wpsManager = wpsManager;
    }

    /** Returns the executionId bound to this thread, if any */
    @Override
    public String getCurrentExecutionId() {
        return wpsManager.getCurrentExecutionId();
    }
}
