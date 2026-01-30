package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;

/**
 * The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the workspace.
 */
@Schema(
        name = "AdminAccessInfo",
        description =
                "The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the workspace. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AdminAccessInfo {

    private Boolean admin;

    private @Nullable String workspace;

    private @Nullable String matchingAdminRule;

    public AdminAccessInfo() {
        super();
    }

    /**
     * Constructor with only required parameters
     */
    public AdminAccessInfo(Boolean admin) {
        this.admin = admin;
    }

    public AdminAccessInfo admin(Boolean admin) {
        this.admin = admin;
        return this;
    }

    /**
     * True if the user has administrative access to the workspace, false otherwise.
     * @return admin
     */
    @NotNull
    @Schema(
            name = "admin",
            example = "true",
            description = "True if the user has administrative access to the workspace, false otherwise. ",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("admin")
    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public AdminAccessInfo workspace(@Nullable String workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * The workspace this admin access decision applies to.
     * @return workspace
     */
    @Schema(
            name = "workspace",
            example = "cartography",
            description = "The workspace this admin access decision applies to. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspace")
    public @Nullable String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(@Nullable String workspace) {
        this.workspace = workspace;
    }

    public AdminAccessInfo matchingAdminRule(@Nullable String matchingAdminRule) {
        this.matchingAdminRule = matchingAdminRule;
        return this;
    }

    /**
     * ID of the admin rule that determined this access decision. Useful for debugging and auditing.
     * @return matchingAdminRule
     */
    @Schema(
            name = "matchingAdminRule",
            description =
                    "ID of the admin rule that determined this access decision. Useful for debugging and auditing. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("matchingAdminRule")
    public @Nullable String getMatchingAdminRule() {
        return matchingAdminRule;
    }

    public void setMatchingAdminRule(@Nullable String matchingAdminRule) {
        this.matchingAdminRule = matchingAdminRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdminAccessInfo adminAccessInfo = (AdminAccessInfo) o;
        return Objects.equals(this.admin, adminAccessInfo.admin)
                && Objects.equals(this.workspace, adminAccessInfo.workspace)
                && Objects.equals(this.matchingAdminRule, adminAccessInfo.matchingAdminRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(admin, workspace, matchingAdminRule);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AdminAccessInfo {\n");
        sb.append("    admin: ").append(toIndentedString(admin)).append("\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
        sb.append("    matchingAdminRule: ")
                .append(toIndentedString(matchingAdminRule))
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
