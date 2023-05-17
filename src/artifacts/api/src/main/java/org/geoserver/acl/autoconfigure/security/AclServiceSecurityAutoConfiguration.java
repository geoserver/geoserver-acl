/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityConfigProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j(topic = "org.geoserver.acl.autoconfigure.security")
public class AclServiceSecurityAutoConfiguration {

    private @Autowired(required = false) RequestHeaderAuthenticationFilter preAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            SecurityConfigProperties config)
            throws Exception {

        http.csrf().disable();

        if (!config.enabled()) {
            log.warn("No security authentication method is defined!");
            return http.build();
        }

        http.authenticationManager(authenticationManager);

        if (null == preAuthFilter) {
            log.info("Pre-authentication headers disabled");
        } else {
            log.info(
                    "Pre-authentication headers enabled for {}/{}. Admin roles: {}",
                    config.getHeaders().getUserHeader(),
                    config.getHeaders().getRolesHeader(),
                    config.getHeaders().getAdminRoles());
            http.addFilterAfter(preAuthFilter, RequestHeaderAuthenticationFilter.class);
        }

        http.authorizeRequests()
                .antMatchers("/actuator/health/**")
                .permitAll()
                .antMatchers("/actuator/**")
                .hasAuthority("ROLE_ADMIN")
                .antMatchers("/", "/api/api-docs/**", "/api/swagger-ui.html", "/api/swagger-ui/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and();

        if (config.getInternal().isEnabled()) {
            http.httpBasic();
        }
        return http.build();
    }
}
