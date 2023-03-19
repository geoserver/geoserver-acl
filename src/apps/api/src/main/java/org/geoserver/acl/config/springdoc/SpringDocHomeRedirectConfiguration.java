/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.springdoc;

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
