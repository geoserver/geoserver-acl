/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.client;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.geoserver.acl.api.client.integration.AdminRuleRepositoryClientAdaptor;
import org.geoserver.acl.api.client.integration.AuthorizationServiceClientAdaptor;
import org.geoserver.acl.api.client.integration.RuleRepositoryClientAdaptor;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;

public class AclClientAdaptor {

    private final @NonNull @Getter AclClient client;

    private final @NonNull @Getter RuleRepositoryClientAdaptor ruleRepository;
    private final @NonNull @Getter AdminRuleRepository adminRuleRepository;
    private @NonNull @Getter @Setter AuthorizationService authorizationService;

    public AclClientAdaptor(@NonNull AclClient client) {
        this.client = client;
        this.ruleRepository = new RuleRepositoryClientAdaptor(client.getRulesApi());
        this.adminRuleRepository = new AdminRuleRepositoryClientAdaptor(client.getAdminRulesApi());
        this.authorizationService = new AuthorizationServiceClientAdaptor(client.getAuthorizationApi());
    }
}
