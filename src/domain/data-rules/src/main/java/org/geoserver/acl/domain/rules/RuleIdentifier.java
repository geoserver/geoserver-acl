/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.domain.rules;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

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
 * @since 1.0
 * @see Rule
 * @see GrantType
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class RuleIdentifier {

    /** The access grant type (ALLOW, DENY, or LIMIT). Never null; defaults to DENY. */
    @NonNull
    @Default
    private GrantType access = GrantType.DENY;

    /** Username to match. If {@code null}, matches any username. */
    private String username;

    /** Role name to match. If {@code null}, matches any role. */
    private String rolename;

    /**
     * OGC service type to match (e.g., "WMS", "WFS", "WCS").
     *
     * <p>Service names are normalized to uppercase for case-insensitive matching. If {@code null},
     * matches any service.
     */
    private String service;

    /**
     * OGC request operation to match (e.g., "GetMap", "GetFeature").
     *
     * <p>Request names are normalized to uppercase for case-insensitive matching. If {@code null},
     * matches any request.
     */
    private String request;

    /**
     * Request subfield to match for fine-grained filtering beyond service and request.
     *
     * <p>In practice, this field is primarily used to match specific <b>WPS process names</b>
     * (e.g., "geo:buffer", "vec:Reproject"). For example, you can create rules that allow access
     * to specific WPS processes while denying others within the WPS service.
     *
     * <p>The field is kept generic to allow for future extensions to other services that may
     * require similar fine-grained filtering capabilities beyond the service/request level.
     *
     * <p>If {@code null}, matches any subfield (all processes, or any future subfield values).
     *
     * <p><b>Example for WPS:</b>
     *
     * <pre>{@code
     * // Allow only specific WPS process
     * RuleIdentifier wpsProcessRule = RuleIdentifier.builder()
     *     .access(ALLOW)
     *     .service("WPS")
     *     .request("Execute")
     *     .subfield("geo:buffer")  // Only allow the buffer process
     *     .build();
     * }</pre>
     */
    private String subfield;

    /**
     * GeoServer workspace name to match.
     *
     * <p>If {@code null}, matches any workspace. Rules can target specific workspaces to control
     * access at the workspace level.
     */
    private String workspace;

    /**
     * Layer name to match (without workspace prefix).
     *
     * <p>If {@code null}, matches any layer. When combined with {@code workspace}, identifies a
     * specific layer (e.g., workspace="topp", layer="states").
     */
    private String layer;

    /**
     * IP address or CIDR range to match.
     *
     * <p>Supports single IP addresses (e.g., "192.168.1.100") or CIDR notation (e.g.,
     * "192.168.1.0/24"). If {@code null}, matches any IP address.
     */
    private String addressRange;

    public String toShortString() {
        StringBuilder builder = new StringBuilder();
        addNonNull(builder, "access", access);
        addNonNull(builder, "username", username);
        addNonNull(builder, "rolename", rolename);
        addNonNull(builder, "addressRange", addressRange);
        addNonNull(builder, "service", service);
        addNonNull(builder, "request", request);
        addNonNull(builder, "subfield", subfield);
        addNonNull(builder, "workspace", workspace);
        addNonNull(builder, "layer", layer);
        return builder.toString();
    }

    private void addNonNull(StringBuilder builder, String prop, Object value) {
        if (null != value) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(prop).append(": ").append(value);
        }
    }
}
