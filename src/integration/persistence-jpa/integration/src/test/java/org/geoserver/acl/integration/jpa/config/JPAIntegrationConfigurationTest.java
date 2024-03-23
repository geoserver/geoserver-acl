/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper;
import org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JPAIntegrationConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withPropertyValues(
                            "geoserver.acl.datasource.url=jdbc:h2:mem:geoserver-acl-test")
                    .withUserConfiguration(
                            AuthorizationJPAPropertiesTestConfiguration.class,
                            JPAIntegrationConfiguration.class);

    @Test
    void testAuthorizationRuleRepositoryJpaAdaptor() {

        runner.run(
                context -> {
                    assertThat(context)
                            .hasNotFailed()
                            .hasSingleBean(RuleRepository.class)
                            .hasSingleBean(AdminRuleRepository.class)
                            .hasSingleBean(RuleJpaMapper.class)
                            .hasSingleBean(AdminRuleJpaMapper.class);
                });
    }
}
