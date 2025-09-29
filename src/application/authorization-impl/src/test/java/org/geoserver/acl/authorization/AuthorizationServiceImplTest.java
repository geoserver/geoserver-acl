/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.adminrules.AdminGrantType.ADMIN;
import static org.geoserver.acl.domain.adminrules.AdminGrantType.USER;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;
import static org.geoserver.acl.domain.rules.GrantType.LIMIT;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geoserver.acl.authorization.WorkspaceAccessSummary.Builder;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.MemoryRuleRepository;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.junit.jupiter.api.Test;

/**
 * {@link AuthorizationService} integration/conformance test
 *
 * <p>Concrete implementations must supply the required services in {@link
 * BaseAuthorizationServiceTest}
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@SuppressWarnings("java:S5786") // class is public cause it's inherited
public class AuthorizationServiceImplTest extends AuthorizationServiceTest {

    @Override
    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminServiceImpl(new MemoryRuleRepository());
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return new AdminRuleAdminServiceImpl(new MemoryAdminRuleRepository());
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return new AuthorizationServiceImpl(super.adminruleAdminService, super.ruleAdminService);
    }

    @Test
    void getAdminRulesByWorkspace() {
        AuthorizationServiceImpl service = (AuthorizationServiceImpl) super.authorizationService;
        String user = "user1";
        Set<String> roles = Set.of("ROLE_1", "ROLE_2");

        Map<String, List<AdminRule>> adminRules;

        adminRules = service.getAdminRulesByWorkspace(user, roles);
        assertThat(adminRules).isEmpty();

        AdminRule r1 = insert(ADMIN, 1, "*", "ROLE_1", "ws1");
        adminRules = service.getAdminRulesByWorkspace(user, roles);
        assertThat(adminRules).isEqualTo(Map.of("ws1", list(r1)));

        AdminRule r2 = insert(ADMIN, 2, "*", "ROLE_2", "ws2");
        adminRules = service.getAdminRulesByWorkspace(user, roles);
        assertThat(adminRules).isEqualTo(Map.of("ws1", list(r1), "ws2", list(r2)));

        AdminRule ws3UserRule = insert(USER, 3, "user1", "*", "ws3");
        adminRules = service.getAdminRulesByWorkspace(user, roles);
        assertThat(adminRules).isEqualTo(Map.of("ws1", list(r1), "ws2", list(r2), "ws3", list(ws3UserRule)));

        AdminRule ws3RoleRule = insert(ADMIN, 4, "*", "ROLE_2", "ws3");
        adminRules = service.getAdminRulesByWorkspace(user, roles);
        assertThat(adminRules)
                .isEqualTo(Map.of("ws1", list(r1), "ws2", list(r2), "ws3", list(ws3UserRule, ws3RoleRule)));
    }

    @Test
    void conflateAdminRules() {
        AuthorizationServiceImpl service = (AuthorizationServiceImpl) super.authorizationService;

        AdminRule r1 = insert(ADMIN, 1, "*", "ROLE_1", "ws1");
        AdminRule r2 = insert(ADMIN, 2, "*", "ROLE_2", "ws2");
        AdminRule r3 = insert(USER, 3, "user1", "*", "ws3");
        AdminRule r4 = insert(ADMIN, 4, "*", "ROLE_2", "ws3");

        AdminAccessRequest ws3AdminReq = AdminAccessRequest.builder()
                .user("user1")
                .roles("ROLE_1", "ROLE_2")
                .workspace("ws3")
                .build();
        // verify r3 takes over r4
        AdminAccessInfo ws3Auth = service.getAdminAuthorization(ws3AdminReq);
        assertThat(ws3Auth.isAdmin()).isFalse();

        // conflate should give only user access
        var builder = WorkspaceAccessSummary.builder().workspace("ws3");
        service.conflateAdminRules(builder, List.of(r3, r4));
        var wsSummary = builder.build();
        assertThat(wsSummary.getAdminAccess()).isEqualTo(USER);

        builder = WorkspaceAccessSummary.builder().workspace("ws3");
        service.conflateAdminRules(builder, List.of(r4, r3));
        wsSummary = builder.build();
        assertThat(wsSummary.getAdminAccess()).isEqualTo(USER);
    }

    @Test
    void getRulesByWorkspace() {
        String user = "user1";
        Set<String> roles = Set.of("ROLE_1", "ROLE_2");
        AuthorizationServiceImpl service = (AuthorizationServiceImpl) super.authorizationService;
        Map<String, List<Rule>> actual;

        actual = service.getRulesByWorkspace(user, roles);
        assertThat(actual).isEmpty();

        Rule r1 = insert(ALLOW, 1, "*", "ROLE_1", "ws1", "*");
        Rule r2 = insert(ALLOW, 2, "*", "ROLE_2", "ws2", "*");
        Rule r3 = insert(ALLOW, 3, "user1", "*", "ws1", "*");
        Rule r4 = insert(ALLOW, 4, "user1", "*", "ws2", "*");
        insert(ALLOW, 5, "user2", "*", "ws2", "*");
        insert(DENY, 6, "*", "ROLE_3", "ws2", "*");

        actual = service.getRulesByWorkspace(user, roles);
        assertThat(actual)
                .isEqualTo(Map.of(
                        "ws1", list(r1, r3),
                        "ws2", list(r2, r4)));
    }

    @Test
    void getRulesByWorkspace2() {
        Rule r1 = insert(ALLOW, 1, "*", "ROLE_1", "w1", "L1");
        Rule r2 = insert(ALLOW, 2, "*", "ROLE_2", "w1", "L2");
        Rule r3 = insert(DENY, 3, "user1", "*", "w1", "*");
        insert(ALLOW, 5, "user2", "*", "w1", "*");

        AuthorizationServiceImpl service = (AuthorizationServiceImpl) super.authorizationService;
        String user = "user1";
        Set<String> roles = Set.of("ROLE_1", "ROLE_2");
        var actual = service.getRulesByWorkspace(user, roles);
        assertThat(actual).isEqualTo(Map.of("w1", list(r1, r2, r3)));
    }

    @Test
    void conflateRules() {
        Rule r1 = insert(LIMIT, 1, "*", "ROLE_1", "ws1", "L1");
        var wsSummary = conflateRules("ws1", r1);
        assertThat(wsSummary.getAllowed()).isEmpty();
        assertThat(wsSummary.getForbidden()).isEmpty();

        Rule r2 = insert(ALLOW, 2, "*", "ROLE_2", "ws1", "L1");
        wsSummary = conflateRules("ws1", r1, r2);
        assertThat(wsSummary.getAllowed()).containsOnly("L1");
        assertThat(wsSummary.getForbidden()).isEmpty();

        Rule r3 = insert(ALLOW, 3, "user1", "*", "ws1", "*");
        Rule r4 = insert(DENY, 4, "user1", "*", "ws1", "L3");

        wsSummary = conflateRules("ws1", r1, r2, r3, r4);
        assertThat(wsSummary.getAllowed()).containsOnly("*");
        assertThat(wsSummary.getForbidden()).containsOnly("L3");
    }

    WorkspaceAccessSummary conflateRules(String ws, Rule... rules) {
        Builder builder = builder(ws);
        AuthorizationServiceImpl service = (AuthorizationServiceImpl) super.authorizationService;
        service.conflateRules(builder, list(rules));
        return builder.build();
    }

    private WorkspaceAccessSummary.Builder builder(String ws) {
        return WorkspaceAccessSummary.builder().workspace(ws);
    }

    private <T> List<T> list(@SuppressWarnings("unchecked") T... items) {
        return List.of(items);
    }

    protected Rule insert(GrantType access, long priority, String user, String role, String workspace, String layer) {

        if ("*".equals(user)) user = null;
        if ("*".equals(role)) role = null;
        if ("*".equals(workspace)) workspace = null;
        if ("*".equals(layer)) layer = null;
        return insert(priority, user, role, null, null, null, null, workspace, layer, access);
    }
}
