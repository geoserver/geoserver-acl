/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.ForwardingAuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.springframework.context.event.EventListener;

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
    private final ConcurrentMap<AccessSummaryRequest, AccessSummary> viewablesCache;

    public CachingAuthorizationService(
            @NonNull AuthorizationService delegate,
            @NonNull ConcurrentMap<AccessRequest, AccessInfo> dataAccessCache,
            @NonNull ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminAccessCache,
            @NonNull ConcurrentMap<AccessSummaryRequest, AccessSummary> viewablesCache) {
        super(delegate);

        this.ruleAccessCache = dataAccessCache;
        this.adminRuleAccessCache = adminAccessCache;
        this.viewablesCache = viewablesCache;
    }

    @Override
    public AccessInfo getAccessInfo(@NonNull AccessRequest request) {
        return ruleAccessCache.computeIfAbsent(request, this::load);
    }

    private AccessInfo load(AccessRequest request) {
        return logLoaded(request, super.getAccessInfo(request));
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(@NonNull AdminAccessRequest request) {
        return adminRuleAccessCache.computeIfAbsent(request, this::load);
    }

    private AdminAccessInfo load(AdminAccessRequest request) {
        return logLoaded(request, super.getAdminAuthorization(request));
    }

    @Override
    public AccessSummary getUserAccessSummary(@NonNull AccessSummaryRequest request) {
        return viewablesCache.computeIfAbsent(request, this::load);
    }

    private AccessSummary load(AccessSummaryRequest request) {
        return logLoaded(request, super.getUserAccessSummary(request));
    }

    private <A> A logLoaded(Object request, A accessInfo) {
        log.debug("loaded and cached {} -> {}", request, accessInfo);
        return accessInfo;
    }

    @EventListener(RuleEvent.class)
    public void onRuleEvent(RuleEvent event) {
        int evictCount = evictAll(ruleAccessCache);
        evictViewables();
        log.debug("evicted all {} authorizations upon event {}", evictCount, event);
    }

    @EventListener(AdminRuleEvent.class)
    public void onAdminRuleEvent(AdminRuleEvent event) {
        int evictCount = evictAll(adminRuleAccessCache);
        evictViewables();
        log.debug("evicted all {} admin authorizations upon event {}", evictCount, event);
    }

    public void evictAll() {
        int dataAuth = evictAll(ruleAccessCache);
        int adminAuth = evictAll(adminRuleAccessCache);
        int summaries = evictAll(viewablesCache);
        int total = dataAuth + adminAuth + summaries;
        log.info("evicted {} cached ACL authorizations", total);
    }

    private int evictAll(Map<?, ?> cache) {
        int size = cache.size();
        cache.clear();
        return size;
    }

    void evictViewables() {
        evictAll(viewablesCache);
    }
}
