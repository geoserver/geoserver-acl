package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;

/**
 * Summary of what resources a user can access across all workspaces. For each workspace, lists the admin access level and which layers are allowed or forbidden.
 */
@Schema(
        name = "AccessSummary",
        description =
                "Summary of what resources a user can access across all workspaces. For each workspace, lists the admin access level and which layers are allowed or forbidden. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AccessSummary {

    @Valid
    private List<@Valid WorkspaceAccessSummary> workspaces = new ArrayList<>();

    public AccessSummary workspaces(List<@Valid WorkspaceAccessSummary> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    public AccessSummary addWorkspacesItem(WorkspaceAccessSummary workspacesItem) {
        if (this.workspaces == null) {
            this.workspaces = new ArrayList<>();
        }
        this.workspaces.add(workspacesItem);
        return this;
    }

    /**
     * Per-workspace access summary for the user.
     * @return workspaces
     */
    @Valid
    @Schema(
            name = "workspaces",
            description = "Per-workspace access summary for the user. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspaces")
    public List<@Valid WorkspaceAccessSummary> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<@Valid WorkspaceAccessSummary> workspaces) {
        this.workspaces = workspaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessSummary accessSummary = (AccessSummary) o;
        return Objects.equals(this.workspaces, accessSummary.workspaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaces);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessSummary {\n");
        sb.append("    workspaces: ").append(toIndentedString(workspaces)).append("\n");
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
