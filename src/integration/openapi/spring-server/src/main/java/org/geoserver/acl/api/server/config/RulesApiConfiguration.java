/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.config;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.api.server.AdminRulesApiController;
import org.geoserver.acl.api.server.AdminRulesApiDelegate;
import org.geoserver.acl.api.server.RulesApiController;
import org.geoserver.acl.api.server.RulesApiDelegate;
import org.geoserver.acl.api.server.rules.AdminRulesApiImpl;
import org.geoserver.acl.api.server.rules.RulesApiImpl;
import org.geoserver.acl.api.server.support.AdminRulesApiSupport;
import org.geoserver.acl.api.server.support.RequestBodyBufferingServletFilter;
import org.geoserver.acl.api.server.support.RulesApiSupport;
import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.AuthorizationServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.geoserver.acl.rules.RuleAdminService;
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
    RulesApiController rulesApiController(RulesApiDelegate delegate) {
        return new RulesApiController(delegate);
    }

    @Bean
    AdminRulesApiController adminRulesApiController(AdminRulesApiDelegate delegate) {
        return new AdminRulesApiController(delegate);
    }

    @Bean
    RulesApiDelegate rulesApiDelegate(RuleAdminService rules, RulesApiSupport support) {
        return new RulesApiImpl(rules, support);
    }

    @Bean
    RulesApiSupport rulesApiImplSupport(
            NativeWebRequest nativeReq,
            RuleApiMapper mapper,
            LayerDetailsApiMapper layerDetailsMapper,
            RuleLimitsApiMapper limitsMapper) {

        return new RulesApiSupport(nativeReq, mapper, layerDetailsMapper, limitsMapper);
    }

    @Bean
    AdminRulesApiDelegate adminRulesApiDelegate(
            AdminRuleAdminService service, AdminRulesApiSupport support) {
        return new AdminRulesApiImpl(service, support);
    }

    @Bean
    AdminRulesApiSupport adminRulesApiImplSupport(
            NativeWebRequest nativeReq, AdminRuleApiMapper mapper) {

        return new AdminRulesApiSupport(nativeReq, mapper);
    }
}
