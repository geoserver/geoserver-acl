/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.domain.adminrules;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Matching criteria for workspace administration rules.
 *
 * <p>Specifies which users, roles, IP addresses, and workspaces an {@link AdminRule} applies to.
 * Simpler than {@link org.geoserver.acl.domain.rules.RuleIdentifier} - doesn't include service
 * types, request operations, or layers.
 *
 * <p>All fields are optional. Null fields act as wildcards. An AdminRule matches if ALL non-null
 * criteria match.
 *
 * <p>Examples:
 * <ul>
 *   <li>username="alice", workspace="topp" -> Matches user "alice" accessing workspace "topp"
 *   <li>rolename="ROLE_ADMIN", workspace=null -> Matches any workspace for "ROLE_ADMIN"
 *   <li>addressRange="192.168.1.0/24", workspace="internal" -> Matches workspace "internal" from subnet
 *   <li>username=null, workspace="public" -> Matches any user accessing "public"
 * </ul>
 *
 * <p>More specific rules (fewer nulls) should typically have higher priority (lower priority number).
 *
 * <p>Immutable. Use {@code with*()} methods or {@code toBuilder()} for modifications.
 *
 * @since 1.0
 * @see AdminRule
 * @see org.geoserver.acl.domain.rules.RuleIdentifier
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AdminRuleIdentifier {

    /** Username to match (null matches any). */
    private String username;

    /** Role name to match (null matches any). */
    private String rolename;

    /** Workspace name to match (null matches any). */
    private String workspace;

    /** IP address or CIDR range (e.g., "192.168.1.0/24"). Null matches any. */
    private String addressRange;

    public String toShortString() {
        StringBuilder builder = new StringBuilder();
        addNonNull(builder, "username", username);
        addNonNull(builder, "rolename", rolename);
        addNonNull(builder, "workspace", workspace);
        addNonNull(builder, "addressRange", addressRange);
        return builder.toString();
    }

    private void addNonNull(StringBuilder builder, String prop, Object value) {
        if (null != value) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(prop).append(": ").append(value);
        }
    }
}
