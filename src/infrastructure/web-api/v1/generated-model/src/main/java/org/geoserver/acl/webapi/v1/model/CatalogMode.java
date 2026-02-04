package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;

/**
 * Gets or Sets CatalogMode
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public enum CatalogMode {
    HIDE("HIDE"),

    CHALLENGE("CHALLENGE"),

    MIXED("MIXED");

    private final String value;

    CatalogMode(String value) {
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
    public static CatalogMode fromValue(String value) {
        for (CatalogMode b : CatalogMode.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        return null;
    }
}
