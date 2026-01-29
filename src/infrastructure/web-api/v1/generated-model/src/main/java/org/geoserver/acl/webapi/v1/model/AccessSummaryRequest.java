package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Request for a comprehensive summary of what resources a user can access. Used to build user interfaces that display available workspaces and layers based on the user&#39;s permissions.
 */
@Schema(
        name = "AccessSummaryRequest",
        description =
                "Request for a comprehensive summary of what resources a user can access. Used to build user interfaces that display available workspaces and layers based on the user's permissions. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AccessSummaryRequest {

    private String user;

    @Valid
    private Set<String> roles = new LinkedHashSet<>();

    public AccessSummaryRequest() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public AccessSummaryRequest(String user, Set<String> roles) {
        this.user = user;
        this.roles = roles;
    }

    public AccessSummaryRequest user(String user) {
        this.user = user;
        return this;
    }

    /**
     * The username to generate the access summary for.
     * @return user
     */
    @NotNull
    @Schema(
            name = "user",
            example = "viewer.user",
            description = "The username to generate the access summary for. ",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public AccessSummaryRequest roles(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public AccessSummaryRequest addRolesItem(String rolesItem) {
        if (this.roles == null) {
            this.roles = new LinkedHashSet<>();
        }
        this.roles.add(rolesItem);
        return this;
    }

    /**
     * The roles the user belongs to. Used to evaluate which rules apply to this user.
     * @return roles
     */
    @NotNull
    @Schema(
            name = "roles",
            example = "[\"ROLE_VIEWER\"]",
            description = "The roles the user belongs to. Used to evaluate which rules apply to this user. ",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("roles")
    public Set<String> getRoles() {
        return roles;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessSummaryRequest accessSummaryRequest = (AccessSummaryRequest) o;
        return Objects.equals(this.user, accessSummaryRequest.user)
                && Objects.equals(this.roles, accessSummaryRequest.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, roles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessSummaryRequest {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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
