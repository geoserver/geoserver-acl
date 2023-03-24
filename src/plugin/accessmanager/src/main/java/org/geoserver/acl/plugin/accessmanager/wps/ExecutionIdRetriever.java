/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.wps;

/**
 * @author etj (Emanuele Tajariol @ GeoSolutions) - Originally as part of GeoFence's GeoServer
 *     extension
 */
public interface ExecutionIdRetriever {

    String getCurrentExecutionId();
}
