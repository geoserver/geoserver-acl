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

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class RuleIdentifier {

    @NonNull @Default private GrantType access = GrantType.DENY;

    private String username;

    private String rolename;

    private String service;

    private String request;

    private String subfield;

    private String workspace;

    private String layer;

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
