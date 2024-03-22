/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.geoserver.acl.api.server.AuthorizationApiController;
import org.geoserver.acl.api.server.DataRulesApiController;
import org.geoserver.acl.api.server.WorkspaceAdminRulesApiController;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.context.request.NativeWebRequest;

class RulesApiAutoConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(RulesApiAutoConfiguration.class))
                    // expected dependencies:
                    .withBean(RuleRepository.class, () -> mock(RuleRepository.class))
                    .withBean(AdminRuleRepository.class, () -> mock(AdminRuleRepository.class))
                    .withBean(NativeWebRequest.class, () -> mock(NativeWebRequest.class));

    @Test
    void testConfiguration() {
        runner.run(
                context -> {
                    assertThat(context)
                            .hasNotFailed()
                            .hasSingleBean(DataRulesApiController.class)
                            .hasSingleBean(WorkspaceAdminRulesApiController.class)
                            .hasSingleBean(AuthorizationApiController.class);
                });
    }
}
