package org.geoserver.cloud.acl.config.springdoc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocHomeRedirectConfiguration {

    @Bean
    SpringDocHomeRedirectController homeController(
            @Value("${springdoc.swagger-ui.path}") String basePath) {
        return new SpringDocHomeRedirectController(basePath);
    }
}
