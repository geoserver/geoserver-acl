/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import java.util.Set;
import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Request context evaluated by
 * {@link AuthorizationService#getAdminAuthorization(AdminAccessRequest)} to decide whether a user
 * holds administrative privileges on a workspace.
 *
 * <p>Identifies the calling principal (user, roles), the origin of the request (source address),
 * and the workspace whose administrative rights are being queried. The {@code "*"} wildcard is
 * reserved as syntactic sugar at the API edge and is not valid in a request submitted for
 * evaluation; call {@link #validate()} to enforce that constraint.
 *
 * @param user the authentication user name on behalf of which the request is performed;
 *     {@code null} for anonymous requests, in which case {@code roles} is expected to contain
 *     some role name with anonymous meaning (typically {@literal ROLE_ANONYMOUS}).
 * @param roles the authentication role names on behalf of which the request is performed; never
 *     {@code null}, defaults to an empty set.
 * @param workspace the name of the workspace whose administrative rights are being queried.
 *     {@code null} matches rules that do not constrain the workspace.
 * @param sourceAddress the IP address the request originates from, used to match rules with an
 *     address-range constraint. {@code null} when the address is unknown or not applicable.
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record AdminAccessRequest(
        @Nullable String user, Set<String> roles, @Nullable String workspace, @Nullable String sourceAddress) {

    public AdminAccessRequest {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /**
     * Verifies the request is fit for authorization evaluation: no field uses the {@code "*"}
     * wildcard placeholder that the API layer accepts as syntactic sugar.
     *
     * @throws IllegalStateException if any property contains a {@code "*"} wildcard
     * @return {@code this}
     */
    public AdminAccessRequest validate() {
        checkNotAny("user", user);
        checkNotAny("workspace", workspace);
        checkNotAny("sourceAddress", sourceAddress);
        roles.forEach(role -> checkNotAny("roles", role));
        return this;
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
            throw new IllegalStateException("AdminAccessRequest.%s can't contain a * wildcard".formatted(prop));
        }
    }
}
