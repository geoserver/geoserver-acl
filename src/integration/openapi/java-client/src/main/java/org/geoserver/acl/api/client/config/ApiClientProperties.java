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
    private boolean debug;
}
