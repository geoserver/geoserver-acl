/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.api.client.AdminRulesApi;
import org.geoserver.acl.api.client.RulesApi;
import org.geoserver.acl.api.client.integration.AdminRuleRepositoryClientAdaptor;
import org.geoserver.acl.api.client.integration.RuleRepositoryClientAdaptor;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.rules.RuleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackageClasses = RuleApiMapper.class)
public class RepositoryClientAdaptorsConfiguration {

    @Bean
    RuleRepository authorizationRepositoryClientAdaptor(
            RulesApi apiClient,
            RuleApiMapper mapper,
            EnumsApiMapper enumsMapper,
            RuleLimitsApiMapper limitsMapper,
            LayerDetailsApiMapper detailsMapper) {

        return new RuleRepositoryClientAdaptor(
                apiClient, mapper, enumsMapper, limitsMapper, detailsMapper);
    }

    @Bean
    AdminRuleRepository authorizationAdminRuleRepositoryClientAdaptor(
            AdminRulesApi apiClient, AdminRuleApiMapper mapper, EnumsApiMapper enumsMapper) {
        return new AdminRuleRepositoryClientAdaptor(apiClient, mapper, enumsMapper);
    }
}
