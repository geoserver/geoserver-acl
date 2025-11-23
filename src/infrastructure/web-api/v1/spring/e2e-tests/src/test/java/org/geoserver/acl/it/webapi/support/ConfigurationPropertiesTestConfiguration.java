/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.it.webapi.support;

import org.geoserver.acl.config.persistence.jpa.AclJpaProperties;
import org.geoserver.acl.config.webapi.client.ApiClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ConfigurationPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AclJpaProperties authorizationJPAProperties() {
        return new AclJpaProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl.client")
    ApiClientProperties apiClientProperties() {
        return new ApiClientProperties();
    }
}
