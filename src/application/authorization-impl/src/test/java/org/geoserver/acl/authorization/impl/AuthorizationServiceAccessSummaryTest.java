/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.authorization.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.adminrules.AdminGrantType.ADMIN;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.authorization.WorkspaceAccessSummary;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration/comformance test for {@link
 * AuthorizationService#getUserAccessSummary(AccessSummaryRequest)}
 *
 * @since 2.3
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AuthorizationServiceAccessSummaryTest extends BaseAuthorizationServiceTest {

    AccessSummaryRequest req(@NonNull String user, @NonNull String... roles) {
        return AccessSummaryRequest.builder()
                .user(user)
                .roles(Set.copyOf(List.of(roles)))
                .build();
    }

    @Test
    @Order(9)
    void adminRules() {
        insert(ADMIN, 1, "*", "ROLE_1", "ws1");
        AccessSummaryRequest req = req("*", "ROLE_1");
        Set<String> allowedLayers = Set.of();
        Set<String> forbiddenLayers = Set.of();
        AccessSummary expected = summary(workspace(ADMIN, "ws1", allowedLayers, forbiddenLayers));
        AccessSummary actual = authorizationService.getUserAccessSummary(req);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Order(10)
    @DisplayName("given an empty rule database, nothing is visible")
    void empty() {
        AccessSummaryRequest req = req("user1", "ROLE_1");
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        assertThat(viewables).isNotNull();
        assertThat(viewables.getWorkspaces()).isEmpty();
    }

    @Test
    @Order(20)
    @DisplayName("given a single matching rule on role with no layer, all layers in the workspace are visible")
    void singleWorkspaceRuleAllowAllLayers() {
        AccessSummaryRequest req = req("user1", "ROLE_1");
        insert(1, "*", "ROLE_1", "w1", "*", ALLOW);
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        AccessSummary expected = summary(workspace("w1", "*"));
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(30)
    void rulesMatchingUsernameAndRoles() {
        AccessSummaryRequest req = req("user1", "ROLE_1", "ROLE_2");
        insert(1, "*", "ROLE_1", "w1", "*", ALLOW);
        insert(2, "*", "ROLE_2", "w2", "allowed1", ALLOW);
        insert(3, "user1", null, "w3", "L3", ALLOW);
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        AccessSummary expected = summary(workspace("w1", "*"), workspace("w2", "allowed1"), workspace("w3", "L3"));
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(40)
    void denyRulePreserverdIfCatchesAllRequests() {
        AuthorizationService service = authorizationService;
        AccessSummaryRequest req = req("user1", "ROLE_1", "ROLE_2");
        insert(1, "user1", "*", "w1", "hidden1", DENY);
        insert(2, "*", "ROLE_1", "w1", "*", ALLOW);

        AccessRequest.Builder accessRequestBuilder =
                AccessRequest.builder().user("user1").roles("ROLE_1", "ROLE_2").workspace("w1");

        AccessInfo accessInfo =
                service.getAccessInfo(accessRequestBuilder.layer("visible").build());
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);

        accessInfo = service.getAccessInfo(accessRequestBuilder.layer("hidden1").build());
        assertThat(accessInfo.grant()).isEqualTo(DENY);

        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        Set<String> allowed = Set.of("*");
        Set<String> forbidden = Set.of("hidden1");
        AccessSummary expected = summary(workspace("w1", allowed, forbidden));
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(41)
    void denyRuleRemovedIfNotCatchAll() {
        // rule 2 prevails over rule 1's service-scoped deny: the summary lists forbidden layers
        // only when they can never be visible, and rule 2 makes hidden1 visible outside WMS
        insert(1, "user1", "*", null, "WMS", null, null, "w1", "hidden1", DENY);
        insert(2, "*", "ROLE_1", "w1", "*", ALLOW);

        AccessSummaryRequest req = req("user1", "ROLE_1", "ROLE_2");
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        Set<String> allowed = Set.of("*");
        Set<String> forbidden = Set.of();
        AccessSummary expected = summary(workspace("w1", allowed, forbidden));
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(42)
    void denyAllPreservedButExplicitAllowRuleAlsoPreserved() {
        AccessSummaryRequest req = req("user1", "ROLE_1", "ROLE_2");
        insert(1, "*", "ROLE_1", "w1", "L1", ALLOW);
        insert(2, "*", "ROLE_2", "w1", "L2", ALLOW);
        insert(3, "user1", "*", "w1", "*", DENY);

        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        Set<String> allowed = Set.of("L1", "L2");
        Set<String> forbidden = Set.of("*");
        AccessSummary expected = summary(workspace("w1", allowed, forbidden));
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(50)
    void singleRoleMatchAndDefaultAllow() {
        AccessSummaryRequest req = req("user1", "ROLE_1");
        insert(1, "user1", "ROLE_1", "w1", "*", ALLOW);
        insert(2, null, null, "w2", null, ALLOW);
        AccessSummary expected = summary(workspace("w1", "*"), workspace("w2", "*"));
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        assertThat(viewables).isEqualTo(expected);
    }

    @Test
    @Order(60)
    void workspaceAdminMustAdhereToExplicitlyHiddenLayers() {
        insert(1, "user1", "*", "w1", null, ALLOW);
        insert(2, "*", "ROLE_1", "w1", "hiddenlayer", DENY);
        insert(3, "*", "ROLE_2", "w1", "*", ALLOW);

        AccessSummary expected = summary(workspace("w1", "*", "hiddenlayer"));

        AccessSummaryRequest req = req("user1", "ROLE_1", "ROLE_2");
        AccessSummary viewables = authorizationService.getUserAccessSummary(req);
        assertThat(viewables).isEqualTo(expected);

        insert(4, "*", "ROLE_2", "*", "*", ALLOW);
        // promoting to admin must not reveal layers explicitly denied at the data-rule level
        insert(ADMIN, 1, "*", "ROLE_1", "w1");

        expected = summary(workspace("*", "*"), workspace(ADMIN, "w1", Set.of("*"), Set.of("hiddenlayer")));
        viewables = authorizationService.getUserAccessSummary(req);
        assertThat(viewables).isEqualTo(expected);
    }

    protected WorkspaceAccessSummary workspace(@NonNull String workspace, @NonNull String allowedLayer) {
        return workspace(workspace, Set.of(allowedLayer), Set.of());
    }

    protected WorkspaceAccessSummary workspace(
            @NonNull String workspace, @NonNull String allowedLayer, @NonNull String forbiddenLayer) {
        return workspace(workspace, Set.of(allowedLayer), Set.of(forbiddenLayer));
    }

    protected WorkspaceAccessSummary workspace(
            AdminGrantType admin, @NonNull String workspace, @NonNull String allowedLayer) {
        return WorkspaceAccessSummary.builder()
                .workspace(workspace)
                .adminAccess(admin)
                .addAllowed(allowedLayer)
                .build();
    }

    protected WorkspaceAccessSummary workspace(
            @NonNull String workspace, @NonNull Set<String> allowedLayers, @NonNull Set<String> forbiddenLayers) {
        return workspace(null, workspace, allowedLayers, forbiddenLayers);
    }

    protected WorkspaceAccessSummary workspace(
            AdminGrantType admin,
            @NonNull String workspace,
            @NonNull Set<String> allowedLayers,
            @NonNull Set<String> forbiddenLayers) {
        return WorkspaceAccessSummary.builder()
                .workspace(workspace)
                .adminAccess(admin)
                .allowed(allowedLayers)
                .forbidden(forbiddenLayers)
                .build();
    }

    protected Rule insert(int priority, String user, String role, String workspace, String layer, GrantType access) {
        if ("*".equals(user)) user = null;
        if ("*".equals(role)) role = null;
        if ("*".equals(workspace)) workspace = null;
        if ("*".equals(layer)) layer = null;
        return super.insert(priority, user, role, null, null, null, null, workspace, layer, access);
    }

    private AccessSummary summary(WorkspaceAccessSummary... workspaceSummaries) {
        return AccessSummary.of(Arrays.asList(workspaceSummaries));
    }
}
