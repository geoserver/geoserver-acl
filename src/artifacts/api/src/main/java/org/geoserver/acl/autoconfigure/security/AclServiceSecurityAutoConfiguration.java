/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@AutoConfiguration
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityConfigProperties.class)
@EnableMethodSecurity
@Slf4j(topic = "org.geoserver.acl.autoconfigure.security")
public class AclServiceSecurityAutoConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            SecurityConfigProperties config,
            Optional<RequestHeaderAuthenticationFilter> preAuthFilter)
            throws Exception {

        http.csrf(csrf -> csrf.disable());

        if (!config.enabled()) {
            log.warn("No security authentication method is defined!");
            return http.build();
        }

        http.authenticationManager(authenticationManager);

        if (preAuthFilter.isPresent()) {
            RequestHeaderAuthenticationFilter preAuth = preAuthFilter.orElseThrow();
            log.info(
                    "Pre-authentication headers enabled for {}/{}. Admin roles: {}",
                    config.getHeaders().getUserHeader(),
                    config.getHeaders().getRolesHeader(),
                    config.getHeaders().getAdminRoles());
            http = http.addFilterAfter(preAuth, RequestHeaderAuthenticationFilter.class);
        } else {
            log.info("Pre-authentication headers disabled");
        }

        http = http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (config.getInternal().isEnabled()) {
            http = http.httpBasic(withDefaults());
        }

        http.authorizeHttpRequests(requests -> requests.requestMatchers("/actuator/health/**")
                .permitAll()
                .requestMatchers("/actuator/**")
                .hasAuthority("ROLE_ADMIN")
                .requestMatchers("/", "/api/api-docs/**", "/api/swagger-ui.html", "/api/swagger-ui/**")
                .permitAll()
                .anyRequest()
                .authenticated());

        return http.build();
    }
}
