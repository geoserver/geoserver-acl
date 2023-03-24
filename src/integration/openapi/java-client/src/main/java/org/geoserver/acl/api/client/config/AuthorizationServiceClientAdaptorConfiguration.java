/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.client.integration.AuthorizationServiceClientAdaptor;
import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.authorization.AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackageClasses = RuleApiMapper.class)
public class AuthorizationServiceClientAdaptorConfiguration {

    @Bean
    AuthorizationService aclAuthorizationServiceClientAdaptor(
            AuthorizationApi apiClient,
            AuthorizationModelApiMapper mapper,
            RuleApiMapper rulesMapper) {
        return new AuthorizationServiceClientAdaptor(apiClient, mapper, rulesMapper);
    }
}
