package org.geoserver.acl.webapi.v1.server;

import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Optional;
import org.geoserver.acl.webapi.v1.model.AccessInfo;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AccessSummary;
import org.geoserver.acl.webapi.v1.model.AccessSummaryRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessInfo;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link AuthorizationApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:51:14.805992-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.17.0")
public interface AuthorizationApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * POST /authorization/resources : Evaluate resource access permissions
     * Evaluates the data access rules for a specific request and returns the compiled access permissions. The response indicates whether access is granted, denied, or limited, along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Rules are evaluated in priority order until a matching rule determines the final access decision.
     *
     * @param accessRequest  (required)
     * @return The result of evaluating access rules for a resource request. This response contains the final access decision (ALLOW, DENY, or LIMIT) along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Includes a list of rule IDs that were applied to reach this decision.  (status code 200)
     * @see AuthorizationApi#getAccessInfo
     */
    default ResponseEntity<AccessInfo> getAccessInfo(AccessRequest accessRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"area\" : { \"wkt\" : \"SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))\" }, \"cqlFilterWrite\" : \"owner = 'current_user'\", \"cqlFilterRead\" : \"status = 'public'\", \"defaultStyle\" : \"default_style\", \"allowedStyles\" : [ \"basic\", \"detailed\" ], \"catalogMode\" : \"HIDE\", \"attributes\" : [ { \"access\" : \"NONE\", \"dataType\" : \"dataType\", \"name\" : \"name\" }, { \"access\" : \"NONE\", \"dataType\" : \"dataType\", \"name\" : \"name\" } ], \"grant\" : \"ALLOW\", \"matchingRules\" : [ \"matchingRules\", \"matchingRules\" ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/x-jackson-smile"))) {
                    String exampleString = "Custom MIME type example not yet supported: application/x-jackson-smile";
                    ApiUtil.setExampleResponse(request, "application/x-jackson-smile", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /authorization/admin : Evaluate workspace administration access
     * Determines whether a user has administrative privileges on a specific workspace by evaluating the configured admin rules. Returns admin access information including whether the user has full administrative rights or only user-level access to the workspace.
     *
     * @param adminAccessRequest  (required)
     * @return The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the requested workspace and includes the ID of the matching admin rule.  (status code 200)
     * @see AuthorizationApi#getAdminAuthorization
     */
    default ResponseEntity<AdminAccessInfo> getAdminAuthorization(AdminAccessRequest adminAccessRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"cartography\", \"admin\" : true, \"matchingAdminRule\" : \"matchingAdminRule\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/x-jackson-smile"))) {
                    String exampleString = "Custom MIME type example not yet supported: application/x-jackson-smile";
                    ApiUtil.setExampleResponse(request, "application/x-jackson-smile", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /authorization/resources/matchingrules : Get rules that match an access request
     * Returns the list of data rules that match and would be applied when evaluating the given access request. This is useful for debugging and understanding which rules affect a particular request. Rules are returned in the order they would be evaluated (by priority).
     *
     * @param accessRequest  (required)
     * @return A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.  (status code 200)
     * @see AuthorizationApi#getMatchingRules
     */
    default ResponseEntity<List<Rule>> getMatchingRules(AccessRequest accessRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "[ { \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" }, { \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /authorization/accesssummary : Get user access summary across workspaces
     * Provides a comprehensive summary of what resources a user can access across all workspaces. For each workspace, returns the admin access level and lists of allowed and forbidden layers. This is useful for building user interfaces that need to display what resources are available to a particular user.
     *
     * @param accessSummaryRequest  (required)
     * @return The list of per-workspace access summary for a user (status code 200)
     * @see AuthorizationApi#getUserAccessSummary
     */
    default ResponseEntity<AccessSummary> getUserAccessSummary(AccessSummaryRequest accessSummaryRequest) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspaces\" : [ { \"workspace\" : \"topp\", \"adminAccess\" : \"ADMIN\", \"allowed\" : [ \"roads\", \"buildings\" ], \"forbidden\" : [ \"sensitive_data\" ] }, { \"workspace\" : \"topp\", \"adminAccess\" : \"ADMIN\", \"allowed\" : [ \"roads\", \"buildings\" ], \"forbidden\" : [ \"sensitive_data\" ] } ] }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/x-jackson-smile"))) {
                    String exampleString = "Custom MIME type example not yet supported: application/x-jackson-smile";
                    ApiUtil.setExampleResponse(request, "application/x-jackson-smile", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
