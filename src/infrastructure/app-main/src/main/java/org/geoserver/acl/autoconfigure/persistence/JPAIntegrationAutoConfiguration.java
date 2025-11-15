/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.persistence;

import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.geoserver.acl.jpa.config.AclJpaProperties;
import org.geoserver.cloud.config.jndidatasource.JNDIDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties
@Import({JNDIDataSourceAutoConfiguration.class, JPAIntegrationConfiguration.class})
public class JPAIntegrationAutoConfiguration {

    @Bean
    @DependsOn("configuredJndiDataSources")
    @ConfigurationProperties(prefix = "geoserver.acl")
    AclJpaProperties authorizationJPAProperties() {
        return new AclJpaProperties();
    }
}
