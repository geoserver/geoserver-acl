/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.authorization.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.cache.CachingAuthorizationService;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.PropertyResolver;

/**
 * Contributes:
 * <ul>
 * <li> {@link CachingAuthorizationService}
 * </ul>
 * Requires:
 * <ul>
 * <li> Optional: {@link CacheManager}
 * </ul>
 * @since 2.0
 */
@Configuration
@Slf4j(topic = "org.geoserver.acl.config.application.cache")
public class CachingAuthorizationServiceConfiguration {

    private static final Duration DEFAULT_CACHE_TTL = Duration.ofSeconds(30);

    @Bean
    @Primary
    CachingAuthorizationService cachingAuthorizationService(
            AuthorizationService delegate,
            ConcurrentMap<AccessRequest, AccessInfo> authorizationCache,
            ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminAuthorizationCache,
            ConcurrentMap<AccessSummaryRequest, AccessSummary> viewablesCache) {

        return new CachingAuthorizationService(delegate, authorizationCache, adminAuthorizationCache, viewablesCache);
    }

    /**
     * Defines the ACL Auth cache time-to-live from the {@code geoserver.acl.client.cache.ttl}
     * config property, which only applies if there's no {@link CacheManager} in the application
     * context or if it's not a {@link CaffeineCacheManager}.
     *
     * <p>The {@code geoserver.acl.client.cache.ttl} expects a {@link Duration} string, for example:
     *
     * <pre>
     * <code>
     *    {@literal PT20.345S} -- parses as "20.345 seconds"
     *    {@literal PT15M}     -- parses as "15 minutes" (where a minute is 60 seconds)
     *    {@literal PT10H}     -- parses as "10 hours" (where an hour is 3600 seconds)
     *    {@literal P2D}       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
     *    {@literal P2DT3H4M}  -- parses as "2 days, 3 hours and 4 minutes"
     * </code>
     * </pre>
     *
     * Defaults to 30 seconds otherwise.
     */
    @Bean
    Duration aclAuthCacheTTL(Optional<CacheManager> cacheManager, PropertyResolver propertyResolver) {

        Duration expireAfterWrite = DEFAULT_CACHE_TTL;

        if (caffeineCacheManager(cacheManager).isPresent()) {
            log.info(
                    "CaffeineCacheManager is provided, ACL cache time-to-live won't be enforced. Define it in the cache configuration if necessary.");
        } else {
            final String ttl = propertyResolver.getProperty("geoserver.acl.client.cache.ttl");
            if (ttl == null) {
                log.info("Using default ACL cache time-to-live of {}", expireAfterWrite);
            } else {
                try {
                    expireAfterWrite = Duration.parse(ttl);
                } catch (DateTimeParseException e) {
                    String msg = String.format(
                            "Error parsing geoserver.acl.client.cache.ttl='%s', "
                                    + "expected a duration string (e.g. PT10S for 10 seconds)",
                            ttl);
                    throw new BeanInitializationException(msg);
                }
                if (expireAfterWrite.isNegative()) {
                    String msg = String.format(
                            "Got Negative duration from geoserver.acl.client.cache.ttl='%s', "
                                    + "expected a positive duration string (e.g. PT10S for 10 seconds)",
                            ttl);
                    throw new BeanInitializationException(msg);
                }
            }
        }
        return expireAfterWrite;
    }

    @Bean
    ConcurrentMap<AccessRequest, AccessInfo> aclAuthCache(
            Optional<CacheManager> cacheManager, @Qualifier("aclAuthCacheTTL") Duration ttl) {
        return getCache(cacheManager, ttl, "acl-data-grants");
    }

    @Bean
    ConcurrentMap<AdminAccessRequest, AdminAccessInfo> aclAdminAuthCache(
            Optional<CacheManager> cacheManager, @Qualifier("aclAuthCacheTTL") Duration ttl) {
        return getCache(cacheManager, ttl, "acl-admin-grants");
    }

    @Bean
    ConcurrentMap<AccessSummaryRequest, AccessSummary> aclViewablesCache(
            Optional<CacheManager> cacheManager, @Qualifier("aclAuthCacheTTL") Duration ttl) {
        return getCache(cacheManager, ttl, "acl-access-summary");
    }

    @SuppressWarnings("unchecked")
    private <K, V> ConcurrentMap<K, V> getCache(Optional<CacheManager> cacheManager, Duration ttl, String cacheName) {

        return (ConcurrentMap<K, V>) caffeineCacheManager(cacheManager)
                .map(ccm -> getCache(ccm, cacheName))
                .orElseGet(() -> newCache(ttl))
                .asMap();
    }

    private Optional<CaffeineCacheManager> caffeineCacheManager(Optional<CacheManager> cacheManager) {
        return cacheManager.filter(CaffeineCacheManager.class::isInstance).map(CaffeineCacheManager.class::cast);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Cache<K, V> getCache(CaffeineCacheManager ccm, String cacheName) {
        org.springframework.cache.Cache cache = ccm.getCache(cacheName);
        if (cache != null) {
            return (Cache<K, V>) cache.getNativeCache();
        }
        return null;
    }

    private <K, V> Cache<K, V> newCache(Duration ttl) {
        return Caffeine.newBuilder().softValues().expireAfterWrite(ttl).build();
    }
}
