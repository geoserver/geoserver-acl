/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Request context evaluated by {@link AuthorizationService#getAccessInfo(AccessRequest)} and
 * {@link AuthorizationService#getMatchingRules(AccessRequest)} to produce a data access decision.
 *
 * <p>Identifies who is asking (user, roles), where the request originates (source address), what
 * operation is being performed (service, request, subfield), and which resource is targeted
 * (workspace, layer). The {@code "*"} wildcard is reserved as syntactic sugar at the API edge and
 * is not valid in a request submitted for evaluation; call {@link #validate()} to enforce that
 * constraint.
 *
 * @param user the authentication user name on behalf of which the request is performed;
 *     {@code null} for anonymous requests, in which case {@code roles} is expected to contain
 *     some role name with anonymous meaning (typically {@literal ROLE_ANONYMOUS}).
 * @param roles the authentication role names on behalf of which the request is performed; never
 *     {@code null}, defaults to an empty set.
 * @param sourceAddress the IP address the request originates from, used to match rules with an
 *     address-range constraint. {@code null} when the address is unknown or not applicable.
 * @param service the OWS service name being invoked (e.g., {@code WMS}, {@code WFS}, {@code WCS}).
 *     {@code null} matches rules that do not constrain the service.
 * @param request the OWS request name being invoked (e.g., {@code GetMap}, {@code GetFeature}).
 *     {@code null} matches rules that do not constrain the request.
 * @param subfield a finer-grained request qualifier beyond {@code service} and {@code request}.
 *     {@code null} matches rules that do not constrain the subfield.
 * @param workspace the name of the targeted workspace. {@code null} matches rules that do not
 *     constrain the workspace.
 * @param layer the name of the targeted layer. {@code null} matches rules that do not constrain
 *     the layer.
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record AccessRequest(
        @Nullable String user,
        Set<String> roles,
        @Nullable String sourceAddress,
        @Nullable String service,
        @Nullable String request,
        @Nullable String subfield,
        @Nullable String workspace,
        @Nullable String layer) {

    public AccessRequest {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /**
     * Verifies the request is fit for authorization evaluation: no field uses the {@code "*"}
     * wildcard placeholder that the API layer accepts as syntactic sugar.
     *
     * @throws IllegalStateException if any property contains a {@code "*"} wildcard
     * @return {@code this}
     */
    public AccessRequest validate() {
        checkNotAny("user", user);
        roles.forEach(role -> checkNotAny("roles", role));
        checkNotAny("sourceAddress", sourceAddress);
        checkNotAny("service", service);
        checkNotAny("request", request);
        checkNotAny("subfield", subfield);
        checkNotAny("workspace", workspace);
        checkNotAny("layer", layer);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccessRequest[");
        sb.append("user: ").append(user == null ? "" : user);
        sb.append(", roles: ").append(roles.stream().collect(Collectors.joining(",")));
        if (null != sourceAddress) sb.append(", origin IP: ").append(sourceAddress);
        if (null != service) sb.append(", service: ").append(service);
        if (null != request) sb.append(", request: ").append(request);
        if (null != subfield) sb.append(", subfield: ").append(subfield);
        if (null != workspace) sb.append(", workspace: ").append(workspace);
        if (null != layer) sb.append(", layer: ").append(layer);
        return sb.append("]").toString();
    }

    public static class Builder {
        @SuppressWarnings({"java:S1068", "java:S1450"})
        private Set<String> roles = Set.of();

        public Builder roles(String... roleNames) {
            return roles(roleNames.length == 0 ? Set.of() : Set.of(roleNames));
        }

        public Builder roles(@Nullable Set<String> roleNames) {
            this.roles = roleNames == null ? Set.of() : roleNames;
            return this;
        }
    }

    private static void checkNotAny(String prop, @Nullable String value) {
        if (null != value && "*".equals(value.trim())) {
            throw new IllegalStateException("AccessRequest.%s can't contain a * wildcard".formatted(prop));
        }
    }
}
