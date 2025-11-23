/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.it.webapi.support;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImpl;
import org.geoserver.acl.config.domain.DomainServicesConfiguration;
import org.geoserver.acl.config.webapi.client.ApiClientAdapterConfiguration;
import org.geoserver.acl.config.webapi.client.ApiClientApplicationServicesConfiguration;
import org.geoserver.acl.config.webapi.client.ApiClientDomainPortsConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.webapi.client.AclClientAdapter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

public class ClientContextSupport {

    private AnnotationConfigApplicationContext clientContext;
    private boolean logRequests;
    private int serverPort;

    public ClientContextSupport serverPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public void close() {
        if (clientContext != null) clientContext.close();
    }

    public ClientContextSupport setUp() {

        String basePath = "http://localhost:%d/api".formatted(serverPort);
        String username = "admin";
        String password = "auth-not-yet-implemented";

        ConfigurableEnvironment clientEnv = new MockEnvironment() //
                .withProperty("geoserver.acl.client.basePath", basePath)
                .withProperty("geoserver.acl.client.username", username)
                .withProperty("geoserver.acl.client.password", password)
                .withProperty("geoserver.acl.client.debug", String.valueOf(logRequests));

        clientContext = new AnnotationConfigApplicationContext();
        clientContext.setEnvironment(clientEnv);

        clientContext.register(
                ConfigurationPropertiesTestConfiguration.class,
                // AclClientAdapter from org.geoserver.acl.integration.openapi:gs-acl-api-client
                ApiClientAdapterConfiguration.class,
                // domain ports from org.geoserver.acl.integration.openapi:gs-acl-api-client
                ApiClientDomainPortsConfiguration.class,
                // AuthorizationService from
                // org.geoserver.acl.integration.openapi:gs-acl-api-client
                ApiClientApplicationServicesConfiguration.class,
                // domain services from
                // org.geoserver.acl.integration:gs-acl-domain-spring-integration
                DomainServicesConfiguration.class);
        clientContext.refresh();
        return this;
    }

    public RuleAdminService getRuleAdminServiceClient() {
        return clientContext.getBean(RuleAdminService.class);
    }

    public AdminRuleAdminService getAdminRuleAdminServiceClient() {
        return clientContext.getBean(AdminRuleAdminService.class);
    }

    public AuthorizationService getInProcessAuthorizationService() {
        AdminRuleAdminService adminRuleService = getAdminRuleAdminServiceClient();
        RuleAdminService ruleService = getRuleAdminServiceClient();
        return new AuthorizationServiceImpl(adminRuleService, ruleService);
    }

    public AuthorizationService getAuthorizationServiceClientAdaptor() {
        AclClientAdapter adaptors = clientContext.getBean(AclClientAdapter.class);
        return adaptors.createAuthorizationService();
    }
}
