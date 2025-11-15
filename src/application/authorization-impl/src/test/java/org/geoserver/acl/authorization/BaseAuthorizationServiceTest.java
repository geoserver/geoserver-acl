/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleIdentifier;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
public abstract class BaseAuthorizationServiceTest {

    protected RuleAdminService ruleAdminService;
    protected AdminRuleAdminService adminruleAdminService;
    protected AuthorizationService authorizationService;

    @BeforeEach
    protected void setUp() throws Exception {
        ruleAdminService = getRuleAdminService();
        adminruleAdminService = getAdminRuleAdminService();
        authorizationService = getAuthorizationService();
    }

    protected abstract RuleAdminService getRuleAdminService();

    protected abstract AdminRuleAdminService getAdminRuleAdminService();

    protected abstract AuthorizationService getAuthorizationService();

    protected AccessRequest createRequest(String name, String... roles) {
        validateNotAny(name);
        Set<String> roleNames = Set.of();
        if (null != roles) {
            roleNames = Arrays.stream(roles)
                    .filter(Objects::nonNull)
                    .map(this::validateNotAny)
                    .collect(Collectors.toSet());
        }

        return AccessRequest.builder().user(name).roles(roleNames).build();
    }

    protected String validateNotAny(String value) {
        if (null != value) {
            assertThat(value).isNotEqualTo("*");
        }
        return value;
    }

    protected Rule insert(
            long priority,
            String username,
            String rolename,
            String addressRange,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer,
            GrantType access) {

        Rule rule =
                rule(priority, username, rolename, addressRange, service, request, subfield, workspace, layer, access);
        return insert(rule);
    }

    protected Rule insert(Rule rule) {
        return ruleAdminService.insert(rule);
    }

    protected Rule rule(
            long priority,
            String username,
            String rolename,
            String addressRange,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer,
            GrantType access) {

        RuleIdentifier identifier = RuleIdentifier.builder()
                .username(username)
                .rolename(rolename)
                .addressRange(addressRange)
                .service(service)
                .request(request)
                .subfield(subfield)
                .workspace(workspace)
                .layer(layer)
                .access(access)
                .build();
        return Rule.builder().priority(priority).identifier(identifier).build();
    }

    protected AdminRule insert(AdminRule adminRule) {
        return adminruleAdminService.insert(adminRule);
    }

    protected AdminRule insert(AdminGrantType admin, int priority, String user, String role, String workspace) {
        if ("*".equals(user)) user = null;
        if ("*".equals(role)) role = null;
        if ("*".equals(workspace)) workspace = null;
        AdminRuleIdentifier identifier = AdminRuleIdentifier.builder()
                .username(user)
                .rolename(role)
                .workspace(workspace)
                .build();
        AdminRule rule = AdminRule.builder()
                .priority(priority)
                .access(admin)
                .identifier(identifier)
                .build();
        return insert(rule);
    }
}
