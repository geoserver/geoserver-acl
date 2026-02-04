package org.geoserver.acl.webapi.v1.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geoserver.acl.webapi.v1.model.AccessInfo;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AccessSummary;
import org.geoserver.acl.webapi.v1.model.AccessSummaryRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessInfo;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestClientResponseException;

@jakarta.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        date = "2026-01-29T18:51:09.125560-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.17.0")
public class AuthorizationApi {
    private ApiClient apiClient;

    public AuthorizationApi() {
        this(new ApiClient());
    }

    public AuthorizationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Evaluate resource access permissions
     * Evaluates the data access rules for a specific request and returns the compiled access permissions. The response indicates whether access is granted, denied, or limited, along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Rules are evaluated in priority order until a matching rule determines the final access decision.
     * <p><b>200</b> - The result of evaluating access rules for a resource request. This response contains the final access decision (ALLOW, DENY, or LIMIT) along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Includes a list of rule IDs that were applied to reach this decision.
     * @param accessRequest The accessRequest parameter
     * @return AccessInfo
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAccessInfoRequestCreation(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        Object postBody = accessRequest;
        // verify the required parameter 'accessRequest' is set
        if (accessRequest == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'accessRequest' when calling getAccessInfo",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/authorization/resources",
                HttpMethod.POST,
                pathParams,
                queryParams,
                postBody,
                headerParams,
                cookieParams,
                formParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Evaluate resource access permissions
     * Evaluates the data access rules for a specific request and returns the compiled access permissions. The response indicates whether access is granted, denied, or limited, along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Rules are evaluated in priority order until a matching rule determines the final access decision.
     * <p><b>200</b> - The result of evaluating access rules for a resource request. This response contains the final access decision (ALLOW, DENY, or LIMIT) along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Includes a list of rule IDs that were applied to reach this decision.
     * @param accessRequest The accessRequest parameter
     * @return AccessInfo
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AccessInfo getAccessInfo(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        ParameterizedTypeReference<AccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAccessInfoRequestCreation(accessRequest).body(localVarReturnType);
    }

    /**
     * Evaluate resource access permissions
     * Evaluates the data access rules for a specific request and returns the compiled access permissions. The response indicates whether access is granted, denied, or limited, along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Rules are evaluated in priority order until a matching rule determines the final access decision.
     * <p><b>200</b> - The result of evaluating access rules for a resource request. This response contains the final access decision (ALLOW, DENY, or LIMIT) along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Includes a list of rule IDs that were applied to reach this decision.
     * @param accessRequest The accessRequest parameter
     * @return ResponseEntity&lt;AccessInfo&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AccessInfo> getAccessInfoWithHttpInfo(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        ParameterizedTypeReference<AccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAccessInfoRequestCreation(accessRequest).toEntity(localVarReturnType);
    }

    /**
     * Evaluate resource access permissions
     * Evaluates the data access rules for a specific request and returns the compiled access permissions. The response indicates whether access is granted, denied, or limited, along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Rules are evaluated in priority order until a matching rule determines the final access decision.
     * <p><b>200</b> - The result of evaluating access rules for a resource request. This response contains the final access decision (ALLOW, DENY, or LIMIT) along with any applicable restrictions such as spatial filters, CQL filters, allowed styles, and attribute-level permissions. Includes a list of rule IDs that were applied to reach this decision.
     * @param accessRequest The accessRequest parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAccessInfoWithResponseSpec(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        return getAccessInfoRequestCreation(accessRequest);
    }

    /**
     * Evaluate workspace administration access
     * Determines whether a user has administrative privileges on a specific workspace by evaluating the configured admin rules. Returns admin access information including whether the user has full administrative rights or only user-level access to the workspace.
     * <p><b>200</b> - The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the requested workspace and includes the ID of the matching admin rule.
     * @param adminAccessRequest The adminAccessRequest parameter
     * @return AdminAccessInfo
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAdminAuthorizationRequestCreation(
            @jakarta.annotation.Nonnull AdminAccessRequest adminAccessRequest) throws RestClientResponseException {
        Object postBody = adminAccessRequest;
        // verify the required parameter 'adminAccessRequest' is set
        if (adminAccessRequest == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'adminAccessRequest' when calling getAdminAuthorization",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AdminAccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/authorization/admin",
                HttpMethod.POST,
                pathParams,
                queryParams,
                postBody,
                headerParams,
                cookieParams,
                formParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Evaluate workspace administration access
     * Determines whether a user has administrative privileges on a specific workspace by evaluating the configured admin rules. Returns admin access information including whether the user has full administrative rights or only user-level access to the workspace.
     * <p><b>200</b> - The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the requested workspace and includes the ID of the matching admin rule.
     * @param adminAccessRequest The adminAccessRequest parameter
     * @return AdminAccessInfo
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminAccessInfo getAdminAuthorization(@jakarta.annotation.Nonnull AdminAccessRequest adminAccessRequest)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminAccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAdminAuthorizationRequestCreation(adminAccessRequest).body(localVarReturnType);
    }

    /**
     * Evaluate workspace administration access
     * Determines whether a user has administrative privileges on a specific workspace by evaluating the configured admin rules. Returns admin access information including whether the user has full administrative rights or only user-level access to the workspace.
     * <p><b>200</b> - The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the requested workspace and includes the ID of the matching admin rule.
     * @param adminAccessRequest The adminAccessRequest parameter
     * @return ResponseEntity&lt;AdminAccessInfo&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminAccessInfo> getAdminAuthorizationWithHttpInfo(
            @jakarta.annotation.Nonnull AdminAccessRequest adminAccessRequest) throws RestClientResponseException {
        ParameterizedTypeReference<AdminAccessInfo> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAdminAuthorizationRequestCreation(adminAccessRequest).toEntity(localVarReturnType);
    }

    /**
     * Evaluate workspace administration access
     * Determines whether a user has administrative privileges on a specific workspace by evaluating the configured admin rules. Returns admin access information including whether the user has full administrative rights or only user-level access to the workspace.
     * <p><b>200</b> - The result of evaluating admin rules for workspace access. Indicates whether the user has administrative privileges on the requested workspace and includes the ID of the matching admin rule.
     * @param adminAccessRequest The adminAccessRequest parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAdminAuthorizationWithResponseSpec(
            @jakarta.annotation.Nonnull AdminAccessRequest adminAccessRequest) throws RestClientResponseException {
        return getAdminAuthorizationRequestCreation(adminAccessRequest);
    }

    /**
     * Get rules that match an access request
     * Returns the list of data rules that match and would be applied when evaluating the given access request. This is useful for debugging and understanding which rules affect a particular request. Rules are returned in the order they would be evaluated (by priority).
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param accessRequest The accessRequest parameter
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMatchingRulesRequestCreation(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        Object postBody = accessRequest;
        // verify the required parameter 'accessRequest' is set
        if (accessRequest == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'accessRequest' when calling getMatchingRules",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/authorization/resources/matchingrules",
                HttpMethod.POST,
                pathParams,
                queryParams,
                postBody,
                headerParams,
                cookieParams,
                formParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get rules that match an access request
     * Returns the list of data rules that match and would be applied when evaluating the given access request. This is useful for debugging and understanding which rules affect a particular request. Rules are returned in the order they would be evaluated (by priority).
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param accessRequest The accessRequest parameter
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public List<Rule> getMatchingRules(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getMatchingRulesRequestCreation(accessRequest).body(localVarReturnType);
    }

    /**
     * Get rules that match an access request
     * Returns the list of data rules that match and would be applied when evaluating the given access request. This is useful for debugging and understanding which rules affect a particular request. Rules are returned in the order they would be evaluated (by priority).
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param accessRequest The accessRequest parameter
     * @return ResponseEntity&lt;List&lt;Rule&gt;&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<Rule>> getMatchingRulesWithHttpInfo(
            @jakarta.annotation.Nonnull AccessRequest accessRequest) throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getMatchingRulesRequestCreation(accessRequest).toEntity(localVarReturnType);
    }

    /**
     * Get rules that match an access request
     * Returns the list of data rules that match and would be applied when evaluating the given access request. This is useful for debugging and understanding which rules affect a particular request. Rules are returned in the order they would be evaluated (by priority).
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param accessRequest The accessRequest parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMatchingRulesWithResponseSpec(@jakarta.annotation.Nonnull AccessRequest accessRequest)
            throws RestClientResponseException {
        return getMatchingRulesRequestCreation(accessRequest);
    }

    /**
     * Get user access summary across workspaces
     * Provides a comprehensive summary of what resources a user can access across all workspaces. For each workspace, returns the admin access level and lists of allowed and forbidden layers. This is useful for building user interfaces that need to display what resources are available to a particular user.
     * <p><b>200</b> - The list of per-workspace access summary for a user
     * @param accessSummaryRequest The accessSummaryRequest parameter
     * @return AccessSummary
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserAccessSummaryRequestCreation(
            @jakarta.annotation.Nonnull AccessSummaryRequest accessSummaryRequest) throws RestClientResponseException {
        Object postBody = accessSummaryRequest;
        // verify the required parameter 'accessSummaryRequest' is set
        if (accessSummaryRequest == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'accessSummaryRequest' when calling getUserAccessSummary",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AccessSummary> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/authorization/accesssummary",
                HttpMethod.POST,
                pathParams,
                queryParams,
                postBody,
                headerParams,
                cookieParams,
                formParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get user access summary across workspaces
     * Provides a comprehensive summary of what resources a user can access across all workspaces. For each workspace, returns the admin access level and lists of allowed and forbidden layers. This is useful for building user interfaces that need to display what resources are available to a particular user.
     * <p><b>200</b> - The list of per-workspace access summary for a user
     * @param accessSummaryRequest The accessSummaryRequest parameter
     * @return AccessSummary
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AccessSummary getUserAccessSummary(@jakarta.annotation.Nonnull AccessSummaryRequest accessSummaryRequest)
            throws RestClientResponseException {
        ParameterizedTypeReference<AccessSummary> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getUserAccessSummaryRequestCreation(accessSummaryRequest).body(localVarReturnType);
    }

    /**
     * Get user access summary across workspaces
     * Provides a comprehensive summary of what resources a user can access across all workspaces. For each workspace, returns the admin access level and lists of allowed and forbidden layers. This is useful for building user interfaces that need to display what resources are available to a particular user.
     * <p><b>200</b> - The list of per-workspace access summary for a user
     * @param accessSummaryRequest The accessSummaryRequest parameter
     * @return ResponseEntity&lt;AccessSummary&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AccessSummary> getUserAccessSummaryWithHttpInfo(
            @jakarta.annotation.Nonnull AccessSummaryRequest accessSummaryRequest) throws RestClientResponseException {
        ParameterizedTypeReference<AccessSummary> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getUserAccessSummaryRequestCreation(accessSummaryRequest).toEntity(localVarReturnType);
    }

    /**
     * Get user access summary across workspaces
     * Provides a comprehensive summary of what resources a user can access across all workspaces. For each workspace, returns the admin access level and lists of allowed and forbidden layers. This is useful for building user interfaces that need to display what resources are available to a particular user.
     * <p><b>200</b> - The list of per-workspace access summary for a user
     * @param accessSummaryRequest The accessSummaryRequest parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserAccessSummaryWithResponseSpec(
            @jakarta.annotation.Nonnull AccessSummaryRequest accessSummaryRequest) throws RestClientResponseException {
        return getUserAccessSummaryRequestCreation(accessSummaryRequest);
    }
}
