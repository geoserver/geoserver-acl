package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Filter for matching text properties. Supports exact matching and wildcard behavior. The matching logic is: - If value is null or absent: matches rules where the property is set to any value or is null (wildcard) - If value is \&quot;*\&quot;: matches only rules where the property is null (explicitly wildcard) - Otherwise: matches rules where the property equals the specified value
 */
@Schema(
        name = "TextFilter",
        description =
                "Filter for matching text properties. Supports exact matching and wildcard behavior. The matching logic is: - If value is null or absent: matches rules where the property is set to any value or is null (wildcard) - If value is \"*\": matches only rules where the property is null (explicitly wildcard) - Otherwise: matches rules where the property equals the specified value ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class TextFilter {

    private @Nullable Boolean includeDefault = null;

    private @Nullable String value = null;

    public TextFilter includeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
        return this;
    }

    /**
     * When true, includes rules with null/wildcard values for this property in the results. When false, only matches rules with an explicit value.
     * @return includeDefault
     */
    @Schema(
            name = "includeDefault",
            description =
                    "When true, includes rules with null/wildcard values for this property in the results. When false, only matches rules with an explicit value. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("includeDefault")
    public @Nullable Boolean getIncludeDefault() {
        return includeDefault;
    }

    public void setIncludeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    public TextFilter value(@Nullable String value) {
        this.value = value;
        return this;
    }

    /**
     * The value to match. Use \"*\" to explicitly match wildcard rules, null/absent to match any value.
     * @return value
     */
    @Schema(
            name = "value",
            example = "WMS",
            description =
                    "The value to match. Use \"*\" to explicitly match wildcard rules, null/absent to match any value. ",
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
        TextFilter textFilter = (TextFilter) o;
        return Objects.equals(this.includeDefault, textFilter.includeDefault)
                && Objects.equals(this.value, textFilter.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeDefault, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TextFilter {\n");
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
