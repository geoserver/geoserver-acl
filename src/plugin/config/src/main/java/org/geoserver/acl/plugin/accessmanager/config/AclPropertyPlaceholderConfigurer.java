/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.config;

import java.io.IOException;
import java.util.Properties;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.GeoServerPropertyConfigurer;

public class AclPropertyPlaceholderConfigurer extends GeoServerPropertyConfigurer {

    public AclPropertyPlaceholderConfigurer(GeoServerDataDirectory data) {
        super(data);
    }

    public Properties getMergedProperties() throws IOException {
        return mergeProperties();
    }
}
