package org.geoserver.acl.authorization;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.util.Set;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class User {

    private @NonNull String name;

    @Default private @NonNull Set<String> roles = Set.of();
}
