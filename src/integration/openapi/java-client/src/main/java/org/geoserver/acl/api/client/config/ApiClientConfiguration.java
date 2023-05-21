/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Include this configuration to contribute an {@link org.geoserver.acl.api.client.ApiClient}
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class ApiClientConfiguration {

    @Bean
    AclClient aclClient(ApiClientProperties config) {

        String basePath = config.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            throw new IllegalStateException(
                    "Authorization service target URL not provided through config property geoserver.acl.client.basePath");
        }

        String username = config.getUsername();
        String password = config.getPassword();
        boolean debugging = config.isDebug();
        AclClient client = new AclClient();
        client.setBasePath(basePath);
        client.setUsername(username);
        client.setPassword(password);
        client.setLogRequests(debugging);

        return client;
    }

    @Bean
    AclClientAdaptor aclClientAdaptor(AclClient client) {
        return new AclClientAdaptor(client);
    }

    @Bean
    RuleRepository aclRuleRepositoryClientAdaptor(AclClientAdaptor adaptors) {
        return adaptors.getRuleRepository();
    }

    @Bean
    AdminRuleRepository aclAdminRuleRepositoryClientAdaptor(AclClientAdaptor adaptors) {
        return adaptors.getAdminRuleRepository();
    }

    @Bean
    AuthorizationService aclAuthorizationService(AclClientAdaptor adaptors) {
        return adaptors.getAuthorizationService();
    }
}
