package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.lang.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Filter for matching against a set of possible values. Used for properties like roles where a rule might match any of several values.
 */
@Schema(
        name = "SetFilter",
        description =
                "Filter for matching against a set of possible values. Used for properties like roles where a rule might match any of several values. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class SetFilter {

    private @Nullable Boolean includeDefault = null;

    @Valid
    private @Nullable Set<String> values;

    public SetFilter includeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
        return this;
    }

    /**
     * When true, includes rules with null/wildcard values for this property. When false, only matches rules with explicit values from the provided set.
     * @return includeDefault
     */
    @Schema(
            name = "includeDefault",
            description =
                    "When true, includes rules with null/wildcard values for this property. When false, only matches rules with explicit values from the provided set. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("includeDefault")
    public @Nullable Boolean getIncludeDefault() {
        return includeDefault;
    }

    public void setIncludeDefault(@Nullable Boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    public SetFilter values(@Nullable Set<String> values) {
        this.values = values;
        return this;
    }

    public SetFilter addValuesItem(String valuesItem) {
        if (this.values == null) {
            this.values = new LinkedHashSet<>();
        }
        this.values.add(valuesItem);
        return this;
    }

    /**
     * Set of values to match. A rule matches if its property value is in this set.
     * @return values
     */
    @Schema(
            name = "values",
            example = "[\"ROLE_ADMIN\",\"ROLE_EDITOR\"]",
            description = "Set of values to match. A rule matches if its property value is in this set. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("values")
    public @Nullable Set<String> getValues() {
        return values;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setValues(@Nullable Set<String> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetFilter setFilter = (SetFilter) o;
        return Objects.equals(this.includeDefault, setFilter.includeDefault)
                && Objects.equals(this.values, setFilter.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeDefault, values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetFilter {\n");
        sb.append("    includeDefault: ")
                .append(toIndentedString(includeDefault))
                .append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
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
