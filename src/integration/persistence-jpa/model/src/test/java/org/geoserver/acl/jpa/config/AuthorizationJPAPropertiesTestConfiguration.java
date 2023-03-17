package org.geoserver.acl.jpa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class AuthorizationJPAPropertiesTestConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "geoserver.acl")
    AuthorizationJPAProperties authorizationJPAProperties() {
        return new AuthorizationJPAProperties();
    }
}
