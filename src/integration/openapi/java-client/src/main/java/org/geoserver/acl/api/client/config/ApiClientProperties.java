/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import lombok.Data;

@Data
public class ApiClientProperties {

    private String basePath;
    private String username;
    private String password;
    private boolean caching = true;

    /** whether to check the connection at startup */
    private boolean startupCheck = true;

    /**
     * timeout in seconds for startup to fail if API is not available. Ignored if startupCheck=false
     */
    private int initTimeout = 5;
}
