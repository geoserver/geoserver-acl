package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Wkt
 */
@JsonTypeName("wkt")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class Wkt {

    private @Nullable String wkt;

    public Wkt wkt(@Nullable String wkt) {
        this.wkt = wkt;
        return this;
    }

    /**
     * Get wkt
     * @return wkt
     */
    @Schema(
            name = "wkt",
            example = "SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("wkt")
    public @Nullable String getWkt() {
        return wkt;
    }

    public void setWkt(@Nullable String wkt) {
        this.wkt = wkt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Wkt wkt = (Wkt) o;
        return Objects.equals(this.wkt, wkt.wkt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wkt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Wkt {\n");
        sb.append("    wkt: ").append(toIndentedString(wkt)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(@Nullable Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
