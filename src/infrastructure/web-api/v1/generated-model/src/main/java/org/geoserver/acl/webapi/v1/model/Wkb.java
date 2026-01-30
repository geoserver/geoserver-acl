package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Wkb
 */
@JsonTypeName("wkb")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class Wkb {

    private @Nullable byte[] wkb;

    public Wkb wkb(@Nullable byte[] wkb) {
        this.wkb = wkb;
        return this;
    }

    /**
     * Get wkb
     * @return wkb
     */
    @Schema(name = "wkb", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("wkb")
    public @Nullable byte[] getWkb() {
        return wkb;
    }

    public void setWkb(@Nullable byte[] wkb) {
        this.wkb = wkb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Wkb wkb = (Wkb) o;
        return Arrays.equals(this.wkb, wkb.wkb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(wkb));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Wkb {\n");
        sb.append("    wkb: ").append(toIndentedString(wkb)).append("\n");
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
