/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

class InternalSecurityAutoConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(InternalSecurityAutoConfiguration.class));

    @Test
    void conditionalOnInternalAuthenticationEnabledIsDisabledByDefault() {
        runner.run(
                context ->
                        assertThat(context)
                                .hasNotFailed()
                                .doesNotHaveBean(AuthenticationProvider.class));
    }

    @Test
    void conditionalOnInternalAuthenticationEnabled() {
        runner.withPropertyValues("geoserver.acl.security.internal.enabled=true")
                .run(
                        context ->
                                assertThat(context)
                                        .hasNotFailed()
                                        .hasSingleBean(UserDetailsService.class)
                                        .hasSingleBean(PasswordEncoder.class)
                                        .hasSingleBean(AuthenticationProvider.class)
                                        .getBean(AuthenticationProvider.class)
                                        .isExactlyInstanceOf(DaoAuthenticationProvider.class));
    }

    @Test
    void testUsers() {
        runner.withPropertyValues(
                        "geoserver.acl.security.internal.enabled=true",
                        // define some users
                        // admin user
                        "geoserver.acl.security.internal.users.testadmin.enabled=true",
                        "geoserver.acl.security.internal.users.testadmin.admin=true",
                        "geoserver.acl.security.internal.users.testadmin.password={bcrypt}$2a$10$eMyaZRLZBAZdor8nOX.qwuwOyWazXjR2hddGLCT6f6c382WiwdQGG",
                        // non-admin user
                        "geoserver.acl.security.internal.users.testuser.enabled=true",
                        "geoserver.acl.security.internal.users.testuser.admin=false",
                        "geoserver.acl.security.internal.users.testuser.password={noop}changeme")
                .run(
                        context -> {
                            assertThat(context)
                                    .hasNotFailed()
                                    .hasSingleBean(UserDetailsService.class)
                                    .getBean(UserDetailsService.class)
                                    .isInstanceOf(InMemoryUserDetailsManager.class);

                            UserDetailsService service = context.getBean(UserDetailsService.class);
                            UserDetails admin = service.loadUserByUsername("testadmin");
                            assertThat(admin.getPassword())
                                    .isEqualTo(
                                            "{bcrypt}$2a$10$eMyaZRLZBAZdor8nOX.qwuwOyWazXjR2hddGLCT6f6c382WiwdQGG");
                            assertThat(admin.getAuthorities())
                                    .hasSize(1)
                                    .map(GrantedAuthority::getAuthority)
                                    .isEqualTo(List.of("ROLE_ADMIN"));

                            UserDetails user = service.loadUserByUsername("testuser");
                            assertThat(user.getPassword()).isEqualTo("{noop}changeme");
                            assertThat(user.getAuthorities())
                                    .hasSize(1)
                                    .map(GrantedAuthority::getAuthority)
                                    .isEqualTo(List.of("ROLE_USER"));
                        });
    }

    @Test
    void testFailureOnEnabledPasswordLessUser() {
        runner.withPropertyValues(
                        "geoserver.acl.security.internal.enabled=true",
                        // ill-defined passwsord-less user
                        "geoserver.acl.security.internal.users.baduser.enabled=true")
                .run(
                        context -> {
                            assertThat(context)
                                    .hasFailed()
                                    .getFailure()
                                    .hasMessageContaining("User baduser has no password");
                        });
    }

    @Test
    void testDisabledPasswordLessUserIgnored() {
        runner.withPropertyValues(
                        "geoserver.acl.security.internal.enabled=true",
                        // ill-defined but disabled passwsord-less user
                        "geoserver.acl.security.internal.users.baduser.enabled=false",
                        "geoserver.acl.security.internal.users.baduser.admin=true")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();

                            UserDetailsService service = context.getBean(UserDetailsService.class);

                            assertThrows(
                                    UsernameNotFoundException.class,
                                    () -> service.loadUserByUsername("baduser"));
                        });
    }

    @Test
    void testUsersPlainTextPasswordMappedAsNoop() {
        runner.withPropertyValues(
                        "geoserver.acl.security.internal.enabled=true",
                        "geoserver.acl.security.internal.users.testuser.enabled=true",
                        "geoserver.acl.security.internal.users.testuser.admin=false",
                        "geoserver.acl.security.internal.users.testuser.password=changeme")
                .run(
                        context -> {
                            UserDetailsService service = context.getBean(UserDetailsService.class);
                            UserDetails user = service.loadUserByUsername("testuser");
                            assertThat(user.getPassword())
                                    .as(
                                            """
                                    		plain text password should have been mapped as \
                                    		a '{noop}' prefixed literal
                                    		""")
                                    .isEqualTo("{noop}changeme");
                        });
    }
}
