package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * The access decision for this rule: - ALLOW: Grant full access to the matched resource - DENY: Completely deny access to the matched resource - LIMIT: Grant access but apply restrictions defined in RuleLimits or LayerDetails
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public enum GrantType {
    ALLOW("ALLOW"),

    DENY("DENY"),

    LIMIT("LIMIT");

    private final String value;

    GrantType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static GrantType fromValue(String value) {
        for (GrantType b : GrantType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return null;
    }
}
