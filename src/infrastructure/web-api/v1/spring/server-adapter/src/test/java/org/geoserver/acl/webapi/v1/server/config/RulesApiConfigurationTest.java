/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.webapi.v1.server.DataRulesApiController;
import org.geoserver.acl.webapi.v1.server.DataRulesApiDelegate;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiController;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.context.request.NativeWebRequest;

class RulesApiConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(UserConfigurations.of(RulesApiConfiguration.class));

    private ApplicationContextRunner withMockRepositories() {
        runner = withMock(NativeWebRequest.class);
        runner = withMock(RuleRepository.class);
        runner = withMock(AdminRuleRepository.class);
        return runner;
    }

    private <T> ApplicationContextRunner withMock(Class<T> beanType) {
        return runner.withBean(beanType, () -> mock(beanType));
    }

    @Test
    void testWithAvailableRepositories() {
        withMockRepositories().run(context -> {
            assertThat(context)
                    .hasSingleBean(DataRulesApiController.class)
                    .hasSingleBean(DataRulesApiDelegate.class)
                    .hasSingleBean(RuleApiMapper.class)
                    .hasSingleBean(WorkspaceAdminRulesApiController.class)
                    .hasSingleBean(WorkspaceAdminRulesApiDelegate.class)
                    .hasSingleBean(AdminRuleApiMapper.class)
                    .hasSingleBean(JavaTimeModule.class)
                    .hasSingleBean(EnumsApiMapper.class);
        });
    }

    @Test
    void testMissingRuleRepository() {
        runner = withMock(AdminRuleRepository.class);
        runner.run(context -> {
            assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .isInstanceOf(UnsatisfiedDependencyException.class)
                    .hasMessageContaining("RuleRepository");
        });
    }

    @Test
    void testMissingAdminRuleRepository() {
        runner = withMock(RuleRepository.class);
        runner.run(context -> {
            assertThat(context)
                    .hasFailed()
                    .getFailure()
                    .isInstanceOf(UnsatisfiedDependencyException.class)
                    .hasMessageContaining("AdminRuleRepository");
        });
    }
}
