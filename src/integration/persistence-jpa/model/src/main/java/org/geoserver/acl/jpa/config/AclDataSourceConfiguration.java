/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.geoserver.acl.jpa.config.AclJpaProperties.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
public class AclDataSourceConfiguration {

    /**
     * E.g.:
     *
     * <pre>{@code
     * geoserver.acl:
     *   datasource:
     *     url: jdbc:h2:mem:geoserver-acl
     *     username: sa
     *     password: sa
     *     hikari:
     *       minimum-idle: 0
     *       maximum-pool-size: 20
     * }</pre>
     *
     * Or:
     *
     * <pre>{@code
     * geoserver.acl.datasource.jndiName: java:comp/env/jdbc/gsuath
     * }</pre>
     */
    @Primary
    @Bean("authorizationDataSource")
    DataSource authorizationDataSource(AclJpaProperties props) {
        DataSourceProperties dsprops = props.getDatasource();
        final String jndiName = dsprops.getJndiName();
        if (StringUtils.hasText(jndiName)) {
            return new JndiDataSourceLookup().getDataSource(jndiName);
        }

        String url = dsprops.getUrl();
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException(
                    "geoserver.acl.datasource.url or geoserver.acl.datasource.jndiName is requried");
        }
        String username = dsprops.getUsername();
        String pwd = dsprops.getPassword();
        HikariConfig hikariConfig = dsprops.getHikari();
        if (null == hikariConfig.getJdbcUrl()) hikariConfig.setJdbcUrl(url);
        if (null == hikariConfig.getUsername()) hikariConfig.setUsername(username);
        if (null == hikariConfig.getPassword()) hikariConfig.setPassword(pwd);
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        return ds;
    }
}
