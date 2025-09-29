/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.config;

import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper;
import org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper;
import org.geoserver.acl.integration.jpa.repository.AdminRuleRepositoryJpaAdaptor;
import org.geoserver.acl.integration.jpa.repository.RuleRepositoryJpaAdaptor;
import org.geoserver.acl.jpa.config.AclDataSourceConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAConfiguration;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({AclDataSourceConfiguration.class, AuthorizationJPAConfiguration.class})
@ComponentScan(basePackageClasses = {RuleJpaMapper.class, AdminRuleJpaMapper.class})
public class JPAIntegrationConfiguration {

    @Bean
    RuleRepository aclRuleRepositoryJpaAdaptor(
            EntityManager em,
            JpaRuleRepository jpaRuleRepository,
            RuleJpaMapper modelMapper,
            ApplicationEventPublisher eventPublisher) {

        RuleRepositoryJpaAdaptor adaptor = new RuleRepositoryJpaAdaptor(em, jpaRuleRepository, modelMapper);
        Consumer<RuleEvent> publisher = eventPublisher::publishEvent;
        adaptor.setEventPublisher(publisher);
        return adaptor;
    }

    @Bean
    AdminRuleRepository aclAdminRuleRepositoryJpaAdaptor(
            EntityManager em,
            JpaAdminRuleRepository jpaAdminRuleRepo,
            AdminRuleJpaMapper modelMapper,
            ApplicationEventPublisher eventPublisher) {

        AdminRuleRepositoryJpaAdaptor adaptor = new AdminRuleRepositoryJpaAdaptor(em, jpaAdminRuleRepo, modelMapper);
        Consumer<AdminRuleEvent> publisher = eventPublisher::publishEvent;
        adaptor.setEventPublisher(publisher);
        return adaptor;
    }
}
