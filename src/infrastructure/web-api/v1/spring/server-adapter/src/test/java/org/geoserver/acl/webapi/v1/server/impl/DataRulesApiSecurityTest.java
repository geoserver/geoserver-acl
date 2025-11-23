/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server.impl;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.config.webapi.v1.server.ApiServerConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.geoserver.acl.webapi.v1.model.AdminRule;
import org.geoserver.acl.webapi.v1.model.AdminRuleFilter;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.geoserver.acl.webapi.v1.model.LayerDetails;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.geoserver.acl.webapi.v1.model.RuleFilter;
import org.geoserver.acl.webapi.v1.model.RuleLimits;
import org.geoserver.acl.webapi.v1.server.AuthorizationApi;
import org.geoserver.acl.webapi.v1.server.DataRulesApi;
import org.geoserver.acl.webapi.v1.server.DataRulesApiSupport;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = DataRulesApiSecurityTest.SecurityTestConfiguration.class,
        properties = "spring.main.banner-mode=off")
class DataRulesApiSecurityTest {

    private @MockitoBean RuleAdminService rulesService;
    private @MockitoBean AdminRuleAdminService adminRulesService;
    private @MockitoBean AuthorizationService authService;

    private @Autowired DataRulesApi rulesApi;
    private @Autowired WorkspaceAdminRulesApi adminRulesApi;
    private @Autowired AuthorizationApi authApi;

    private @Autowired DataRulesApiSupport support;

    @Configuration
    @Import(ApiServerConfiguration.class)
    @EnableMethodSecurity
    static class SecurityTestConfiguration {}

    @Test
    void rulesApiNoSecuritySetUp() {
        assertAuthCredentialsNotFound(() -> rulesApi.getLayerDetailsByRuleId("id"));
        assertAuthCredentialsNotFound(() -> rulesApi.getRuleById("id1"));
        assertAuthCredentialsNotFound(() -> rulesApi.getRules(null, null));
        assertAuthCredentialsNotFound(rulesApi::countAllRules);
        assertAuthCredentialsNotFound(() -> rulesApi.countRules(new RuleFilter()));
        assertAuthCredentialsNotFound(() -> rulesApi.createRule(new Rule(), InsertPosition.FIXED));
        assertAuthCredentialsNotFound(() -> rulesApi.deleteRuleById("id"));
        assertAuthCredentialsNotFound(() -> rulesApi.findOneRuleByPriority(1L));
        assertAuthCredentialsNotFound(() -> rulesApi.queryRules(1, "2", new RuleFilter()));
        assertAuthCredentialsNotFound(() -> rulesApi.ruleExistsById("1"));
        assertAuthCredentialsNotFound(() -> rulesApi.setRuleAllowedStyles("1", Set.of()));
        assertAuthCredentialsNotFound(() -> rulesApi.setRuleLayerDetails("1", new LayerDetails()));
        assertAuthCredentialsNotFound(() -> rulesApi.setRuleLimits("1", new RuleLimits()));
        assertAuthCredentialsNotFound(() -> rulesApi.shiftRulesByPriority(1L, 2L));
        assertAuthCredentialsNotFound(() -> rulesApi.swapRules("1", "2"));
        assertAuthCredentialsNotFound(() -> rulesApi.updateRuleById("1", new Rule()));
    }

    @Test
    void adminRulesApiNoSecuritySetUp() {
        assertAuthCredentialsNotFound(() -> adminRulesApi.adminRuleExistsById("1"));
        assertAuthCredentialsNotFound(() -> adminRulesApi.countAdminRules(new AdminRuleFilter()));
        assertAuthCredentialsNotFound(() -> adminRulesApi.countAllAdminRules());
        assertAuthCredentialsNotFound(() -> adminRulesApi.createAdminRule(new AdminRule(), InsertPosition.FIXED));
        assertAuthCredentialsNotFound(() -> adminRulesApi.deleteAdminRuleById("1"));
        assertAuthCredentialsNotFound(() -> adminRulesApi.findAdminRules(1, "", new AdminRuleFilter()));
        assertAuthCredentialsNotFound(() -> adminRulesApi.findAllAdminRules(null, null));
        assertAuthCredentialsNotFound(() -> adminRulesApi.findFirstAdminRule(new AdminRuleFilter()));
        assertAuthCredentialsNotFound(() -> adminRulesApi.findOneAdminRuleByPriority(1L));
        assertAuthCredentialsNotFound(() -> adminRulesApi.getAdminRuleById("1"));
        assertAuthCredentialsNotFound(() -> adminRulesApi.shiftAdminRulesByPriority(1L, 2L));
        assertAuthCredentialsNotFound(() -> adminRulesApi.swapAdminRules("1", "2"));
        assertAuthCredentialsNotFound(() -> adminRulesApi.updateAdminRule("1", new AdminRule()));
    }

    @Test
    void authorizationApiNoSecuritySetUp() {
        assertAuthCredentialsNotFound(() -> authApi.getAccessInfo(new AccessRequest()));
        assertAuthCredentialsNotFound(() -> authApi.getAdminAuthorization(new AdminAccessRequest()));
        assertAuthCredentialsNotFound(() -> authApi.getMatchingRules(new AccessRequest()));
    }

    private void assertAuthCredentialsNotFound(Executable callee) {
        assertThrows(AuthenticationCredentialsNotFoundException.class, callee);
    }

    @Test
    @WithMockUser(
            username = "someUser",
            authorities = {"ANY_ROLE"})
    void authorizationApi() {
        authApi.getAccessInfo(new AccessRequest());
        verify(authService, times(1)).getAccessInfo(any());

        authApi.getAdminAuthorization(new AdminAccessRequest());
        verify(authService, times(1)).getAdminAuthorization(any());

        authApi.getMatchingRules(new AccessRequest());
        verify(authService, times(1)).getMatchingRules(any());
    }

    @Test
    @WithMockUser(
            username = "someUser",
            authorities = {"ANY_ROLE"})
    void rulesApiNonAdminUser_readOnlyMethods() {
        assertRulesApiReadOnlyMethodsAllowed();
    }

    @Test
    @WithMockUser(
            username = "someUser",
            authorities = {"ANY_ROLE"})
    void rulesApiNonAdminUser_mutatingMethods() {
        assertAccessDenied(() -> rulesApi.createRule(new Rule(), InsertPosition.FIXED));
        assertAccessDenied(() -> rulesApi.setRuleAllowedStyles("1", Set.of()));
        assertAccessDenied(() -> rulesApi.setRuleLayerDetails("1", new LayerDetails()));
        assertAccessDenied(() -> rulesApi.setRuleLimits("1", new RuleLimits()));
        assertAccessDenied(() -> rulesApi.shiftRulesByPriority(1L, 2L));
        assertAccessDenied(() -> rulesApi.swapRules("1", "2"));
        assertAccessDenied(() -> rulesApi.updateRuleById("1", new Rule()));
        assertAccessDenied(() -> rulesApi.deleteRuleById("id"));
    }

    void assertRulesApiReadOnlyMethodsAllowed() {
        rulesApi.getLayerDetailsByRuleId("id");
        rulesApi.getRuleById("id1");
        rulesApi.getRules(null, null);
        rulesApi.countAllRules();
        rulesApi.countRules(new RuleFilter());
        rulesApi.findOneRuleByPriority(1L);
        rulesApi.queryRules(1, "2", new RuleFilter());
        rulesApi.ruleExistsById("1");
    }

    @Test
    @WithMockUser(
            username = "adminUser",
            authorities = {"ROLE_ADMIN"})
    void rulesApiAdminUser() {
        assertRulesApiReadOnlyMethodsAllowed();

        rulesApi.createRule(support.toApi(org.geoserver.acl.domain.rules.Rule.allow()), InsertPosition.FIXED);
        verify(rulesService, times(1)).insert(any(), any());

        rulesApi.setRuleAllowedStyles("1", Set.of());
        verify(rulesService, times(1)).setAllowedStyles(eq("1"), anySet());

        rulesApi.setRuleLayerDetails("1", new LayerDetails());
        verify(rulesService, times(1)).setLayerDetails(eq("1"), any());

        rulesApi.setRuleLimits("1", new RuleLimits());
        verify(rulesService, times(1)).setLimits(eq("1"), any());

        rulesApi.shiftRulesByPriority(1L, 2L);
        verify(rulesService, times(1)).shift(eq(1L), eq(2L));

        rulesApi.swapRules("1", "2");
        verify(rulesService, times(1)).swapPriority("1", "2");

        rulesApi.deleteRuleById("id");
        verify(rulesService, times(1)).delete("id");

        rulesApi.updateRuleById("1", new Rule());
    }

    @Test
    @WithMockUser(
            username = "someUser",
            authorities = {"ANY_ROLE"})
    void adminRulesApiNonAdminUser_readOnlyMethods() {
        assertAdminRulesApiReadOnlyAllowed();
    }

    private void assertAdminRulesApiReadOnlyAllowed() {
        try {
            adminRulesApi.adminRuleExistsById("1");
            adminRulesApi.countAdminRules(new AdminRuleFilter());
            adminRulesApi.countAllAdminRules();
            adminRulesApi.findAdminRules(1, "", new AdminRuleFilter());
            adminRulesApi.findAllAdminRules(null, null);
            adminRulesApi.findFirstAdminRule(new AdminRuleFilter());
            adminRulesApi.findOneAdminRuleByPriority(1L);
            adminRulesApi.getAdminRuleById("1");
        } catch (Exception e) {
            fail("No exception expected: " + e.getMessage());
        }
    }

    @Test
    @WithMockUser(
            username = "someUser",
            authorities = {"ANY_ROLE"})
    void adminRulesApiNonAdminUser_mutatingMethods() {
        assertAccessDenied(() -> adminRulesApi.createAdminRule(new AdminRule(), InsertPosition.FIXED));
        assertAccessDenied(() -> adminRulesApi.deleteAdminRuleById("1"));
        assertAccessDenied(() -> adminRulesApi.shiftAdminRulesByPriority(1L, 2L));
        assertAccessDenied(() -> adminRulesApi.swapAdminRules("1", "2"));
        assertAccessDenied(() -> adminRulesApi.updateAdminRule("1", new AdminRule()));
    }

    @Test
    @WithMockUser(
            username = "adminUser",
            authorities = {"ROLE_ADMIN"})
    void adminRulesApiAdminUser() {
        assertAdminRulesApiReadOnlyAllowed();

        try {
            adminRulesApi.createAdminRule(new AdminRule(), InsertPosition.FIXED);
            adminRulesApi.deleteAdminRuleById("1");
            adminRulesApi.shiftAdminRulesByPriority(1L, 2L);
            adminRulesApi.swapAdminRules("1", "2");
            adminRulesApi.updateAdminRule("1", new AdminRule());
        } catch (Exception e) {
            fail("No exception expected: " + e.getMessage());
        }
    }

    private void assertAccessDenied(Executable callee) {
        assertThrows(AccessDeniedException.class, callee);
    }
}
