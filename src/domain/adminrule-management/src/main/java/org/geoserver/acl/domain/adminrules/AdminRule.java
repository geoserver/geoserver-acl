/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.adminrules;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

/**
 * An AdminRule expresses if a given combination of request access is allowed or not.
 *
 * <p>It's used for setting admin privileges on workspaces.
 *
 * <p>AdminRule filtering and selection is almost identical to {@see Rule}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AdminRule {

    private String id;

    /**
     * External Id. An ID used in an external systems. This field should simplify Authorization
     * integration in complex systems.
     */
    private String extId;

    private String name;
    private String description;

    private long priority;

    @NonNull @Default
    private AdminRuleIdentifier identifier = AdminRuleIdentifier.builder().build();

    private AdminGrantType access;

    public @Override String toString() {
        return String.format(
                "AdminRule[id: %s, priority: %d, access: %s,  %s]",
                id, priority, access, identifier.toShortString());
    }

    public AdminRule withInstanceName(String instanceName) {
        return withIdentifier(identifier.withInstanceName(instanceName));
    }

    public AdminRule withUsername(String username) {
        return withIdentifier(identifier.withUsername(username));
    }

    public AdminRule withRolename(String rolename) {
        return withIdentifier(identifier.withRolename(rolename));
    }

    public AdminRule withWorkspace(String workspace) {
        return withIdentifier(identifier.withWorkspace(workspace));
    }

    public AdminRule withAddressRange(String addressRange) {
        return withIdentifier(identifier.withAddressRange(addressRange));
    }

    public static AdminRule user() {
        return AdminRule.builder().access(AdminGrantType.USER).build();
    }

    public static AdminRule admin() {
        return AdminRule.builder().access(AdminGrantType.ADMIN).build();
    }

    public String toShortString() {
        return String.format("access: %s, %s", getAccess(), getIdentifier().toShortString());
    }
}
