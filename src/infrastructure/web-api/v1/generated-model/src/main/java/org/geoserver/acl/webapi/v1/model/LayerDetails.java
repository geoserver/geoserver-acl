package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.lang.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Detailed access control settings for a specific layer. These settings can only be configured for rules that specify a non-wildcarded workspace and layer name. LayerDetails allows you to define attribute-level permissions, CQL filters, spatial filters, allowed styles, and catalog mode.
 */
@Schema(
        name = "LayerDetails",
        description =
                "Detailed access control settings for a specific layer. These settings can only be configured for rules that specify a non-wildcarded workspace and layer name. LayerDetails allows you to define attribute-level permissions, CQL filters, spatial filters, allowed styles, and catalog mode. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class LayerDetails {

    /**
     * Gets or Sets type
     */
    public enum TypeEnum {
        VECTOR("VECTOR"),

        RASTER("RASTER"),

        LAYERGROUP("LAYERGROUP");

        private final String value;

        TypeEnum(String value) {
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
        public static TypeEnum fromValue(String value) {
            for (TypeEnum b : TypeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            return null;
        }
    }

    private @Nullable TypeEnum type = null;

    private @Nullable String defaultStyle = null;

    private @Nullable String cqlFilterRead = null;

    private @Nullable String cqlFilterWrite = null;

    private @Nullable Geom allowedArea;

    private @Nullable SpatialFilterType spatialFilterType = null;

    private @Nullable CatalogMode catalogMode = null;

    @Valid
    private @Nullable Set<String> allowedStyles;

    @Valid
    private @Nullable Set<@Valid LayerAttribute> layerAttributes;

    public LayerDetails type(@Nullable TypeEnum type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * @return type
     */
    @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("type")
    public @Nullable TypeEnum getType() {
        return type;
    }

    public void setType(@Nullable TypeEnum type) {
        this.type = type;
    }

    public LayerDetails defaultStyle(@Nullable String defaultStyle) {
        this.defaultStyle = defaultStyle;
        return this;
    }

    /**
     * Get defaultStyle
     * @return defaultStyle
     */
    @Schema(name = "defaultStyle", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("defaultStyle")
    public @Nullable String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(@Nullable String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public LayerDetails cqlFilterRead(@Nullable String cqlFilterRead) {
        this.cqlFilterRead = cqlFilterRead;
        return this;
    }

    /**
     * Get cqlFilterRead
     * @return cqlFilterRead
     */
    @Schema(name = "cqlFilterRead", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("cqlFilterRead")
    public @Nullable String getCqlFilterRead() {
        return cqlFilterRead;
    }

    public void setCqlFilterRead(@Nullable String cqlFilterRead) {
        this.cqlFilterRead = cqlFilterRead;
    }

    public LayerDetails cqlFilterWrite(@Nullable String cqlFilterWrite) {
        this.cqlFilterWrite = cqlFilterWrite;
        return this;
    }

    /**
     * Get cqlFilterWrite
     * @return cqlFilterWrite
     */
    @Schema(name = "cqlFilterWrite", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("cqlFilterWrite")
    public @Nullable String getCqlFilterWrite() {
        return cqlFilterWrite;
    }

    public void setCqlFilterWrite(@Nullable String cqlFilterWrite) {
        this.cqlFilterWrite = cqlFilterWrite;
    }

    public LayerDetails allowedArea(@Nullable Geom allowedArea) {
        this.allowedArea = allowedArea;
        return this;
    }

    /**
     * Get allowedArea
     * @return allowedArea
     */
    @Valid
    @Schema(name = "allowedArea", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("allowedArea")
    public @Nullable Geom getAllowedArea() {
        return allowedArea;
    }

    public void setAllowedArea(@Nullable Geom allowedArea) {
        this.allowedArea = allowedArea;
    }

    public LayerDetails spatialFilterType(@Nullable SpatialFilterType spatialFilterType) {
        this.spatialFilterType = spatialFilterType;
        return this;
    }

    /**
     * Get spatialFilterType
     * @return spatialFilterType
     */
    @Valid
    @Schema(name = "spatialFilterType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("spatialFilterType")
    public @Nullable SpatialFilterType getSpatialFilterType() {
        return spatialFilterType;
    }

    public void setSpatialFilterType(@Nullable SpatialFilterType spatialFilterType) {
        this.spatialFilterType = spatialFilterType;
    }

    public LayerDetails catalogMode(@Nullable CatalogMode catalogMode) {
        this.catalogMode = catalogMode;
        return this;
    }

    /**
     * Get catalogMode
     * @return catalogMode
     */
    @Valid
    @Schema(name = "catalogMode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("catalogMode")
    public @Nullable CatalogMode getCatalogMode() {
        return catalogMode;
    }

    public void setCatalogMode(@Nullable CatalogMode catalogMode) {
        this.catalogMode = catalogMode;
    }

    public LayerDetails allowedStyles(@Nullable Set<String> allowedStyles) {
        this.allowedStyles = allowedStyles;
        return this;
    }

    public LayerDetails addAllowedStylesItem(String allowedStylesItem) {
        if (this.allowedStyles == null) {
            this.allowedStyles = new LinkedHashSet<>();
        }
        this.allowedStyles.add(allowedStylesItem);
        return this;
    }

    /**
     * Get allowedStyles
     * @return allowedStyles
     */
    @Schema(name = "allowedStyles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("allowedStyles")
    public @Nullable Set<String> getAllowedStyles() {
        return allowedStyles;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setAllowedStyles(@Nullable Set<String> allowedStyles) {
        this.allowedStyles = allowedStyles;
    }

    public LayerDetails layerAttributes(@Nullable Set<@Valid LayerAttribute> layerAttributes) {
        this.layerAttributes = layerAttributes;
        return this;
    }

    public LayerDetails addLayerAttributesItem(LayerAttribute layerAttributesItem) {
        if (this.layerAttributes == null) {
            this.layerAttributes = new LinkedHashSet<>();
        }
        this.layerAttributes.add(layerAttributesItem);
        return this;
    }

    /**
     * Get layerAttributes
     * @return layerAttributes
     */
    @Valid
    @Schema(name = "layerAttributes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("layerAttributes")
    public @Nullable Set<@Valid LayerAttribute> getLayerAttributes() {
        return layerAttributes;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setLayerAttributes(@Nullable Set<@Valid LayerAttribute> layerAttributes) {
        this.layerAttributes = layerAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LayerDetails layerDetails = (LayerDetails) o;
        return Objects.equals(this.type, layerDetails.type)
                && Objects.equals(this.defaultStyle, layerDetails.defaultStyle)
                && Objects.equals(this.cqlFilterRead, layerDetails.cqlFilterRead)
                && Objects.equals(this.cqlFilterWrite, layerDetails.cqlFilterWrite)
                && Objects.equals(this.allowedArea, layerDetails.allowedArea)
                && Objects.equals(this.spatialFilterType, layerDetails.spatialFilterType)
                && Objects.equals(this.catalogMode, layerDetails.catalogMode)
                && Objects.equals(this.allowedStyles, layerDetails.allowedStyles)
                && Objects.equals(this.layerAttributes, layerDetails.layerAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                defaultStyle,
                cqlFilterRead,
                cqlFilterWrite,
                allowedArea,
                spatialFilterType,
                catalogMode,
                allowedStyles,
                layerAttributes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LayerDetails {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    defaultStyle: ").append(toIndentedString(defaultStyle)).append("\n");
        sb.append("    cqlFilterRead: ").append(toIndentedString(cqlFilterRead)).append("\n");
        sb.append("    cqlFilterWrite: ")
                .append(toIndentedString(cqlFilterWrite))
                .append("\n");
        sb.append("    allowedArea: ").append(toIndentedString(allowedArea)).append("\n");
        sb.append("    spatialFilterType: ")
                .append(toIndentedString(spatialFilterType))
                .append("\n");
        sb.append("    catalogMode: ").append(toIndentedString(catalogMode)).append("\n");
        sb.append("    allowedStyles: ").append(toIndentedString(allowedStyles)).append("\n");
        sb.append("    layerAttributes: ")
                .append(toIndentedString(layerAttributes))
                .append("\n");
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
