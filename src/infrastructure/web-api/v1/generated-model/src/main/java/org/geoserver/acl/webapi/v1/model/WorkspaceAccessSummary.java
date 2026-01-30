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
 * Summary of a user&#39;s access to resources within a specific workspace.
 */
@Schema(
        name = "WorkspaceAccessSummary",
        description = "Summary of a user's access to resources within a specific workspace. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class WorkspaceAccessSummary {

    private @Nullable String workspace;

    private @Nullable AdminGrantType adminAccess = null;

    @Valid
    private @Nullable Set<String> allowed;

    @Valid
    private @Nullable Set<String> forbidden;

    public WorkspaceAccessSummary workspace(@Nullable String workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * The workspace name this summary applies to.
     * @return workspace
     */
    @Schema(
            name = "workspace",
            example = "topp",
            description = "The workspace name this summary applies to. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspace")
    public @Nullable String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(@Nullable String workspace) {
        this.workspace = workspace;
    }

    public WorkspaceAccessSummary adminAccess(@Nullable AdminGrantType adminAccess) {
        this.adminAccess = adminAccess;
        return this;
    }

    /**
     * Get adminAccess
     * @return adminAccess
     */
    @Valid
    @Schema(name = "adminAccess", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("adminAccess")
    public @Nullable AdminGrantType getAdminAccess() {
        return adminAccess;
    }

    public void setAdminAccess(@Nullable AdminGrantType adminAccess) {
        this.adminAccess = adminAccess;
    }

    public WorkspaceAccessSummary allowed(@Nullable Set<String> allowed) {
        this.allowed = allowed;
        return this;
    }

    public WorkspaceAccessSummary addAllowedItem(String allowedItem) {
        if (this.allowed == null) {
            this.allowed = new LinkedHashSet<>();
        }
        this.allowed.add(allowedItem);
        return this;
    }

    /**
     * Layer names in this workspace that the user can access, possibly under specific conditions (e.g., particular OWS service/request combinations). These result from ALLOW rules.
     * @return allowed
     */
    @Schema(
            name = "allowed",
            example = "[\"roads\",\"buildings\"]",
            description =
                    "Layer names in this workspace that the user can access, possibly under specific conditions (e.g., particular OWS service/request combinations). These result from ALLOW rules. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("allowed")
    public @Nullable Set<String> getAllowed() {
        return allowed;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setAllowed(@Nullable Set<String> allowed) {
        this.allowed = allowed;
    }

    public WorkspaceAccessSummary forbidden(@Nullable Set<String> forbidden) {
        this.forbidden = forbidden;
        return this;
    }

    public WorkspaceAccessSummary addForbiddenItem(String forbiddenItem) {
        if (this.forbidden == null) {
            this.forbidden = new LinkedHashSet<>();
        }
        this.forbidden.add(forbiddenItem);
        return this;
    }

    /**
     * Layer names in this workspace that the user definitely cannot access under any circumstances. These result from DENY rules. This complements the allowed list - there may be ALLOW rules for all layers in a workspace except specific layers listed here as forbidden.
     * @return forbidden
     */
    @Schema(
            name = "forbidden",
            example = "[\"sensitive_data\"]",
            description =
                    "Layer names in this workspace that the user definitely cannot access under any circumstances. These result from DENY rules. This complements the allowed list - there may be ALLOW rules for all layers in a workspace except specific layers listed here as forbidden. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("forbidden")
    public @Nullable Set<String> getForbidden() {
        return forbidden;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setForbidden(@Nullable Set<String> forbidden) {
        this.forbidden = forbidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkspaceAccessSummary workspaceAccessSummary = (WorkspaceAccessSummary) o;
        return Objects.equals(this.workspace, workspaceAccessSummary.workspace)
                && Objects.equals(this.adminAccess, workspaceAccessSummary.adminAccess)
                && Objects.equals(this.allowed, workspaceAccessSummary.allowed)
                && Objects.equals(this.forbidden, workspaceAccessSummary.forbidden);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspace, adminAccess, allowed, forbidden);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkspaceAccessSummary {\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
        sb.append("    adminAccess: ").append(toIndentedString(adminAccess)).append("\n");
        sb.append("    allowed: ").append(toIndentedString(allowed)).append("\n");
        sb.append("    forbidden: ").append(toIndentedString(forbidden)).append("\n");
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
