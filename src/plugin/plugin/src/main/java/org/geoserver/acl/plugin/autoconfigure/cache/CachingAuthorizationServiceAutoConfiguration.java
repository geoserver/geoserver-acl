/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.autoconfigure.cache;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.authorization.cache.CachingAuthorizationServiceConfiguration;
import org.geoserver.acl.plugin.autoconfigure.accessmanager.AclAccessManagerAutoConfiguration;
import org.geoserver.acl.plugin.autoconfigure.accessmanager.ConditionalOnAclEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @since 1.0
 * @see CachingAuthorizationServiceConfiguration
 */
@AutoConfiguration(after = AclAccessManagerAutoConfiguration.class)
@ConditionalOnAclEnabled
@ConditionalOnProperty(
        name = "geoserver.acl.client.caching",
        havingValue = "true",
        matchIfMissing = true)
@Import(CachingAuthorizationServiceConfiguration.class)
@Slf4j(topic = "org.geoserver.acl.plugin.autoconfigure.cache")
public class CachingAuthorizationServiceAutoConfiguration {

    @PostConstruct
    void logUsing() {
        log.info("Caching ACL AuthorizationService enabled");
    }
}
