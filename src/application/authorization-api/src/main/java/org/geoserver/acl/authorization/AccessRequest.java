/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.util.Set;
import java.util.stream.Collectors;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AccessRequest {

    private String user;

    @NonNull private Set<String> roles;

    private String sourceAddress;

    private String service;
    private String request;
    private String subfield;
    private String workspace;
    private String layer;

    /**
     * @throws IllegalStateException if a {@code *} wildcard is used in any property
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

    public @Override String toString() {
        return String.format(
                "%s[from:%s, by: %s(%s), for:%s:%s%s, layer:%s%s]",
                getClass().getSimpleName(),
                sourceAddress == null ? "<no IP>" : sourceAddress,
                user,
                roles.stream().collect(Collectors.joining(", ")),
                service,
                request,
                subfield == null ? "" : "(" + subfield + ")",
                workspace == null ? "<null ws>" : (workspace.isEmpty() ? "" : workspace + ":"),
                layer);
    }

    public static class Builder {
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
                    String.format(
                            "%s.%s can't contain a * wildcard", getClass().getSimpleName(), prop);
            throw new IllegalStateException(msg);
        }
    }
}
