/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.springdoc;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** {@link AutoConfiguration} redirect the home page to the swagger-ui */
@AutoConfiguration
public class SpringDocAutoConfiguration {

    @Bean
    SpringDocHomeRedirectController homeController(
            @Value("${server.servlet.context-path:}") String contextPath) {
        return new SpringDocHomeRedirectController("/swagger-ui/index.html");
    }

    @Bean
    public OpenApiCustomiser contextPathCustomiser(
            @Value("${server.servlet.context-path:/}") String contextPath) {
        return openApi -> {
            if (!"/".equals(contextPath)) {
                openApi.getServers().stream()
                        .forEach(
                                server -> {
                                    final String serverUrl = server.getUrl();
                                    if (!serverUrl.endsWith(contextPath)) {
                                        final String newServerUrl = serverUrl + contextPath;
                                        server.setUrl(newServerUrl);
                                    }
                                });
            }
        };
    }
}
