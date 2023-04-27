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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
    public UserDetailsService internalUserDetailsService(SecurityConfigProperties config) {

        List<SecurityConfigProperties.Internal.User> users = config.getInternal().getUsers();
        Collection<UserDetails> authUsers =
                users.stream()
                        .peek(this::validate)
                        .peek(
                                u ->
                                        log.info(
                                                "Loading internal user {}, admin: {}",
                                                u.getName(),
                                                u.isAdmin()))
                        .map(this::toUserDetails)
                        .collect(Collectors.toList());

        return new InMemoryUserDetailsManager(authUsers);
    }

    private UserDetails toUserDetails(SecurityConfigProperties.Internal.User u) {
        return User.builder()
                .username(u.getName())
                .password(u.getPassword())
                .authorities(u.authorities())
                .disabled(!u.isEnabled())
                .build();
    }

    private void validate(SecurityConfigProperties.Internal.User user) {
        if (user.isEnabled()) {
            if (!hasText(user.getName()))
                throw new IllegalArgumentException("User has no name: " + user);
            if (!hasText(user.getPassword()))
                throw new IllegalArgumentException("User has no password: " + user);
        }
    }
}
