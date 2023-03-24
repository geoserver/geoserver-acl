package org.geoserver.acl.plugin.config.domain.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.api.client.integration.AuthorizationServiceClientAdaptor;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ApiClientAclDomainServicesConfigurationTest {

    ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withUserConfiguration(ApiClientAclDomainServicesConfiguration.class);

    @Test
    void testMissingConfig() {
        runner.run(
                context -> {
                    assertThat(context)
                            .hasFailed()
                            .getFailure()
                            .hasMessageContaining(
                                    "Authorization service target URL not provided through config property geoserver.acl.client.basePath");
                });
    }

    @Test
    void testConfiguredThroughConfigProperties() {
        runner.withPropertyValues(
                        "geoserver.acl.client.basePath=http://localhost:8181/acl/api",
                        "geoserver.acl.client.username=testme",
                        "geoserver.acl.client.password=s3cr3t")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(context).hasSingleBean(RuleAdminService.class);
                            assertThat(context).hasSingleBean(AdminRuleAdminService.class);
                            assertThat(context).hasSingleBean(AuthorizationService.class);
                            assertThat(context)
                                    .hasSingleBean(AuthorizationServiceClientAdaptor.class);
                        });
    }
}
