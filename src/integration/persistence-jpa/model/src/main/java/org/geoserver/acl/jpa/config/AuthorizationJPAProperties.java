/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.config;

import com.zaxxer.hikari.HikariConfig;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuthorizationJPAProperties {

    private DataSourceProperties datasource = new DataSourceProperties();
    private JpaProperties jpa = new JpaProperties();

    @Data
    public static class DataSourceProperties {
        private String jndiName;
        private String url;
        private String username;
        private String password;
        private HikariConfig hikari = new HikariConfig();
    }

    @Data
    public static class JpaProperties {
        private boolean showSql;
        private boolean generateDdl;
        private String databasePlatform;
        private Map<String, String> properties = new HashMap<>();
    }
}
