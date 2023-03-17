/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */
package org.geoserver.acl.authorization;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.IPAddressRange;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleIdentifier;
import org.geoserver.acl.rules.RuleAdminService;

import java.util.Set;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
public class ServiceTestBase {

    protected RuleAdminService ruleAdminService;
    protected AdminRuleAdminService adminruleAdminService;
    protected RuleReaderService ruleReaderService;

    protected User createUser(String name, String... groups) {
        return User.builder().name(name).roles(Set.of(groups)).build();
    }

    protected Rule insert(
            long priority,
            String username,
            String rolename,
            String instance,
            IPAddressRange addressRange,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer,
            GrantType access) {

        Rule rule =
                rule(
                        priority,
                        username,
                        rolename,
                        instance,
                        addressRange,
                        service,
                        request,
                        subfield,
                        workspace,
                        layer,
                        access);
        return insert(rule);
    }

    protected Rule insert(Rule rule) {
        return ruleAdminService.insert(rule);
    }

    protected Rule rule(
            long priority,
            String username,
            String rolename,
            String instance,
            IPAddressRange addressRange,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer,
            GrantType access) {

        RuleIdentifier identifier =
                RuleIdentifier.builder()
                        .username(username)
                        .rolename(rolename)
                        .instanceName(instance)
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
}
