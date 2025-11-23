/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.examples.javaclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.testcontainer.GeoServerAclContainer;
import org.geoserver.acl.webapi.client.AclClientAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Demonstrates how to use {@link AclClientAdapter} to use the {@link RuleAdminService} and {@link AuthorizationService} APIs
 * directly instead of the raw OpenAPI to manipulate rules and perform
 * authorization requests.
 */
@Testcontainers(disabledWithoutDocker = true)
class JavaClientAdaptorExampleTest {

    @Container
    static GeoServerAclContainer aclServer =
            GeoServerAclContainer.currentVersion().withDevMode();

    /**
     * {@link AclClientAdapter} provides domain repositories and the authorization
     * implementation using the {@link AclClientAdapter} API client through
     * {@link AclClientAdapter#createRuleAdminService(java.util.function.Consumer)},
     * {@link AclClientAdapter#getRuleRepository()}, and
     * {@link AclClientAdapter#createAuthorizationService()}
     */
    private AclClientAdapter adaptor;

    @BeforeEach
    void beforeEach() {
        assertTrue(aclServer.isRunning());

        final String apiUrl = aclServer.apiUrl();
        final String username = aclServer.devAdminUser();
        final String password = aclServer.devAdminPassword();

        adaptor = new AclClientAdapter() //
                .setBasePath(apiUrl) //
                .setUsername(username) //
                .setPassword(password);
    }

    @Test
    void testRulesAndAuthorization() {
        List<Rule> rules = createSampleRules();
        assertThat(rules).isNotEmpty();

        // Get the AuthorizationService that will defer requests to the remote ACL
        // service
        AuthorizationService authService = adaptor.createAuthorizationService();

        // a user with ROLE_USER has access to layers in the users_ws workspace
        AccessRequest request = AccessRequest.builder() //
                .user("john")
                .roles("ROLE_AUTHENTICATED", "ROLE_USER") //
                .workspace("users_ws") //
                .layer("layer") //
                .build();

        AccessInfo accessInfo = authService.getAccessInfo(request);
        assertThat(accessInfo.getGrant()).isEqualTo(GrantType.ALLOW);

        // but does not have access to layers in the editors_ws workspace
        request = request.withWorkspace("editors_ws");

        accessInfo = authService.getAccessInfo(request);
        assertThat(accessInfo.getGrant()).isEqualTo(GrantType.DENY);
    }

    public List<Rule> createSampleRules() {
        // you can use a RuleAdminService using the repository implementation provided
        // by the API client adaptor to defer service calls to the remote service
        RuleAdminService service = adaptor.createRuleAdminService();

        // prepare rules to insert
        Rule r1 = Rule.allow().withPriority(1L).withRolename("ROLE_USER").withWorkspace("users_ws");
        Rule r2 = Rule.allow().withPriority(2L).withRolename("ROLE_EDITOR").withWorkspace("editors_ws");

        // insert the rules, response comes with assigned id
        r1 = service.insert(r1);
        r2 = service.insert(r2);
        return List.of(r1, r2);
    }
}
