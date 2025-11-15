/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.domain;

import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RuleAdminServiceConfiguration {

    @Bean
    RuleAdminService ruleAdminService(RuleRepository ruleRepository, ApplicationEventPublisher eventPublisher) {
        RuleAdminService service = new RuleAdminServiceImpl(ruleRepository);
        service.setEventPublisher(eventPublisher::publishEvent);
        return service;
    }
}
