/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.domain;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServiceConfiguration {

    @Bean
    AuthorizationService aclAuthorizationService(
            AdminRuleAdminService adminRuleService, RuleAdminService ruleService) {
        return new AuthorizationServiceImpl(adminRuleService, ruleService);
    }
}
