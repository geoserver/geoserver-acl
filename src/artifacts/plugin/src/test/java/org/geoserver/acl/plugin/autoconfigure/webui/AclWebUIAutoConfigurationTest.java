/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.autoconfigure.webui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.geoserver.web.Category;
import org.geoserver.web.GeoServerBasePage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** {@link AclWebUIAutoConfiguration} tests */
class AclWebUIAutoConfigurationTest {

    private Category securityCategory = mock(Category.class);

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(AclWebUIAutoConfiguration.class));

    @Test
    void testEnabledWhenAllConditionsMatch() {
        runner.withBean("securityCategory", Category.class, () -> securityCategory)
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .hasBean("aclServiceConfigPageMenuInfo")
                                    .hasBean("accessRulesACLPageMenuInfo")
                                    .hasBean("adminRulesAclPageMenuInfo");
                        });
    }

    @Test
    void testConditionalOnGeoServerBasePage() {
        runner.withBean("securityCategory", Category.class, () -> securityCategory)
                .withClassLoader(new FilteredClassLoader(GeoServerBasePage.class))
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .doesNotHaveBean("aclServiceConfigPageMenuInfo")
                                    .doesNotHaveBean("accessRulesACLPageMenuInfo")
                                    .doesNotHaveBean("adminRulesAclPageMenuInfo");
                        });
    }

    @Test
    void testConditionalOnAclEnabled() {
        runner.withBean("securityCategory", Category.class, () -> securityCategory)
                .withPropertyValues("geoserver.acl.enabled=false")
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .doesNotHaveBean("aclServiceConfigPageMenuInfo")
                                    .doesNotHaveBean("accessRulesACLPageMenuInfo")
                                    .doesNotHaveBean("adminRulesAclPageMenuInfo");
                        });
    }

    @Test
    void testConditionalOnProperty() {
        runner.withBean("securityCategory", Category.class, () -> securityCategory)
                .withPropertyValues(
                        "geoserver.acl.enabled=true", "geoserver.web-ui.acl.enabled=false")
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .doesNotHaveBean("aclServiceConfigPageMenuInfo")
                                    .doesNotHaveBean("accessRulesACLPageMenuInfo")
                                    .doesNotHaveBean("adminRulesAclPageMenuInfo");
                        });
    }

    @Test
    void testConditionalOnBean_securityCategory() {
        runner.run(
                context -> {
                    assertThat(context)
                            .hasNotFailed()
                            .doesNotHaveBean("securityCategory")
                            .doesNotHaveBean("aclServiceConfigPageMenuInfo")
                            .doesNotHaveBean("accessRulesACLPageMenuInfo")
                            .doesNotHaveBean("adminRulesAclPageMenuInfo");
                });
    }
}
