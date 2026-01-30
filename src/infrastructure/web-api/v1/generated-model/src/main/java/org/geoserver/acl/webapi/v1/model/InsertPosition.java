package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Controls how the rule's priority value is interpreted when creating a new rule: - FIXED: Use the exact priority value specified - FROM_START: Interpret priority as position from the beginning (0 = first) - FROM_END: Interpret priority as position from the end (0 = last)
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public enum InsertPosition {
    FIXED("FIXED"),

    FROM_START("FROM_START"),

    FROM_END("FROM_END");

    private final String value;

    InsertPosition(String value) {
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
    public static InsertPosition fromValue(String value) {
        for (InsertPosition b : InsertPosition.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
