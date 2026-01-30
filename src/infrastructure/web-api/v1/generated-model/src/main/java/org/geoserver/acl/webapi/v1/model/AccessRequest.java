package org.geoserver.acl.webapi.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.lang.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Represents a request to access a GeoServer resource. This is used to evaluate what permissions a specific user has for a given operation on a workspace/layer combination. The system matches this request against configured rules to determine the access decision and any applicable restrictions.  Properties default to wildcards (&#39;*&#39;) which match any value when evaluating rules.
 */
@Schema(
        name = "AccessRequest",
        description =
                "Represents a request to access a GeoServer resource. This is used to evaluate what permissions a specific user has for a given operation on a workspace/layer combination. The system matches this request against configured rules to determine the access decision and any applicable restrictions.  Properties default to wildcards ('*') which match any value when evaluating rules. ")
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:56:08.226482-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.19.0")
public class AccessRequest {

    private @Nullable String user;

    @Valid
    private @Nullable Set<String> roles;

    private String sourceAddress = "*";

    private String service = "*";

    private String request = "*";

    private String subfield = "*";

    private String workspace = "*";

    private String layer = "*";

    public AccessRequest user(@Nullable String user) {
        this.user = user;
        return this;
    }

    /**
     * The authenticated username making the request. This is matched against rule user filters.
     * @return user
     */
    @Schema(
            name = "user",
            example = "john.doe",
            description = "The authenticated username making the request. This is matched against rule user filters. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("user")
    public @Nullable String getUser() {
        return user;
    }

    public void setUser(@Nullable String user) {
        this.user = user;
    }

    public AccessRequest roles(@Nullable Set<String> roles) {
        this.roles = roles;
        return this;
    }

    public AccessRequest addRolesItem(String rolesItem) {
        if (this.roles == null) {
            this.roles = new LinkedHashSet<>();
        }
        this.roles.add(rolesItem);
        return this;
    }

    /**
     * The roles the user belongs to. A rule matches if the user has any of the rule's specified roles.
     * @return roles
     */
    @Schema(
            name = "roles",
            example = "[\"ROLE_VIEWER\",\"ROLE_EDITOR\"]",
            description =
                    "The roles the user belongs to. A rule matches if the user has any of the rule's specified roles. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("roles")
    public @Nullable Set<String> getRoles() {
        return roles;
    }

    @JsonDeserialize(as = LinkedHashSet.class)
    public void setRoles(@Nullable Set<String> roles) {
        this.roles = roles;
    }

    public AccessRequest sourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    /**
     * The IP address the request originates from. Matched against rule address range filters.
     * @return sourceAddress
     */
    @Schema(
            name = "sourceAddress",
            example = "192.168.1.100",
            description = "The IP address the request originates from. Matched against rule address range filters. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("sourceAddress")
    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public AccessRequest service(String service) {
        this.service = service;
        return this;
    }

    /**
     * The OWS service type (WMS, WFS, WCS, WPS, etc.). Wildcard '*' matches any service.
     * @return service
     */
    @Schema(
            name = "service",
            example = "WMS",
            description = "The OWS service type (WMS, WFS, WCS, WPS, etc.). Wildcard '*' matches any service. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("service")
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public AccessRequest request(String request) {
        this.request = request;
        return this;
    }

    /**
     * The OWS operation being requested (GetMap, GetFeature, GetCoverage, etc.). Wildcard '*' matches any operation.
     * @return request
     */
    @Schema(
            name = "request",
            example = "GetMap",
            description =
                    "The OWS operation being requested (GetMap, GetFeature, GetCoverage, etc.). Wildcard '*' matches any operation. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("request")
    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public AccessRequest subfield(String subfield) {
        this.subfield = subfield;
        return this;
    }

    /**
     * Additional operation-specific qualifier for fine-grained control. Wildcard '*' matches any subfield.
     * @return subfield
     */
    @Schema(
            name = "subfield",
            description =
                    "Additional operation-specific qualifier for fine-grained control. Wildcard '*' matches any subfield. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("subfield")
    public String getSubfield() {
        return subfield;
    }

    public void setSubfield(String subfield) {
        this.subfield = subfield;
    }

    public AccessRequest workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    /**
     * The workspace being accessed. Wildcard '*' matches any workspace.
     * @return workspace
     */
    @Schema(
            name = "workspace",
            example = "topp",
            description = "The workspace being accessed. Wildcard '*' matches any workspace. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("workspace")
    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public AccessRequest layer(String layer) {
        this.layer = layer;
        return this;
    }

    /**
     * The layer being accessed within the workspace. Wildcard '*' matches any layer.
     * @return layer
     */
    @Schema(
            name = "layer",
            example = "roads",
            description = "The layer being accessed within the workspace. Wildcard '*' matches any layer. ",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("layer")
    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessRequest accessRequest = (AccessRequest) o;
        return Objects.equals(this.user, accessRequest.user)
                && Objects.equals(this.roles, accessRequest.roles)
                && Objects.equals(this.sourceAddress, accessRequest.sourceAddress)
                && Objects.equals(this.service, accessRequest.service)
                && Objects.equals(this.request, accessRequest.request)
                && Objects.equals(this.subfield, accessRequest.subfield)
                && Objects.equals(this.workspace, accessRequest.workspace)
                && Objects.equals(this.layer, accessRequest.layer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, roles, sourceAddress, service, request, subfield, workspace, layer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccessRequest {\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
        sb.append("    sourceAddress: ").append(toIndentedString(sourceAddress)).append("\n");
        sb.append("    service: ").append(toIndentedString(service)).append("\n");
        sb.append("    request: ").append(toIndentedString(request)).append("\n");
        sb.append("    subfield: ").append(toIndentedString(subfield)).append("\n");
        sb.append("    workspace: ").append(toIndentedString(workspace)).append("\n");
        sb.append("    layer: ").append(toIndentedString(layer)).append("\n");
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
