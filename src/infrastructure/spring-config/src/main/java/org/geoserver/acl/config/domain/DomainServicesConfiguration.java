/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.domain;

import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contributes:
 * <ul>
 * <li>{@link RuleAdminService}
 * <li>{@link AdminRuleAdminService}
 * </ul>
 * Requires:
 * <ul>
 * <li>{@link RuleRepository}
 * <li>{@link AdminRuleRepository}
 * <li>{@link ApplicationEventPublisher}
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class DomainServicesConfiguration {

    @Bean
    RuleAdminService ruleAdminService(RuleRepository ruleRepository, ApplicationEventPublisher eventPublisher) {
        RuleAdminService service = new RuleAdminServiceImpl(ruleRepository);
        service.setEventPublisher(eventPublisher::publishEvent);
        return service;
    }

    @Bean
    AdminRuleAdminService adminRuleAdminService(
            AdminRuleRepository repository, ApplicationEventPublisher eventPublisher) {
        AdminRuleAdminService service = new AdminRuleAdminServiceImpl(repository);
        service.setEventPublisher(eventPublisher::publishEvent);
        return service;
    }
}
