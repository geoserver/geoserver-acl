/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

class CachingAuthorizationServiceTest {

    private CachingAuthorizationService caching;
    private AuthorizationService delegate;
    private ConcurrentMap<AccessRequest, AccessInfo> dataAccessCache;
    private ConcurrentMap<AdminAccessRequest, AdminAccessInfo> adminAccessCache;
    private ConcurrentMap<AccessSummaryRequest, AccessSummary> viewablesCache;

    @BeforeEach
    void setUp() throws Exception {
        delegate = mock(AuthorizationService.class);
        dataAccessCache = new ConcurrentHashMap<>();
        adminAccessCache = new ConcurrentHashMap<>();
        viewablesCache = new ConcurrentHashMap<>();
        caching =
                new CachingAuthorizationService(
                        delegate, dataAccessCache, adminAccessCache, viewablesCache);
    }

    @Test
    void testCachingAuthorizationService() {
        var npe = NullPointerException.class;
        assertThrows(
                npe,
                () ->
                        new CachingAuthorizationService(
                                null, dataAccessCache, adminAccessCache, viewablesCache));
        assertThrows(
                npe,
                () ->
                        new CachingAuthorizationService(
                                delegate, null, adminAccessCache, viewablesCache));
        assertThrows(
                npe,
                () ->
                        new CachingAuthorizationService(
                                delegate, dataAccessCache, null, viewablesCache));
        assertThrows(
                npe,
                () ->
                        new CachingAuthorizationService(
                                delegate, dataAccessCache, adminAccessCache, null));
    }

    @Test
    void testGetAccessInfo() {
        AccessRequest req = AccessRequest.builder().roles("ROLE_AUTHENTICATED").build();
        AccessInfo expected = AccessInfo.DENY_ALL.withMatchingRules(List.of("1", "2"));
        when(delegate.getAccessInfo(req)).thenReturn(expected);

        AccessInfo r1 = caching.getAccessInfo(req);
        AccessInfo r2 = caching.getAccessInfo(req);
        AccessInfo r3 = caching.getAccessInfo(req);
        assertSame(expected, r1);
        assertSame(r1, r2);
        assertSame(r2, r3);
        assertSame(expected, this.dataAccessCache.get(req));
        verify(delegate, times(1)).getAccessInfo(req);
    }

    @Test
    void testGetAdminAuthorization() {
        AdminAccessRequest req =
                AdminAccessRequest.builder().roles("ROLE_AUTHENTICATED").workspace("test").build();
        AdminAccessInfo expected =
                AdminAccessInfo.builder()
                        .admin(false)
                        .workspace("test")
                        .matchingAdminRule("1")
                        .build();
        when(delegate.getAdminAuthorization(req)).thenReturn(expected);

        AdminAccessInfo r1 = caching.getAdminAuthorization(req);
        AdminAccessInfo r2 = caching.getAdminAuthorization(req);
        AdminAccessInfo r3 = caching.getAdminAuthorization(req);
        assertSame(expected, r1);
        assertSame(r1, r2);
        assertSame(r2, r3);
        assertSame(expected, this.adminAccessCache.get(req));
        verify(delegate, times(1)).getAdminAuthorization(req);
    }

    @Test
    void testOnRuleEventEvictsAll() {
        Rule rule1 = Rule.allow().withId("r1").withWorkspace("ws1").withLayer("l1");
        Rule rule2 = rule1.withId("r2").withLayer("l2");
        Rule rule3 = rule1.withId("r3").withLayer("l3");

        AccessRequest req1 = req(rule1);
        AccessRequest req2 = req(rule2);
        AccessRequest req3 = req(rule3);

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onRuleEvent(RuleEvent.updated(rule1));
        assertThat(dataAccessCache).isEmpty();

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onRuleEvent(RuleEvent.deleted(rule2.getId()));
        assertThat(dataAccessCache).isEmpty();

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onRuleEvent(RuleEvent.created(rule3));
        assertThat(dataAccessCache).isEmpty();
    }

    private void grantAll(
            Rule rule1,
            Rule rule2,
            Rule rule3,
            AccessRequest req1,
            AccessRequest req2,
            AccessRequest req3) {
        grant(req1, rule1);
        grant(req2, rule1, rule2);
        grant(req3, rule1, rule2, rule3);
        assertThat(dataAccessCache).containsKeys(req1, req2, req3);
    }

    @Test
    void testOnAdminRuleEventEvictsAll() {
        var rule1 = AdminRule.admin().withId("r1").withWorkspace("ws1");
        var rule2 = rule1.withId("r2");
        var rule3 = rule1.withId("r3");

        var req1 = AdminAccessRequest.builder().user("user1").workspace("ws1").build();
        var req2 = req1.withUser("user2");
        var req3 = req1.withUser("user3");

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onAdminRuleEvent(AdminRuleEvent.created(rule1));
        assertThat(adminAccessCache).isEmpty();

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onAdminRuleEvent(AdminRuleEvent.updated(rule2));
        assertThat(adminAccessCache).isEmpty();

        grantAll(rule1, rule2, rule3, req1, req2, req3);
        caching.onAdminRuleEvent(AdminRuleEvent.deleted(rule3.getId()));
        assertThat(adminAccessCache).isEmpty();
    }

    private void grantAll(
            AdminRule rule1,
            AdminRule rule2,
            AdminRule rule3,
            AdminAccessRequest req1,
            AdminAccessRequest req2,
            AdminAccessRequest req3) {
        grant(req1, rule1);
        grant(req2, rule2);
        grant(req3, rule3);
        assertThat(adminAccessCache).hasSize(3);
    }

    private AccessInfo grant(AccessRequest req, Rule... matching) {
        List<String> ids = Stream.of(matching).map(Rule::getId).toList();
        AccessInfo grant = AccessInfo.ALLOW_ALL.withMatchingRules(ids);
        this.dataAccessCache.put(req, grant);
        return grant;
    }

    private AdminAccessInfo grant(AdminAccessRequest req, AdminRule matching) {
        AdminAccessInfo grant =
                AdminAccessInfo.builder().admin(false).matchingAdminRule(matching.getId()).build();
        this.adminAccessCache.put(req, grant);
        return grant;
    }

    private AccessRequest req(Rule r) {
        return AccessRequest.builder()
                .roles("TEST_ROLE")
                .workspace(r.getIdentifier().getWorkspace())
                .layer(r.getIdentifier().getLayer())
                .build();
    }
}
