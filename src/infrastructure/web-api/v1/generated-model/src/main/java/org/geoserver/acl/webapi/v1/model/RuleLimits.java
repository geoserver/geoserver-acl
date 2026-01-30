package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Spatial and catalog restrictions applied when a rule&#39;s access type is LIMIT. Allows you to grant access to a resource while constraining what data can be accessed or how it appears.
 */
@Schema(
        name = "RuleLimits",
        description =
                "Spatial and catalog restrictions applied when a rule's access type is LIMIT. Allows you to grant access to a resource while constraining what data can be accessed or how it appears. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class RuleLimits {

    private @Nullable Geom allowedArea;

    private @Nullable SpatialFilterType spatialFilterType = null;

    private @Nullable CatalogMode catalogMode = null;

    public RuleLimits allowedArea(@Nullable Geom allowedArea) {
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

    public RuleLimits spatialFilterType(@Nullable SpatialFilterType spatialFilterType) {
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

    public RuleLimits catalogMode(@Nullable CatalogMode catalogMode) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RuleLimits ruleLimits = (RuleLimits) o;
        return Objects.equals(this.allowedArea, ruleLimits.allowedArea)
                && Objects.equals(this.spatialFilterType, ruleLimits.spatialFilterType)
                && Objects.equals(this.catalogMode, ruleLimits.catalogMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedArea, spatialFilterType, catalogMode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RuleLimits {\n");
        sb.append("    allowedArea: ").append(toIndentedString(allowedArea)).append("\n");
        sb.append("    spatialFilterType: ")
                .append(toIndentedString(spatialFilterType))
                .append("\n");
        sb.append("    catalogMode: ").append(toIndentedString(catalogMode)).append("\n");
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
