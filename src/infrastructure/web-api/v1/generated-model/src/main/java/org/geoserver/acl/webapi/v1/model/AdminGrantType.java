package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * The level of workspace administration access: - ADMIN: Full administrative access to the workspace (can modify configuration and content) - USER: Read-only user access to the workspace (can view but not modify)
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public enum AdminGrantType {
    ADMIN("ADMIN"),

    USER("USER");

    private final String value;

    AdminGrantType(String value) {
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
    public static AdminGrantType fromValue(String value) {
        for (AdminGrantType b : AdminGrantType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return null;
    }
}
