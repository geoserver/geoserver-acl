/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnPreAuthenticationEnabled
@EnableConfigurationProperties(SecurityConfigProperties.class)
public class PreAuthenticationSecurityAutoConfiguration {

    @Bean
    RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter(
            AuthenticationManager authenticationManager, SecurityConfigProperties config)
            throws Exception {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();

        String userHeader = config.getHeaders().getUserHeader();
        String rolesHeader = config.getHeaders().getRolesHeader();
        if (!StringUtils.hasText(userHeader) || !StringUtils.hasText(rolesHeader)) {
            throw new IllegalStateException(
                    "Both user and roles header names must be provided, got geoserver.acl.security.headers.userHeader: "
                            + userHeader
                            + ", geoserver.acl.security.headers.rolesHeader: "
                            + rolesHeader);
        }

        filter.setPrincipalRequestHeader(userHeader);
        filter.setCredentialsRequestHeader(rolesHeader);

        filter.setAuthenticationManager(authenticationManager);
        // do not throw exception when header is not present.
        // one use case is for actuator endpoints and static assets where security
        // headers are not required.
        filter.setExceptionIfHeaderMissing(false);
        return filter;
    }

    @Bean
    PreAuthenticatedAuthenticationProvider preauthAuthProvider(SecurityConfigProperties config)
            throws Exception {
        PreAuthenticatedAuthenticationProvider provider =
                new PreAuthenticatedAuthenticationProvider();
        Supplier<Collection<String>> adminRoles = config.getHeaders()::getAdminRoles;
        AuthorizationUserDetailsService detailsService =
                new AuthorizationUserDetailsService(adminRoles);
        provider.setPreAuthenticatedUserDetailsService(detailsService);
        return provider;
    }

    @RequiredArgsConstructor
    static class AuthorizationUserDetailsService
            implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

        private final @NonNull Supplier<Collection<String>> adminRoles;

        /**
         * Loads user from data store and creates UserDetails object based on principal and/or
         * credential.
         *
         * <p>Role name needs to have "ROLE_" prefix.
         *
         * @param token instance of PreAuthenticatedAuthenticationToken
         * @return UserDetails object which contains role information for the given user.
         * @throws UsernameNotFoundException
         */
        @Override
        public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
                throws UsernameNotFoundException {
            final String principal = (String) token.getPrincipal();
            final Set<String> credentials = givenCredentials((String) token.getCredentials());
            Collection<String> rolesConsideredAdmin = adminRoles.get();
            boolean isAdmin = rolesConsideredAdmin.stream().anyMatch(credentials::contains);
            if (isAdmin) {
                credentials.add("ROLE_ADMIN");
            } else {
                credentials.add("ROLE_USER");
            }

            List<SimpleGrantedAuthority> authorities =
                    credentials.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            return new User(principal, "", authorities);
        }

        private Set<String> givenCredentials(String rolesHeader) {
            if (StringUtils.hasText(rolesHeader)) {
                return Arrays.stream(rolesHeader.split(";"))
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .collect(Collectors.toSet());
            }
            return Set.of();
        }
    }
}
