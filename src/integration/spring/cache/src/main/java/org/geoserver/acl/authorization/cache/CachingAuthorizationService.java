/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization.cache;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.ForwardingAuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.springframework.context.event.EventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link AuthorizationService} decorator that caches requests and responses, listens to events for
 * eviction.
 *
 * <p>All {@link RuleEvent rule events} result in a full eviction of the {@link AccessInfo} cache,
 * and all {@link AdminRuleEvent adminrule events} in the eviction of the {@link AdminAccessInfo}
 * cache.
 */
@Slf4j(topic = "org.geoserver.acl.authorization.cache")
public class CachingAuthorizationService extends ForwardingAuthorizationService {

    private final ConcurrentMap<AccessRequest, AccessInfo> ruleAccessCache;
    private final ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminRuleAccessCache;

    public CachingAuthorizationService(
            @NonNull AuthorizationService delegate,
            @NonNull ConcurrentMap<AccessRequest, AccessInfo> dataAccessCache,
            @NonNull ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminAccessCache) {
        super(delegate);

        this.ruleAccessCache = dataAccessCache;
        this.adminRuleAccessCache = adminAccessCache;
    }

    @Override
    public AccessInfo getAccessInfo(@NonNull AccessRequest request) {
        AccessInfo grant = ruleAccessCache.computeIfAbsent(request, this::load);
        if (grant.getMatchingRules().isEmpty()) {
            // do not cache results with no matching rules. It'll make it impossible to evict them
            this.ruleAccessCache.remove(request);
        }

        return grant;
    }

    private AccessInfo load(AccessRequest request) {
        return logLoaded(request, super.getAccessInfo(request));
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(@NonNull AdminAccessRequest request) {
        AdminAccessInfo grant = adminRuleAccessCache.computeIfAbsent(request, this::load);
        if (grant.getMatchingAdminRule() == null) {
            // do not cache results with no matching rules. It'll make it impossible to evict them
            this.adminRuleAccessCache.remove(request);
        }
        return grant;
    }

    private AdminAccessInfo load(AdminAccessRequest request) {
        return logLoaded(request, super.getAdminAuthorization(request));
    }

    private <A> A logLoaded(Object request, A accessInfo) {
        log.debug("loaded and cached {} -> {}", request, accessInfo);
        return accessInfo;
    }

    @EventListener(RuleEvent.class)
    public void onRuleEvent(RuleEvent event) {
        int evictCount = evictAll(ruleAccessCache);
        log.debug("evicted all {} authorizations upon event {}", evictCount, event);
    }

    private int evictAll(Map<?, ?> cache) {
        int size = cache.size();
        cache.clear();
        return size;
    }

    @EventListener(AdminRuleEvent.class)
    public void onAdminRuleEvent(AdminRuleEvent event) {
        int evictCount = evictAll(adminRuleAccessCache);
        log.debug("evicted all {} admin authorizations upon event {}", evictCount, event);
    }
}
