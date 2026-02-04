package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Gets or Sets SpatialFilterType
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public enum SpatialFilterType {
    INTERSECT("INTERSECT"),

    CLIP("CLIP");

    private final String value;

    SpatialFilterType(String value) {
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
    public static SpatialFilterType fromValue(String value) {
        for (SpatialFilterType b : SpatialFilterType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return null;
    }
}
