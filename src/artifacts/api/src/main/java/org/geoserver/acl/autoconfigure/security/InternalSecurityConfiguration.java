/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import static org.springframework.util.StringUtils.hasText;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@AutoConfiguration
@ConditionalOnInternalAuthenticationEnabled
@EnableConfigurationProperties(SecurityConfigProperties.class)
@Slf4j(topic = "org.geoserver.acl.autoconfigure.security")
public class InternalSecurityConfiguration {

    @Bean
    AuthenticationProvider internalAuthenticationProvider(
            @Qualifier("internalUserDetailsService")
                    UserDetailsService internalUserDetailsService) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setAuthoritiesMapper(new NullAuthoritiesMapper());
        provider.setUserDetailsService(internalUserDetailsService);

        DelegatingPasswordEncoder encoder =
                (DelegatingPasswordEncoder)
                        PasswordEncoderFactories.createDelegatingPasswordEncoder();

        provider.setPasswordEncoder(encoder);

        return provider;
    }

    @Bean("internalUserDetailsService")
    UserDetailsService internalUserDetailsService(SecurityConfigProperties config) {

        Map<String, SecurityConfigProperties.Internal.UserInfo> users =
                config.getInternal().getUsers();
        Collection<UserDetails> authUsers = new ArrayList<>();
        users.forEach(
                (username, userinfo) -> {
                    validate(username, userinfo);
                    log.info(
                            "Loading internal user {}, admin: {}, enabled: {}",
                            username,
                            userinfo.isAdmin(),
                            userinfo.isEnabled());
                    UserDetails user = toUserDetails(username, userinfo);
                    authUsers.add(user);
                });

        long enabledUsers = authUsers.stream().filter(UserDetails::isEnabled).count();
        if (0L == enabledUsers) {
            log.warn(
                    "No API users are enabled for HTTP Basic Auth. Loaded user names: {}",
                    users.keySet());
        }

        return new InMemoryUserDetailsManager(authUsers);
    }

    private UserDetails toUserDetails(
            String username, SecurityConfigProperties.Internal.UserInfo u) {
        return User.builder()
                .username(username)
                .password(u.getPassword())
                .authorities(u.authorities())
                .disabled(!u.isEnabled())
                .build();
    }

    private void validate(final String name, SecurityConfigProperties.Internal.UserInfo info) {
        if (info.isEnabled()) {
            if (!hasText(name)) throw new IllegalArgumentException("User has no name: " + info);
            if (!hasText(info.getPassword()))
                throw new IllegalArgumentException("User has no password " + name + ": " + info);
        }
    }
}
