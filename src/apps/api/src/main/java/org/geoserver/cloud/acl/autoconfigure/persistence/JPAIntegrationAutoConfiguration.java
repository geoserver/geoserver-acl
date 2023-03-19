/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.cloud.acl.autoconfigure.persistence;

import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties
@Import({JPAIntegrationConfiguration.class})
public class JPAIntegrationAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AuthorizationJPAProperties authorizationJPAProperties() {
        return new AuthorizationJPAProperties();
    }
}
