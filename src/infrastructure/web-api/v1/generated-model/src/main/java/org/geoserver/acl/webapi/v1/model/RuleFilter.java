package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Query filter for searching data access rules. All specified filter criteria must match for a rule to be included in results (AND logic). Omitted filters act as wildcards that match any value. Use this to find rules matching specific combinations of user, role, service, workspace, layer, etc.
 */
@Schema(
        name = "RuleFilter",
        description =
                "Query filter for searching data access rules. All specified filter criteria must match for a rule to be included in results (AND logic). Omitted filters act as wildcards that match any value. Use this to find rules matching specific combinations of user, role, service, workspace, layer, etc. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class RuleFilter {

    private @Nullable TextFilter user = null;

    private @Nullable SetFilter roles = null;

    private @Nullable TextFilter service = null;

    private @Nullable TextFilter request = null;

    private @Nullable TextFilter subfield = null;

    private @Nullable TextFilter workspace = null;

    private @Nullable TextFilter layer = null;

    private @Nullable AddressRangeFilter sourceAddress = null;

    public RuleFilter user(@Nullable TextFilter user) {
        this.user = user;
        return this;
    }

    /**
     * Get user
     * @return user
     */
    @Valid
    @Schema(name = "user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("user")
    public @Nullable TextFilter getUser() {
        return user;
    }

    public void setUser(@Nullable TextFilter user) {
        this.user = user;
    }

    public RuleFilter roles(@Nullable SetFilter roles) {
        this.roles = roles;
        return this;
    }

    /**
     * Get roles
     * @return roles
     */
    @Valid
    @Schema(name = "roles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("roles")
    public @Nullable SetFilter getRoles() {
        return roles;
    }

    public void setRoles(@Nullable SetFilter roles) {
        this.roles = roles;
    }

    public RuleFilter service(@Nullable TextFilter service) {
        this.service = service;
        return this;
    }

    /**
     * Get service
     * @return service
     */
    @Valid
    @Schema(name = "service", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("service")
    public @Nullable TextFilter getService() {
        return service;
    }

    public void setService(@Nullable TextFilter service) {
        this.service = service;
    }

    public RuleFilter request(@Nullable TextFilter request) {
        this.request = request;
        return this;
    }

    /**
     * Get request
     * @return request
     */
    @Valid
    @Schema(name = "request", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("request")
    public @Nullable TextFilter getRequest() {
        return request;
    }

    public void setRequest(@Nullable TextFilter request) {
        this.request = request;
    }

    public RuleFilter subfield(@Nullable TextFilter subfield) {
        this.subfield = subfield;
        return this;
    }

    /**
     * Get subfield
     * @return subfield
     */
    @Valid
    @Schema(name = "subfield", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("subfield")
    public @Nullable TextFilter getSubfield() {
        return subfield;
    }

    public void setSubfield(@Nullable TextFilter subfield) {
        this.subfield = subfield;
    }

    public RuleFilter workspace(@Nullable TextFilter workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * Get workspace
     * @return workspace
     */
    @Valid
    @Schema(name = "workspace", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspace")
    public @Nullable TextFilter getWorkspace() {
        return workspace;
    }

    public void setWorkspace(@Nullable TextFilter workspace) {
        this.workspace = workspace;
    }

    public RuleFilter layer(@Nullable TextFilter layer) {
        this.layer = layer;
        return this;
    }

    /**
     * Get layer
     * @return layer
     */
    @Valid
    @Schema(name = "layer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("layer")
    public @Nullable TextFilter getLayer() {
        return layer;
    }

    public void setLayer(@Nullable TextFilter layer) {
        this.layer = layer;
    }

    public RuleFilter sourceAddress(@Nullable AddressRangeFilter sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    /**
     * Get sourceAddress
     * @return sourceAddress
     */
    @Valid
    @Schema(name = "sourceAddress", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("sourceAddress")
    public @Nullable AddressRangeFilter getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(@Nullable AddressRangeFilter sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RuleFilter ruleFilter = (RuleFilter) o;
        return Objects.equals(this.user, ruleFilter.user)
                && Objects.equals(this.roles, ruleFilter.roles)
                && Objects.equals(this.service, ruleFilter.service)
                && Objects.equals(this.request, ruleFilter.request)
                && Objects.equals(this.subfield, ruleFilter.subfield)
                && Objects.equals(this.workspace, ruleFilter.workspace)
                && Objects.equals(this.layer, ruleFilter.layer)
                && Objects.equals(this.sourceAddress, ruleFilter.sourceAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, roles, service, request, subfield, workspace, layer, sourceAddress);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RuleFilter {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("    service: ").append(toIndentedString(service)).append("\n");
        sb.append("    request: ").append(toIndentedString(request)).append("\n");
        sb.append("    subfield: ").append(toIndentedString(subfield)).append("\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
        sb.append("    layer: ").append(toIndentedString(layer)).append("\n");
        sb.append("    sourceAddress: ").append(toIndentedString(sourceAddress)).append("\n");
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
