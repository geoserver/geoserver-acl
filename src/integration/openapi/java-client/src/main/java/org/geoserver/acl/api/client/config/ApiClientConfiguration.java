/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.config;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.api.client.DataRulesApi;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.client.AclClient;
import org.geoserver.acl.client.AclClientAdaptor;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Include this configuration to contribute an {@link org.geoserver.acl.api.client.ApiClient}
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Slf4j(topic = "org.geoserver.acl.api.client.config")
public class ApiClientConfiguration {

    @Bean
    AclClient aclClient(ApiClientProperties config) throws InterruptedException {

        String basePath = config.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            throw new BeanInitializationException(
                    "Authorization service target URL not provided through config property geoserver.acl.client.basePath");
        }

        String username = config.getUsername();
        String password = config.getPassword();
        boolean debugging = config.isDebug();
        AclClient client = new AclClient();
        client.setBasePath(basePath);
        client.setUsername(username);
        client.setPassword(password);
        client.setLogRequests(debugging);
        if (config.isStartupCheck()) {
            waitForIt(client, config.getInitTimeout());
        }
        return client;
    }

    private void waitForIt(AclClient client, int timeoutSeconds) throws InterruptedException {
        RuntimeException error = null;
        if (timeoutSeconds <= 0) {
            error = connect(client);
        } else {
            final Instant end = Instant.now().plusSeconds(timeoutSeconds);
            error = connect(client);
            while (error != null && Instant.now().isBefore(end)) {
                logWaiting(client, error);
                TimeUnit.SECONDS.sleep(1);
                error = connect(client);
            }
        }
        if (error != null) {
            String msg =
                    format(
                            "Unable to connect to ACL after %,d seconds. URL: %s, user: %s, error: %s",
                            timeoutSeconds,
                            client.getBasePath(),
                            client.getUsername(),
                            error.getMessage());
            throw new BeanInitializationException(msg, error);
        }
    }

    private void logWaiting(AclClient client, RuntimeException e) {
        String msg =
                format(
                        "ACL API endpoint not ready. URL: %s, user: %s, error: %s",
                        client.getBasePath(), client.getUsername(), e.getMessage());
        log.info(msg);
    }

    @Nullable
    private RuntimeException connect(AclClient client) {
        DataRulesApi rulesApi = client.getRulesApi();
        try {
            Integer count = rulesApi.countAllRules();
            log.debug(
                    "Connected to ACL service at {}, rule count: {}", client.getBasePath(), count);
        } catch (RuntimeException e) {
            return e;
        }
        return null;
    }

    @Bean
    AclClientAdaptor aclClientAdaptor(AclClient client) {
        return new AclClientAdaptor(client);
    }

    @Bean
    RuleRepository aclRuleRepositoryClientAdaptor(AclClientAdaptor adaptors) {
        return adaptors.getRuleRepository();
    }

    @Bean
    AdminRuleRepository aclAdminRuleRepositoryClientAdaptor(AclClientAdaptor adaptors) {
        return adaptors.getAdminRuleRepository();
    }

    @Bean
    AuthorizationService aclAuthorizationService(AclClientAdaptor adaptors) {
        return adaptors.getAuthorizationService();
    }
}
