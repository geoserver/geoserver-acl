package org.geoserver.cloud.acl.autoconfigure.persistence;

import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties
@Import({AuthorizationJPAIntegrationConfiguration.class})
public class JPAIntegrationAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AuthorizationJPAProperties authorizationJPAProperties() {
        return new AuthorizationJPAProperties();
    }
}
