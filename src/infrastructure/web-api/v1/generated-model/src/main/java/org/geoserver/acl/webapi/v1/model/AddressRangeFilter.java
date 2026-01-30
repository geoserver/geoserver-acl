package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;

/**
 * Filter for matching IP address ranges. Matches rules whose address range includes or overlaps with the specified address range.
 */
@Schema(
        name = "AddressRangeFilter",
        description =
                "Filter for matching IP address ranges. Matches rules whose address range includes or overlaps with the specified address range. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AddressRangeFilter {

    private @Nullable Boolean includeDefault = null;

    private @Nullable String value = null;

    public AddressRangeFilter includeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
        return this;
    }

    /**
     * When true, includes rules with null/wildcard address ranges in the results.
     * @return includeDefault
     */
    @Schema(
            name = "includeDefault",
            description = "When true, includes rules with null/wildcard address ranges in the results. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("includeDefault")
    public @Nullable Boolean getIncludeDefault() {
        return includeDefault;
    }

    public void setIncludeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    public AddressRangeFilter value(@Nullable String value) {
        this.value = value;
        return this;
    }

    /**
     * IPv4 address range in CIDR notation. Can be a single IP address (e.g., \"192.168.1.1\") or a range using subnet mask notation (e.g., \"192.168.1.0/24\").
     * @return value
     */
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}(/([0-9]|[1-2][0-9]|3[0-2]))?$")
    @Schema(
            name = "value",
            example = "192.168.1.0/24",
            description =
                    "IPv4 address range in CIDR notation. Can be a single IP address (e.g., \"192.168.1.1\") or a range using subnet mask notation (e.g., \"192.168.1.0/24\"). ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("value")
    public @Nullable String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddressRangeFilter addressRangeFilter = (AddressRangeFilter) o;
        return Objects.equals(this.includeDefault, addressRangeFilter.includeDefault)
                && Objects.equals(this.value, addressRangeFilter.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeDefault, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AddressRangeFilter {\n");
        sb.append("    includeDefault: ")
                .append(toIndentedString(includeDefault))
                .append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
