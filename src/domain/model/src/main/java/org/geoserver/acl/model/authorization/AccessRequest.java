/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.authorization;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.util.Set;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AccessRequest {

    public static String DEFAULT = null;
    public static String ANY = "*";

    @Default private String user = ANY;

    @NonNull private Set<String> roles;

    @Default private String instance = ANY;
    @Default private String service = ANY;
    @Default private String request = ANY;
    @Default private String subfield = ANY;
    @Default private String workspace = ANY;
    @Default private String layer = ANY;
    @Default private String sourceAddress = ANY;

    public static class Builder {
        private Set<String> roles = Set.of(ANY);

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
}
