/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.config;

import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper;
import org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper;
import org.geoserver.acl.integration.jpa.repository.AdminRuleRepositoryJpaAdaptor;
import org.geoserver.acl.integration.jpa.repository.RuleRepositoryJpaAdaptor;
import org.geoserver.acl.jpa.config.AuthorizationDataSourceConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAConfiguration;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.rules.RuleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;

@Configuration(proxyBeanMethods = false)
@Import({AuthorizationDataSourceConfiguration.class, AuthorizationJPAConfiguration.class})
@ComponentScan(basePackageClasses = {RuleJpaMapper.class, AdminRuleJpaMapper.class})
public class AuthorizationJPAIntegrationConfiguration {

    @Bean
    public RuleRepository authorizationRuleRepositoryJpaAdaptor(
            EntityManager em, JpaRuleRepository jpaRuleRepository, RuleJpaMapper modelMapper) {

        return new RuleRepositoryJpaAdaptor(em, jpaRuleRepository, modelMapper);
    }

    @Bean
    public AdminRuleRepository authorizationAdminRuleRepositoryJpaAdaptor(
            EntityManager em,
            JpaAdminRuleRepository jpaAdminRuleRepo,
            AdminRuleJpaMapper modelMapper) {
        return new AdminRuleRepositoryJpaAdaptor(em, jpaAdminRuleRepo, modelMapper);
    }
}
