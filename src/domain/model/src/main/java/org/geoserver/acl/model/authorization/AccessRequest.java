/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.authorization;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import org.geoserver.acl.model.filter.RuleFilter;

import java.util.Set;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AccessRequest {

    private User user;
    private @NonNull RuleFilter filter;

    public Set<String> userRoles() {
        return user == null ? Set.of() : user.getRoles();
    }

    public static AccessRequest of(User user, @NonNull RuleFilter filter) {
        return AccessRequest.builder().user(user).filter(filter).build();
    }
}
