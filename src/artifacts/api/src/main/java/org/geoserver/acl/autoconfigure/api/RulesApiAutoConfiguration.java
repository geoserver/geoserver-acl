/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.api;

import org.geoserver.acl.api.server.config.AuthorizationApiConfiguration;
import org.geoserver.acl.api.server.config.RulesApiConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@AutoConfiguration
@Import({RulesApiConfiguration.class, AuthorizationApiConfiguration.class})
public class RulesApiAutoConfiguration {

    @Bean
    CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        var filter = new CommonsRequestLoggingFilter();
        filter.setHeaderPredicate(h -> h.toLowerCase().startsWith("x-forward"));
        filter.setIncludeHeaders(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeClientInfo(true);
        return filter;
    }
}
