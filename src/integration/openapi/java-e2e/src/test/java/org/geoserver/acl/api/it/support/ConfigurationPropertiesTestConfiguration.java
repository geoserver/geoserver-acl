/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.support;

import org.geoserver.acl.api.client.config.ApiClientProperties;
import org.geoserver.acl.jpa.config.AuthorizationJPAProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ConfigurationPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AuthorizationJPAProperties authorizationJPAProperties() {
        return new AuthorizationJPAProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl.client")
    ApiClientProperties apiClientProperties() {
        return new ApiClientProperties();
    }
}
