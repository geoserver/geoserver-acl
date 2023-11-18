/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** {@link AutoConfiguration} redirect the home page to the swagger-ui */
@AutoConfiguration
public class SpringDocAutoConfiguration {

    @Bean
    SpringDocHomeRedirectController homeController() {
        return new SpringDocHomeRedirectController("/swagger-ui/index.html");
    }
}
