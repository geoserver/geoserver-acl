/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.cache;

import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.authorization.cache.CachingAuthorizationService;
import org.geoserver.acl.authorization.cache.CachingAuthorizationServiceConfiguration;
import org.geoserver.config.impl.GeoServerLifecycleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * Plugin-specific extension for {@link CachingAuthorizationServiceConfiguration} to support
 * GeoServer without spring boot enabling and disabling through ConditionalOnAclEnabled.
 *
 * @since 2.3
 * @see CachingAuthorizationServiceConfiguration
 */
@Configuration
@Import(CachingAuthorizationServiceConfiguration.class)
@Slf4j(topic = "org.geoserver.acl.plugin.config.cache")
public class CachingAuthorizationServicePluginConfiguration {

    @PostConstruct
    void logUsing() {
        log.info("Caching ACL AuthorizationService enabled");
    }

    @Bean
    CachingAclAuthorizationCleanupService cachingAclAuthorizationCleanupService(
            CachingAuthorizationService cachingService) {
        return new CachingAclAuthorizationCleanupService(cachingService);
    }

    static class CachingAclAuthorizationCleanupService implements GeoServerLifecycleHandler {
        private CachingAuthorizationService cachingService;

        public CachingAclAuthorizationCleanupService(CachingAuthorizationService cachingService) {
            this.cachingService = cachingService;
        }

        @Override
        public void onReset() {
            cachingService.evictAll();
        }

        @Override
        public void onDispose() {
            // no=op
        }

        @Override
        public void beforeReload() {
            // no=op
        }

        @Override
        public void onReload() {
            // no=op
        }
    }
}
