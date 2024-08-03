/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentMap;

/**
 * @since 2.0
 */
@Configuration
@EnableCaching
public class CachingAuthorizationServiceConfiguration {

    @Bean
    @Primary
    CachingAuthorizationService cachingAuthorizationService(
            AuthorizationService delegate,
            ConcurrentMap<AccessRequest, AccessInfo> authorizationCache,
            ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminAuthorizationCache,
            ConcurrentMap<AccessSummaryRequest, AccessSummary> viewablesCache) {

        return new CachingAuthorizationService(
                delegate, authorizationCache, adminAuthorizationCache, viewablesCache);
    }

    @Bean
    ConcurrentMap<AccessRequest, AccessInfo> aclAuthCache(CacheManager cacheManager) {
        return getCache(cacheManager, "acl-data-grants");
    }

    @Bean
    ConcurrentMap<AdminAccessRequest, AdminAccessInfo> aclAdminAuthCache(
            CacheManager cacheManager) {
        return getCache(cacheManager, "acl-admin-grants");
    }

    @Bean
    ConcurrentMap<AccessSummaryRequest, AccessSummary> aclViewablesCache(
            CacheManager cacheManager) {
        return getCache(cacheManager, "acl-access-summary");
    }

    @SuppressWarnings("unchecked")
    private <K, V> ConcurrentMap<K, V> getCache(CacheManager cacheManager, String cacheName) {
        if (cacheManager instanceof CaffeineCacheManager ccf) {
            org.springframework.cache.Cache cache = ccf.getCache(cacheName);
            if (cache != null) {
                Cache<K, V> caffeineCache = (Cache<K, V>) cache.getNativeCache();
                return caffeineCache.asMap();
            }
        }

        return newCache();
    }

    private <K, V> ConcurrentMap<K, V> newCache() {
        Cache<K, V> cache = Caffeine.newBuilder().softValues().build();
        return cache.asMap();
    }
}
