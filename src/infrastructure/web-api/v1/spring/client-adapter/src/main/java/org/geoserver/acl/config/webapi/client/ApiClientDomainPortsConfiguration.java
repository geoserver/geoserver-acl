/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.webapi.client;

import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.webapi.client.AclClientAdapter;
import org.geoserver.acl.webapi.v1.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Include this configuration to contribute domain ports backed by an {@link ApiClient}
 * <p>
 * Contributes:
 * <ul>
 * <li>{@link RuleRepository}
 * <li>{@link AdminRuleRepository}
 * <ul>
 * Requires:
 * <ul>
 * <li>{@link AclClientAdapter}
 *
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
@Slf4j(topic = "org.geoserver.acl.webapi.config.client")
public class ApiClientDomainPortsConfiguration {

    @Bean
    RuleRepository aclRuleRepositoryApiClientAdapter(AclClientAdapter builder) {
        return builder.createRuleRepository();
    }

    @Bean
    AdminRuleRepository aclAdminRuleRepositoryApiClientAdapter(AclClientAdapter builder) {
        return builder.createAdminRuleRepository();
    }
}
