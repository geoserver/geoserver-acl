/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.support;

import org.geoserver.acl.api.client.config.ApiClientConfiguration;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.AuthorizationServiceImpl;
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;
import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
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
                // repositories from authorization-api-client
                ApiClientConfiguration.class,
                // services from authorization-domain-spring-integration
                RuleAdminServiceConfiguration.class,
                AdminRuleAdminServiceConfiguration.class);
        clientContext.refresh();
        return this;
    }

    /**
     * Enables/disables http client request/response logging, but breaks exception dispatching for
     * error codes, throwing ResourceAccessException (wrapping an IOException) instead of an
     * HttpClientErrorException subclass as it tries to read the response body without checking if
     * there's one, see https://jira.spring.io/browse/SPR-8713?
     */
    public ClientContextSupport log(boolean logRequests) {
        this.logRequests = logRequests;
        if (null != clientContext && clientContext.isActive()) {
            clientContext.getBean(AclClient.class).setLogRequests(logRequests);
        }
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
        AclClientAdaptor adaptors = clientContext.getBean(AclClientAdaptor.class);
        return adaptors.getAuthorizationService();
    }
}
