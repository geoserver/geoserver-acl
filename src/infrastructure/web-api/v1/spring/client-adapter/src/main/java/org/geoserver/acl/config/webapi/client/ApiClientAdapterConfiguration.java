/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.webapi.client;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.webapi.client.AclClientAdapter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Include this configuration to contribute an {@link AclClientAdapter}, domain and application level services
 * backed by the REST API client.
 * <p>
 * Contributes:
 * <ul>
 * <li> {@link AclClientAdapter}
 * <ul>
 * Requires:
 * <ul>
 * <li> {@link ApiClientProperties}
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Slf4j(topic = "org.geoserver.acl.webapi.config.client")
public class ApiClientAdapterConfiguration {

    @Bean
    AclClientAdapter aclClient(ApiClientProperties config) throws InterruptedException {

        String basePath = config.getBasePath();
        if (!StringUtils.hasText(basePath)) {
            throw new BeanInitializationException(
                    "Authorization service target URL not provided through config property geoserver.acl.client.basePath");
        }

        String username = config.getUsername();
        String password = config.getPassword();
        AclClientAdapter client = new AclClientAdapter();
        client.setBasePath(basePath);
        client.setUsername(username);
        client.setPassword(password);
        if (config.isStartupCheck()) {
            waitForIt(client, config.getInitTimeout());
        }
        return client;
    }

    private void waitForIt(AclClientAdapter client, int timeoutSeconds) throws InterruptedException {
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
            String msg = "Unable to connect to ACL after %,d seconds. URL: %s, user: %s, error: %s"
                    .formatted(timeoutSeconds, client.getBasePath(), client.getUsername(), error.getMessage());
            throw new BeanInitializationException(msg, error);
        }
    }

    private void logWaiting(AclClientAdapter client, RuntimeException e) {
        String msg = "ACL API endpoint not ready. URL: %s, user: %s, error: %s"
                .formatted(client.getBasePath(), client.getUsername(), e.getMessage());
        log.info(msg);
    }

    @Nullable
    private RuntimeException connect(AclClientAdapter client) {
        try {
            RuleAdminService ruleAdminService = client.createRuleAdminService();
            Integer count = ruleAdminService.count();
            log.debug("Connected to ACL service at {}, rule count: {}", client.getBasePath(), count);
        } catch (RuntimeException e) {
            return e;
        }
        return null;
    }
}
