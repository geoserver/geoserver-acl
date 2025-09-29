/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AdminAccessRequest {

    private String user;

    @NonNull
    private Set<String> roles;

    private String workspace;
    private String sourceAddress;

    /**
     * @throws IllegalStateException if a {@code *} wildcard is used in any property
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
        // Ignore squid:S1068, private field required for the lombok-generated build() method
        @SuppressWarnings("squid:S1068")
        private Set<String> roles = Set.of();

        public Builder roles(String... roleNames) {
            if (null == roleNames) return roles(Set.of());
            return roles(Set.of(roleNames));
        }

        public Builder roles(Set<String> roleNames) {
            if (null == roleNames) {
                this.roles = Set.of();
                return this;
            }
            this.roles = Set.copyOf(roleNames);
            return this;
        }
    }

    private void checkNotAny(String prop, String value) {
        if (null != value && "*".equals(value.trim())) {
            String msg =
                    String.format("%s.%s can't contain a * wildcard", getClass().getSimpleName(), prop);
            throw new IllegalStateException(msg);
        }
    }
}
