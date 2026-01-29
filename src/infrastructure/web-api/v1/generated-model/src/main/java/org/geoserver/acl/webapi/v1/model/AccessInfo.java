package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * The result of evaluating access rules for a resource request. Contains the final access decision (ALLOW, DENY, or LIMIT) and any restrictions that apply. When access is LIMIT, additional properties define what data can be accessed and how it should be filtered or restricted.
 */
@Schema(
        name = "AccessInfo",
        description =
                "The result of evaluating access rules for a resource request. Contains the final access decision (ALLOW, DENY, or LIMIT) and any restrictions that apply. When access is LIMIT, additional properties define what data can be accessed and how it should be filtered or restricted. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AccessInfo {

    private GrantType grant = null;

    private @Nullable Geom area;

    private @Nullable Geom clipArea;

    private @Nullable CatalogMode catalogMode = null;

    private @Nullable String defaultStyle;

    @Valid
    private Set<String> allowedStyles = new LinkedHashSet<>();

    private @Nullable String cqlFilterRead;

    private @Nullable String cqlFilterWrite;

    @Valid
    private @Nullable Set<@Valid LayerAttribute> attributes;

    @Valid
    private List<String> matchingRules = new ArrayList<>();

    public AccessInfo() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public AccessInfo(GrantType grant) {
        this.grant = grant;
    }

    public AccessInfo grant(GrantType grant) {
        this.grant = grant;
        return this;
    }

    /**
     * Get grant
     * @return grant
     */
    @NotNull
    @Valid
    @Schema(name = "grant", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("grant")
    public GrantType getGrant() {
        return grant;
    }

    public void setGrant(GrantType grant) {
        this.grant = grant;
    }

    public AccessInfo area(@Nullable Geom area) {
        this.area = area;
        return this;
    }

    /**
     * Get area
     * @return area
     */
    @Valid
    @Schema(name = "area", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("area")
    public @Nullable Geom getArea() {
        return area;
    }

    public void setArea(@Nullable Geom area) {
        this.area = area;
    }

    public AccessInfo clipArea(@Nullable Geom clipArea) {
        this.clipArea = clipArea;
        return this;
    }

    /**
     * Get clipArea
     * @return clipArea
     */
    @Valid
    @Schema(name = "clipArea", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("clipArea")
    public @Nullable Geom getClipArea() {
        return clipArea;
    }

    public void setClipArea(@Nullable Geom clipArea) {
        this.clipArea = clipArea;
    }

    public AccessInfo catalogMode(@Nullable CatalogMode catalogMode) {
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

    public AccessInfo defaultStyle(@Nullable String defaultStyle) {
        this.defaultStyle = defaultStyle;
        return this;
    }

    /**
     * The default style to use when rendering this layer.
     * @return defaultStyle
     */
    @Schema(
            name = "defaultStyle",
            example = "default_style",
            description = "The default style to use when rendering this layer. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("defaultStyle")
    public @Nullable String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(@Nullable String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public AccessInfo allowedStyles(Set<String> allowedStyles) {
        this.allowedStyles = allowedStyles;
        return this;
    }

    public AccessInfo addAllowedStylesItem(String allowedStylesItem) {
        if (this.allowedStyles == null) {
            this.allowedStyles = new LinkedHashSet<>();
        }
        this.allowedStyles.add(allowedStylesItem);
        return this;
    }

    /**
     * List of style names the user is permitted to request. If present, only these styles can be used.
     * @return allowedStyles
     */
    @Schema(
            name = "allowedStyles",
            example = "[\"basic\",\"detailed\"]",
            description =
                    "List of style names the user is permitted to request. If present, only these styles can be used. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("allowedStyles")
    public Set<String> getAllowedStyles() {
        return allowedStyles;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setAllowedStyles(Set<String> allowedStyles) {
        this.allowedStyles = allowedStyles;
    }

    public AccessInfo cqlFilterRead(@Nullable String cqlFilterRead) {
        this.cqlFilterRead = cqlFilterRead;
        return this;
    }

    /**
     * CQL filter expression applied when reading features. Only features matching this filter are visible.
     * @return cqlFilterRead
     */
    @Schema(
            name = "cqlFilterRead",
            example = "status = 'public'",
            description =
                    "CQL filter expression applied when reading features. Only features matching this filter are visible. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("cqlFilterRead")
    public @Nullable String getCqlFilterRead() {
        return cqlFilterRead;
    }

    public void setCqlFilterRead(@Nullable String cqlFilterRead) {
        this.cqlFilterRead = cqlFilterRead;
    }

    public AccessInfo cqlFilterWrite(@Nullable String cqlFilterWrite) {
        this.cqlFilterWrite = cqlFilterWrite;
        return this;
    }

    /**
     * CQL filter expression applied when writing/modifying features. Only features matching this filter can be modified.
     * @return cqlFilterWrite
     */
    @Schema(
            name = "cqlFilterWrite",
            example = "owner = 'current_user'",
            description =
                    "CQL filter expression applied when writing/modifying features. Only features matching this filter can be modified. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("cqlFilterWrite")
    public @Nullable String getCqlFilterWrite() {
        return cqlFilterWrite;
    }

    public void setCqlFilterWrite(@Nullable String cqlFilterWrite) {
        this.cqlFilterWrite = cqlFilterWrite;
    }

    public AccessInfo attributes(@Nullable Set<@Valid LayerAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public AccessInfo addAttributesItem(LayerAttribute attributesItem) {
        if (this.attributes == null) {
            this.attributes = new LinkedHashSet<>();
        }
        this.attributes.add(attributesItem);
        return this;
    }

    /**
     * Attribute-level access control. Specifies which feature attributes are accessible and their access level.
     * @return attributes
     */
    @Valid
    @Schema(
            name = "attributes",
            description =
                    "Attribute-level access control. Specifies which feature attributes are accessible and their access level. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("attributes")
    public @Nullable Set<@Valid LayerAttribute> getAttributes() {
        return attributes;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setAttributes(@Nullable Set<@Valid LayerAttribute> attributes) {
        this.attributes = attributes;
    }

    public AccessInfo matchingRules(List<String> matchingRules) {
        this.matchingRules = matchingRules;
        return this;
    }

    public AccessInfo addMatchingRulesItem(String matchingRulesItem) {
        if (this.matchingRules == null) {
            this.matchingRules = new ArrayList<>();
        }
        this.matchingRules.add(matchingRulesItem);
        return this;
    }

    /**
     * IDs of the rules that were evaluated and applied to produce this access decision. Useful for debugging and auditing.
     * @return matchingRules
     */
    @Schema(
            name = "matchingRules",
            description =
                    "IDs of the rules that were evaluated and applied to produce this access decision. Useful for debugging and auditing. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("matchingRules")
    public List<String> getMatchingRules() {
        return matchingRules;
    }

    public void setMatchingRules(List<String> matchingRules) {
        this.matchingRules = matchingRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessInfo accessInfo = (AccessInfo) o;
        return Objects.equals(this.grant, accessInfo.grant)
                && Objects.equals(this.area, accessInfo.area)
                && Objects.equals(this.clipArea, accessInfo.clipArea)
                && Objects.equals(this.catalogMode, accessInfo.catalogMode)
                && Objects.equals(this.defaultStyle, accessInfo.defaultStyle)
                && Objects.equals(this.allowedStyles, accessInfo.allowedStyles)
                && Objects.equals(this.cqlFilterRead, accessInfo.cqlFilterRead)
                && Objects.equals(this.cqlFilterWrite, accessInfo.cqlFilterWrite)
                && Objects.equals(this.attributes, accessInfo.attributes)
                && Objects.equals(this.matchingRules, accessInfo.matchingRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                grant,
                area,
                clipArea,
                catalogMode,
                defaultStyle,
                allowedStyles,
                cqlFilterRead,
                cqlFilterWrite,
                attributes,
                matchingRules);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessInfo {\n");
        sb.append("    grant: ").append(toIndentedString(grant)).append("\n");
        sb.append("    area: ").append(toIndentedString(area)).append("\n");
        sb.append("    clipArea: ").append(toIndentedString(clipArea)).append("\n");
        sb.append("    catalogMode: ").append(toIndentedString(catalogMode)).append("\n");
        sb.append("    defaultStyle: ").append(toIndentedString(defaultStyle)).append("\n");
        sb.append("    allowedStyles: ").append(toIndentedString(allowedStyles)).append("\n");
        sb.append("    cqlFilterRead: ").append(toIndentedString(cqlFilterRead)).append("\n");
        sb.append("    cqlFilterWrite: ")
                .append(toIndentedString(cqlFilterWrite))
                .append("\n");
        sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
        sb.append("    matchingRules: ").append(toIndentedString(matchingRules)).append("\n");
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
