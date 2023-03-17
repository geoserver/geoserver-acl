/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.model.rules;

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

    /** The GeoServer instance name this rule belongs to */
    private String instanceName;

    private String username;

    private String rolename;

    private String service;

    private String request;

    private String subfield;

    private String workspace;

    private String layer;

    private IPAddressRange addressRange;

    public String toShortString() {
        StringBuilder builder = new StringBuilder();
        addNonNull(builder, "access", access);
        addNonNull(builder, "instanceName", instanceName);
        addNonNull(builder, "username", username);
        addNonNull(builder, "rolename", rolename);
        addNonNull(builder, "addressRange", IPAddressRange.getCidrSignature(addressRange));
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

    public static class Builder {
        private IPAddressRange addressRange;

        public Builder addressRange(IPAddressRange range) {
            if (range != null
                    && range.getHigh() == null
                    && range.getLow() == null
                    && range.getSize() == null) {
                addressRange = null;
            } else {
                addressRange = range;
            }
            return this;
        }
    }
}
