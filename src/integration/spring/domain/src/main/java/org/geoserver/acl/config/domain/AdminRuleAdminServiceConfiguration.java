/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.domain;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class AdminRuleAdminServiceConfiguration {

    @Bean
    public AdminRuleAdminService adminRuleAdminService(
            AdminRuleRepository repository, ApplicationEventPublisher eventPublisher) {
        AdminRuleAdminService service = new AdminRuleAdminService(repository);
        service.setEventPublisher(eventPublisher::publishEvent);
        return service;
    }
}
