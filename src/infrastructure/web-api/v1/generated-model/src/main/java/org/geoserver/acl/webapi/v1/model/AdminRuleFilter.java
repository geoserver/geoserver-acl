package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * Query filter for searching workspace admin rules. All specified filter criteria must match for a rule to be included in results (AND logic). Omitted filters act as wildcards that match any value.
 */
@Schema(
        name = "AdminRuleFilter",
        description =
                "Query filter for searching workspace admin rules. All specified filter criteria must match for a rule to be included in results (AND logic). Omitted filters act as wildcards that match any value. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AdminRuleFilter {

    private @Nullable AdminGrantType grantType = null;

    private @Nullable TextFilter user = null;

    private @Nullable SetFilter roles = null;

    private @Nullable TextFilter workspace = null;

    private @Nullable AddressRangeFilter sourceAddress = null;

    public AdminRuleFilter grantType(@Nullable AdminGrantType grantType) {
        this.grantType = grantType;
        return this;
    }

    /**
     * Get grantType
     * @return grantType
     */
    @Valid
    @Schema(name = "grantType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("grantType")
    public @Nullable AdminGrantType getGrantType() {
        return grantType;
    }

    public void setGrantType(@Nullable AdminGrantType grantType) {
        this.grantType = grantType;
    }

    public AdminRuleFilter user(@Nullable TextFilter user) {
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

    public AdminRuleFilter roles(@Nullable SetFilter roles) {
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

    public AdminRuleFilter workspace(@Nullable TextFilter workspace) {
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

    public AdminRuleFilter sourceAddress(@Nullable AddressRangeFilter sourceAddress) {
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
        AdminRuleFilter adminRuleFilter = (AdminRuleFilter) o;
        return Objects.equals(this.grantType, adminRuleFilter.grantType)
                && Objects.equals(this.user, adminRuleFilter.user)
                && Objects.equals(this.roles, adminRuleFilter.roles)
                && Objects.equals(this.workspace, adminRuleFilter.workspace)
                && Objects.equals(this.sourceAddress, adminRuleFilter.sourceAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grantType, user, roles, workspace, sourceAddress);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminRuleFilter {\n");
        sb.append("    grantType: ").append(toIndentedString(grantType)).append("\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
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
