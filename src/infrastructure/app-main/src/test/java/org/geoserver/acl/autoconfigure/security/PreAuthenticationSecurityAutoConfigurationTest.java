/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.geoserver.acl.autoconfigure.security.SecurityConfigProperties.PreauthHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

class PreAuthenticationSecurityAutoConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AuthenticationManagerAutoConfiguration.class, PreAuthenticationSecurityAutoConfiguration.class));

    @BeforeEach
    void setUp() throws Exception {}

    @Test
    void testConditionalOnPreAuthenticationEnabledIsDisabledByDefault() {
        // contribute a mocked up AuthenticationProvider for AuthenticationManagerAutoConfiguration
        // not to fail due to at least one AuthenticationProvider existing
        AuthenticationProvider mockProvider = mock(AuthenticationProvider.class);
        runner.withBean(AuthenticationProvider.class, () -> mockProvider).run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean(SecurityConfigProperties.class)
                .doesNotHaveBean(RequestHeaderAuthenticationFilter.class)
                .doesNotHaveBean(PreAuthenticatedAuthenticationProvider.class));
    }

    @Test
    void testConditionalOnPreAuthenticationEnabled() {
        runner.withPropertyValues("geoserver.acl.security.headers.enabled=true").run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(SecurityConfigProperties.class)
                .hasSingleBean(RequestHeaderAuthenticationFilter.class)
                .hasSingleBean(PreAuthenticatedAuthenticationProvider.class));
    }

    @Test
    void testPreAuthenticationConfig() {
        runner.withPropertyValues(
                        "geoserver.acl.security.headers.enabled: true",
                        "geoserver.acl.security.headers.user-header: sec-username",
                        "geoserver.acl.security.headers.roles-header: sec-roles",
                        "geoserver.acl.security.headers.admin-roles: ROLE_ADMINISTRATOR,ADMIN")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PreauthHeaders config =
                            context.getBean(SecurityConfigProperties.class).getHeaders();
                    assertThat(config.getUserHeader()).isEqualTo("sec-username");
                    assertThat(config.getRolesHeader()).isEqualTo("sec-roles");
                    assertThat(config.getAdminRoles()).containsExactly("ROLE_ADMINISTRATOR", "ADMIN");
                });
    }
}
