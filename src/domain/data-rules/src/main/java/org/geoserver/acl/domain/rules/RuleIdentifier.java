/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.domain.rules;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Matching criteria and access type for a data access rule.
 *
 * <p>Specifies which requests a rule applies to. All fields are optional (can be null) except
 * {@link #access}. Null fields act as wildcards. A rule matches if ALL non-null criteria match.
 *
 * <p>Examples:
 * <ul>
 *   <li>username="alice", rolename=null -> Matches any request from "alice"
 *   <li>username=null, rolename="ROLE_ADMIN" -> Matches any user with "ROLE_ADMIN"
 *   <li>service="WMS", request="GetMap" -> Matches only WMS GetMap requests
 *   <li>workspace="topp", layer=null -> Matches any layer in workspace "topp"
 * </ul>
 *
 * <p>More specific rules (fewer nulls) should typically have higher priority (lower priority number).
 *
 * <p>The {@code service} and {@code request} fields use OGC names, normalized to uppercase:
 * WMS, WFS, WCS, WPS, GetMap, GetFeature, GetCoverage, etc.
 *
 * <p>Immutable. Use {@code with*()} methods or {@code toBuilder()} for modifications.
 *
 * @param access the access grant type (ALLOW, DENY, or LIMIT). Never null; defaults to DENY.
 * @param username username to match. If {@code null}, matches any username.
 * @param rolename role name to match. If {@code null}, matches any role.
 * @param service OGC service type to match (e.g., "WMS", "WFS", "WCS"). Service names are
 *     normalized to uppercase for case-insensitive matching. If {@code null}, matches any service.
 * @param request OGC request operation to match (e.g., "GetMap", "GetFeature"). Request names are
 *     normalized to uppercase for case-insensitive matching. If {@code null}, matches any request.
 * @param subfield request subfield to match for fine-grained filtering beyond service and request.
 *     In practice primarily used to match specific WPS process names (e.g., "geo:buffer",
 *     "vec:Reproject"); kept generic to allow future extensions to other services. If {@code
 *     null}, matches any subfield.
 * @param workspace GeoServer workspace name to match. If {@code null}, matches any workspace.
 * @param layer layer name to match (without workspace prefix). If {@code null}, matches any layer.
 *     When combined with {@code workspace}, identifies a specific layer (e.g., workspace="topp",
 *     layer="states").
 * @param addressRange IP address or CIDR range to match. Supports single IP addresses (e.g.,
 *     "192.168.1.100") or CIDR notation (e.g., "192.168.1.0/24"). If {@code null}, matches any IP
 *     address.
 * @since 1.0
 * @see Rule
 * @see GrantType
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record RuleIdentifier(
        GrantType access,
        @Nullable String username,
        @Nullable String rolename,
        @Nullable String service,
        @Nullable String request,
        @Nullable String subfield,
        @Nullable String workspace,
        @Nullable String layer,
        @Nullable String addressRange) {

    public RuleIdentifier {
        if (access == null) access = GrantType.DENY;
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        addNonNull(sb, "access", access);
        addNonNull(sb, "username", username);
        addNonNull(sb, "rolename", rolename);
        addNonNull(sb, "addressRange", addressRange);
        addNonNull(sb, "service", service);
        addNonNull(sb, "request", request);
        addNonNull(sb, "subfield", subfield);
        addNonNull(sb, "workspace", workspace);
        addNonNull(sb, "layer", layer);
        return sb.toString();
    }

    private void addNonNull(StringBuilder sb, String prop, @Nullable Object value) {
        if (null != value) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(prop).append(": ").append(value);
        }
    }
}
