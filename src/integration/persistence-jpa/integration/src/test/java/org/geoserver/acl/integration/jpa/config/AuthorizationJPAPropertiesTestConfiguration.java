/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.config;

import org.geoserver.acl.integration.jpa.it.RuleEventCollector;
import org.geoserver.acl.jpa.config.AclJpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class AuthorizationJPAPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AclJpaProperties authorizationJPAProperties() {
        return new AclJpaProperties();
    }

    @Bean
    RuleEventCollector ruleEventCollector() {
        return new RuleEventCollector();
    }
}
