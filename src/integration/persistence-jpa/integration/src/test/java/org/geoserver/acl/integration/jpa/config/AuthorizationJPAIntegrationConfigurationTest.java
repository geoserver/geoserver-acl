package org.geoserver.acl.integration.jpa.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.integration.jpa.mapper.AdminRuleJpaMapper;
import org.geoserver.acl.integration.jpa.mapper.RuleJpaMapper;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AuthorizationJPAIntegrationConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withPropertyValues(
                            "geoserver.acl.datasource.url=jdbc:h2:mem:geoserver-acl-test")
                    .withUserConfiguration(
                            AuthorizationJPAPropertiesTestConfiguration.class,
                            AuthorizationJPAIntegrationConfiguration.class);

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
