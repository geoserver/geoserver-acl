/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.webapi.v1.server;

import org.geoserver.acl.config.application.ApplicationServicesConfiguration;
import org.geoserver.acl.config.domain.DomainServicesConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.webapi.v1.mapper.AdminRuleApiMapper;
import org.geoserver.acl.webapi.v1.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.webapi.v1.server.DataRulesApiController;
import org.geoserver.acl.webapi.v1.server.DataRulesApiDelegate;
import org.geoserver.acl.webapi.v1.server.DataRulesApiDelegateImpl;
import org.geoserver.acl.webapi.v1.server.DataRulesApiSupport;
import org.geoserver.acl.webapi.v1.server.RequestBodyBufferingServletFilter;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiController;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiDelegate;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiDelegateImpl;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Provides the {@link DataRulesApiController} and {@link WorkspaceAdminRulesApiController}
 */
@Configuration(proxyBeanMethods = false)
@Import({
    ModelMappersConfiguration.class,
    ObjectMapperConfiguration.class,
    DomainServicesConfiguration.class,
    ApplicationServicesConfiguration.class
})
class WebapiServerDomainServicesConfiguration {

    @Bean
    RequestBodyBufferingServletFilter patchBufferingFilter() {
        return new RequestBodyBufferingServletFilter();
    }

    @Bean
    DataRulesApiController rulesApiController(DataRulesApiDelegate delegate) {
        return new DataRulesApiController(delegate);
    }

    @Bean
    WorkspaceAdminRulesApiController adminRulesApiController(WorkspaceAdminRulesApiDelegate delegate) {
        return new WorkspaceAdminRulesApiController(delegate);
    }

    @Bean
    DataRulesApiDelegate rulesApiDelegate(RuleAdminService rules, DataRulesApiSupport support) {
        return new DataRulesApiDelegateImpl(rules, support);
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
        return new WorkspaceAdminRulesApiDelegateImpl(service, support);
    }

    @Bean
    WorkspaceAdminRulesApiSupport adminRulesApiImplSupport(NativeWebRequest nativeReq, AdminRuleApiMapper mapper) {

        return new WorkspaceAdminRulesApiSupport(nativeReq, mapper);
    }
}
