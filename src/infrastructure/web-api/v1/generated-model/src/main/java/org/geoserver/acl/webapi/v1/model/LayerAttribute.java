package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Access restrictions to a FeatureType attribute
 */
@Schema(name = "LayerAttribute", description = "Access restrictions to a FeatureType attribute")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class LayerAttribute {

    private String name;

    private @Nullable String dataType = null;

    /**
     * Gets or Sets access
     */
    public enum AccessEnum {
        NONE("NONE"),

        READONLY("READONLY"),

        READWRITE("READWRITE");

        private final String value;

        AccessEnum(String value) {
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
        public static AccessEnum fromValue(String value) {
            for (AccessEnum b : AccessEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            return null;
        }
    }

    private @Nullable AccessEnum access = null;

    public LayerAttribute() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public LayerAttribute(String name) {
        this.name = name;
    }

    public LayerAttribute name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     * @return name
     */
    @NotNull
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LayerAttribute dataType(@Nullable String dataType) {
        this.dataType = dataType;
        return this;
    }

    /**
     * Get dataType
     * @return dataType
     */
    @Schema(name = "dataType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("dataType")
    public @Nullable String getDataType() {
        return dataType;
    }

    public void setDataType(@Nullable String dataType) {
        this.dataType = dataType;
    }

    public LayerAttribute access(@Nullable AccessEnum access) {
        this.access = access;
        return this;
    }

    /**
     * Get access
     * @return access
     */
    @Schema(name = "access", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("access")
    public @Nullable AccessEnum getAccess() {
        return access;
    }

    public void setAccess(@Nullable AccessEnum access) {
        this.access = access;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LayerAttribute layerAttribute = (LayerAttribute) o;
        return Objects.equals(this.name, layerAttribute.name)
                && Objects.equals(this.dataType, layerAttribute.dataType)
                && Objects.equals(this.access, layerAttribute.access);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, access);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LayerAttribute {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
        sb.append("    access: ").append(toIndentedString(access)).append("\n");
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
