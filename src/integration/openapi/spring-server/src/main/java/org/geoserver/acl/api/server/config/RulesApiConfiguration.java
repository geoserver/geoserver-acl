/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.config;

import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.api.server.DataRulesApiController;
import org.geoserver.acl.api.server.DataRulesApiDelegate;
import org.geoserver.acl.api.server.WorkspaceAdminRulesApiController;
import org.geoserver.acl.api.server.WorkspaceAdminRulesApiDelegate;
import org.geoserver.acl.api.server.rules.DataRulesApiImpl;
import org.geoserver.acl.api.server.rules.WorkspaceAdminRulesApiImpl;
import org.geoserver.acl.api.server.support.DataRulesApiSupport;
import org.geoserver.acl.api.server.support.RequestBodyBufferingServletFilter;
import org.geoserver.acl.api.server.support.WorkspaceAdminRulesApiSupport;
import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.AuthorizationServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.NativeWebRequest;

@Configuration(proxyBeanMethods = false)
@Import({
    ApiObjectModelMappersConfiguration.class,
    JacksonObjectMapperConfiguration.class,
    RuleAdminServiceConfiguration.class,
    AdminRuleAdminServiceConfiguration.class,
    AuthorizationServiceConfiguration.class
})
public class RulesApiConfiguration {

    @Bean
    RequestBodyBufferingServletFilter patchBufferingFilter() {
        return new RequestBodyBufferingServletFilter();
    }

    @Bean
    DataRulesApiController rulesApiController(DataRulesApiDelegate delegate) {
        return new DataRulesApiController(delegate);
    }

    @Bean
    WorkspaceAdminRulesApiController adminRulesApiController(
            WorkspaceAdminRulesApiDelegate delegate) {
        return new WorkspaceAdminRulesApiController(delegate);
    }

    @Bean
    DataRulesApiDelegate rulesApiDelegate(RuleAdminService rules, DataRulesApiSupport support) {
        return new DataRulesApiImpl(rules, support);
    }

    @Bean
    DataRulesApiSupport rulesApiImplSupport(
            NativeWebRequest nativeReq,
            RuleApiMapper mapper,
            LayerDetailsApiMapper layerDetailsMapper,
            RuleLimitsApiMapper limitsMapper) {

        return new DataRulesApiSupport(nativeReq, mapper, layerDetailsMapper, limitsMapper);
    }

    @Bean
    WorkspaceAdminRulesApiDelegate adminRulesApiDelegate(
            AdminRuleAdminService service, WorkspaceAdminRulesApiSupport support) {
        return new WorkspaceAdminRulesApiImpl(service, support);
    }

    @Bean
    WorkspaceAdminRulesApiSupport adminRulesApiImplSupport(
            NativeWebRequest nativeReq, AdminRuleApiMapper mapper) {

        return new WorkspaceAdminRulesApiSupport(nativeReq, mapper);
    }
}
