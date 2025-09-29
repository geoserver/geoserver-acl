/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.domain.client;

import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.client.DataRulesApi;
import org.geoserver.acl.api.client.WorkspaceAdminRulesApi;
import org.geoserver.acl.api.client.config.ApiClientConfiguration;
import org.geoserver.acl.api.client.config.ApiClientProperties;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * {@link Configuration @Configuration} to contribute a GeoSever ACL {@link AuthorizationService}
 * that works by delegating to a remote ACL service through the OpenAPI HTTP interface.
 *
 * <p>{@link ApiClientConfiguration} sets up the pure OpenAPI clients {@link DataRulesApi}, {@link
 * WorkspaceAdminRulesApi}, and {@link AuthorizationApi} Java clients.
 *
 * <p>{@link RepositoryClientAdaptorsConfiguration} provides GeoServer ACL repository
 * implementations that adapt the OpenAPI client to ACL's domain repositories.
 *
 * <p>{@link RuleAdminServiceConfiguration}, {@link AdminRuleAdminServiceConfiguration}, and {@link
 * AuthorizationServiceClientAdaptorConfiguration} provide the domain services that expect the
 * repositories provided by {@code RepositoryClientAdaptorsConfiguration} as collaborators.
 *
 * <p>The net effect of this {@code @Configuration} class is the {@link ApplicationContext} is set
 * up with GeoServer ACL domain services for managing the ACL rules and admin rules, as well as to
 * request data and workspace admin grants, over a remote GeoServer ACL service.
 *
 * @see ApiClientConfiguration
 * @see RepositoryClientAdaptorsConfiguration
 */
@Configuration
@Import({
    // repositories from api-client
    ApiClientConfiguration.class,
    // services from domain-spring-integration
    RuleAdminServiceConfiguration.class,
    AdminRuleAdminServiceConfiguration.class,
})
@Slf4j(topic = "org.geoserver.acl.plugin.config.domain.client")
public class ApiClientAclDomainServicesConfiguration {

    @Bean
    ApiClientProperties aclApiClientProperties(Environment env) {
        String basePath = env.getProperty("geoserver.acl.client.basePath");
        String username = env.getProperty("geoserver.acl.client.username");
        String password = env.getProperty("geoserver.acl.client.password");
        boolean debug = env.getProperty("geoserver.acl.client.debug", Boolean.class, false);
        boolean caching = env.getProperty("geoserver.acl.client.caching", Boolean.class, true);
        boolean startupCheck = env.getProperty("geoserver.acl.client.startupCheck", Boolean.class, true);
        Integer initTimeout = env.getProperty("geoserver.acl.client.initTimeout", Integer.class);

        log.info("GeoServer Acess Control List API: {}, user: {}, caching: {}", basePath, username, caching);
        ApiClientProperties configProps = new ApiClientProperties();
        configProps.setBasePath(basePath);
        configProps.setUsername(username);
        configProps.setPassword(password);
        configProps.setDebug(debug);
        configProps.setCaching(caching);
        configProps.setStartupCheck(startupCheck);
        if (null != initTimeout) configProps.setInitTimeout(initTimeout);

        return configProps;
    }
}
