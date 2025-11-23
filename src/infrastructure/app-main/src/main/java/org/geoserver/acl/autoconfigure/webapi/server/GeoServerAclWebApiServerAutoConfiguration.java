/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.webapi.server;

import org.geoserver.acl.config.webapi.v1.server.ApiServerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@AutoConfiguration
@Import(ApiServerConfiguration.class)
public class GeoServerAclWebApiServerAutoConfiguration {

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
