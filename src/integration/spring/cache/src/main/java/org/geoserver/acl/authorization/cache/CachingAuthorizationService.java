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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
        if (grant.getMatchingRules().isEmpty()) {}

        return grant;
    }

    private AccessInfo load(AccessRequest request) {
        return super.getAccessInfo(request);
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(@NonNull AdminAccessRequest request) {
        return adminRuleAccessCache.computeIfAbsent(request, this::load);
    }

    private AdminAccessInfo load(AdminAccessRequest request) {
        return super.getAdminAuthorization(request);
    }

    @EventListener(RuleEvent.class)
    public void onRuleEvent(RuleEvent event) {
        switch (event.getEventType()) {
            case DELETED, UPDATED:
                evictRuleAccessCache(event);
                break;
            case CREATED:
            default:
                break;
        }
    }

    @EventListener(AdminRuleEvent.class)
    public void onAdminRuleEvent(AdminRuleEvent event) {
        switch (event.getEventType()) {
            case DELETED, UPDATED:
                evictAdminAccessCache(event);
                break;
            case CREATED:
            default:
                break;
        }
    }

    private void evictRuleAccessCache(RuleEvent event) {
        final Set<String> affectedRuleIds = event.getRuleIds();
        ruleAccessCache.entrySet().stream()
                .parallel()
                .filter(e -> matches(e.getValue(), affectedRuleIds))
                .forEach(
                        e -> {
                            AccessRequest req = e.getKey();
                            AccessInfo grant = e.getValue();
                            ruleAccessCache.remove(req);
                            logEvicted(event, req, grant);
                        });
    }

    private void evictAdminAccessCache(AdminRuleEvent event) {
        final Set<String> affectedRuleIds = event.getRuleIds();
        adminRuleAccessCache.entrySet().stream()
                .parallel()
                .filter(e -> matches(e.getValue(), affectedRuleIds))
                .forEach(
                        e -> {
                            AdminAccessRequest req = e.getKey();
                            AdminAccessInfo grant = e.getValue();
                            adminRuleAccessCache.remove(req);
                            logEvicted(event, req, grant);
                        });
    }

    private boolean matches(AccessInfo cached, final Set<String> affectedRuleIds) {
        List<String> matchingRules = cached.getMatchingRules();
        return matchingRules.stream().anyMatch(affectedRuleIds::contains);
    }

    private boolean matches(AdminAccessInfo cached, final Set<String> affectedRuleIds) {
        String matchingRuleId = cached.getMatchingAdminRule();
        return affectedRuleIds.contains(matchingRuleId);
    }

    private void logEvicted(Object event, Object req, Object grant) {
        if (log.isDebugEnabled()) {
            log.debug("event: {}, evicted {} -> {}", event, req, grant);
        }
    }
}
