/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import java.util.Set;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Request object for {@link AuthorizationService#getUserAccessSummary}.
 *
 * @param user the authentication user name on behalf of which the request is performed,
 *     {@code null} only if the request is anonymous, in which case {@code roles} is expected to
 *     contain some role name with anonymous meaning (typically {@literal ROLE_ANONYMOUS}).
 * @param roles the authentication role names on behalf of which the request is performed.
 * @since 2.3
 * @see WorkspaceAccessSummary
 * @see AuthorizationService#getUserAccessSummary(AccessSummaryRequest)
 */
@Builder(builderClassName = "Builder")
public record AccessSummaryRequest(@Nullable String user, Set<String> roles) {

    public AccessSummaryRequest {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        if (user == null && roles.isEmpty()) {
            throw new IllegalStateException(
                    "AccessSummaryRequest requires user and roles not to be null or empty at the same time. Got user: "
                            + user
                            + ", roles: "
                            + roles);
        }
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
}
