/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.security;

import org.geoserver.acl.api.server.config.AuthorizationApiConfiguration;
import org.geoserver.acl.api.server.config.RulesApiConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@Import({RulesApiConfiguration.class, AuthorizationApiConfiguration.class})
@EnableMethodSecurity
public class SecurityTestConfiguration {}
