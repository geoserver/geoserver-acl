/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.application;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contributes:
 * <ul>
 * <li>{@link AuthorizationService}
 * </ul>
 * Requires:
 * <ul>
 * <li>{@link AdminRuleAdminService}
 * <li>{@link RuleAdminService}
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class ApplicationServicesConfiguration {

    @Bean
    AuthorizationService aclAuthorizationService(AdminRuleAdminService adminRuleService, RuleAdminService ruleService) {
        return new AuthorizationServiceImpl(adminRuleService, ruleService);
    }
}
