/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.webapi.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.webapi.v1.server.AuthorizationApiController;
import org.geoserver.acl.webapi.v1.server.DataRulesApiController;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.context.request.NativeWebRequest;

class GeoServerAclApiServerAutoConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GeoServerAclWebApiServerAutoConfiguration.class))
            // expected dependencies:
            .withBean(RuleRepository.class, () -> mock(RuleRepository.class))
            .withBean(AdminRuleRepository.class, () -> mock(AdminRuleRepository.class))
            .withBean(NativeWebRequest.class, () -> mock(NativeWebRequest.class));

    @Test
    void testConfiguration() {
        runner.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(DataRulesApiController.class)
                    .hasSingleBean(WorkspaceAdminRulesApiController.class)
                    .hasSingleBean(AuthorizationApiController.class);
        });
    }
}
