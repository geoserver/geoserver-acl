package org.geoserver.acl.authorization;

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
