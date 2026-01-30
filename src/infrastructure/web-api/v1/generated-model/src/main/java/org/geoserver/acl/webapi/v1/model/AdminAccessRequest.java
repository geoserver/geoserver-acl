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
 * Represents a request to determine administrative access to a workspace. This is used to evaluate whether a user has admin privileges on a specific workspace, which allows them to modify workspace configuration and content.
 */
@Schema(
        name = "AdminAccessRequest",
        description =
                "Represents a request to determine administrative access to a workspace. This is used to evaluate whether a user has admin privileges on a specific workspace, which allows them to modify workspace configuration and content. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AdminAccessRequest {

    private @Nullable String user;

    @Valid
    private @Nullable Set<String> roles;

    private String sourceAddress = "*";

    private String workspace = "*";

    public AdminAccessRequest user(@Nullable String user) {
        this.user = user;
        return this;
    }

    /**
     * The authenticated username making the admin access request.
     * @return user
     */
    @Schema(
            name = "user",
            example = "admin.user",
            description = "The authenticated username making the admin access request. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("user")
    public @Nullable String getUser() {
        return user;
    }

    public void setUser(@Nullable String user) {
        this.user = user;
    }

    public AdminAccessRequest roles(@Nullable Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public AdminAccessRequest addRolesItem(String rolesItem) {
        if (this.roles == null) {
            this.roles = new LinkedHashSet<>();
        }
        this.roles.add(rolesItem);
        return this;
    }

    /**
     * The roles the user belongs to. Admin rules are matched against these roles.
     * @return roles
     */
    @Schema(
            name = "roles",
            example = "[\"ROLE_ADMIN\"]",
            description = "The roles the user belongs to. Admin rules are matched against these roles. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("roles")
    public @Nullable Set<String> getRoles() {
        return roles;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setRoles(@Nullable Set<String> roles) {
        this.roles = roles;
    }

    public AdminAccessRequest sourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    /**
     * The IP address the request originates from.
     * @return sourceAddress
     */
    @Schema(
            name = "sourceAddress",
            example = "192.168.1.50",
            description = "The IP address the request originates from. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("sourceAddress")
    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public AdminAccessRequest workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * The workspace for which admin access is being evaluated. Wildcard '*' evaluates across all workspaces.
     * @return workspace
     */
    @Schema(
            name = "workspace",
            example = "cartography",
            description =
                    "The workspace for which admin access is being evaluated. Wildcard '*' evaluates across all workspaces. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspace")
    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminAccessRequest adminAccessRequest = (AdminAccessRequest) o;
        return Objects.equals(this.user, adminAccessRequest.user)
                && Objects.equals(this.roles, adminAccessRequest.roles)
                && Objects.equals(this.sourceAddress, adminAccessRequest.sourceAddress)
                && Objects.equals(this.workspace, adminAccessRequest.workspace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, roles, sourceAddress, workspace);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminAccessRequest {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("    sourceAddress: ").append(toIndentedString(sourceAddress)).append("\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
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
