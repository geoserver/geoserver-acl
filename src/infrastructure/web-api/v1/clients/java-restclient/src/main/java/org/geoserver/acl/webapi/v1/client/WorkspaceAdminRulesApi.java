package org.geoserver.acl.webapi.v1.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geoserver.acl.webapi.v1.model.AdminRule;
import org.geoserver.acl.webapi.v1.model.AdminRuleFilter;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
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
public class WorkspaceAdminRulesApi {
    private ApiClient apiClient;

    public WorkspaceAdminRulesApi() {
        this(new ApiClient());
    }

    public WorkspaceAdminRulesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     *
     * Returns whether the AdminRule with the given identifier exists
     * <p><b>200</b> - boolean indicating whether the admin rule with the provided identifier exists
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec adminRuleExistsByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling adminRuleExistsById",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/id/{id}/exists",
                HttpMethod.GET,
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
     *
     * Returns whether the AdminRule with the given identifier exists
     * <p><b>200</b> - boolean indicating whether the admin rule with the provided identifier exists
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Boolean adminRuleExistsById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {};
        return adminRuleExistsByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     *
     * Returns whether the AdminRule with the given identifier exists
     * <p><b>200</b> - boolean indicating whether the admin rule with the provided identifier exists
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Boolean> adminRuleExistsByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {};
        return adminRuleExistsByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     *
     * Returns whether the AdminRule with the given identifier exists
     * <p><b>200</b> - boolean indicating whether the admin rule with the provided identifier exists
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec adminRuleExistsByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return adminRuleExistsByIdRequestCreation(id);
    }

    /**
     *
     * Returns the number of rules that matches the search criteria
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec countAdminRulesRequestCreation(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        Object postBody = adminRuleFilter;
        // verify the required parameter 'adminRuleFilter' is set
        if (adminRuleFilter == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'adminRuleFilter' when calling countAdminRules",
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

        final String[] localVarAccepts = {"application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/query/count",
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
     *
     * Returns the number of rules that matches the search criteria
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer countAdminRules(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAdminRulesRequestCreation(adminRuleFilter).body(localVarReturnType);
    }

    /**
     *
     * Returns the number of rules that matches the search criteria
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> countAdminRulesWithHttpInfo(
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter) throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAdminRulesRequestCreation(adminRuleFilter).toEntity(localVarReturnType);
    }

    /**
     *
     * Returns the number of rules that matches the search criteria
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec countAdminRulesWithResponseSpec(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        return countAdminRulesRequestCreation(adminRuleFilter);
    }

    /**
     *
     * Returns the total number of rules
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec countAllAdminRulesRequestCreation() throws RestClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/query/count",
                HttpMethod.GET,
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
     *
     * Returns the total number of rules
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer countAllAdminRules() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAllAdminRulesRequestCreation().body(localVarReturnType);
    }

    /**
     *
     * Returns the total number of rules
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> countAllAdminRulesWithHttpInfo() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAllAdminRulesRequestCreation().toEntity(localVarReturnType);
    }

    /**
     *
     * Returns the total number of rules
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec countAllAdminRulesWithResponseSpec() throws RestClientResponseException {
        return countAllAdminRulesRequestCreation();
    }

    /**
     *
     *
     * <p><b>201</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * @param adminRule The adminRule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createAdminRuleRequestCreation(
            @jakarta.annotation.Nonnull AdminRule adminRule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        Object postBody = adminRule;
        // verify the required parameter 'adminRule' is set
        if (adminRule == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'adminRule' when calling createAdminRule",
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

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "position", position));

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules",
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
     *
     *
     * <p><b>201</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * @param adminRule The adminRule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminRule createAdminRule(
            @jakarta.annotation.Nonnull AdminRule adminRule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return createAdminRuleRequestCreation(adminRule, position).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>201</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * @param adminRule The adminRule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return ResponseEntity&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminRule> createAdminRuleWithHttpInfo(
            @jakarta.annotation.Nonnull AdminRule adminRule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return createAdminRuleRequestCreation(adminRule, position).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>201</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * @param adminRule The adminRule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createAdminRuleWithResponseSpec(
            @jakarta.annotation.Nonnull AdminRule adminRule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        return createAdminRuleRequestCreation(adminRule, position);
    }

    /**
     *
     *
     * <p><b>200</b> - OK
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteAdminRuleByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling deleteAdminRuleById",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/id/{id}",
                HttpMethod.DELETE,
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
     *
     *
     * <p><b>200</b> - OK
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void deleteAdminRuleById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        deleteAdminRuleByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - OK
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> deleteAdminRuleByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteAdminRuleByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - OK
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteAdminRuleByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return deleteAdminRuleByIdRequestCreation(id);
    }

    /**
     *
     * Atomically deletes all admin rules and return the number of rules removed
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteAllAdminRulesRequestCreation() throws RestClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules",
                HttpMethod.DELETE,
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
     *
     * Atomically deletes all admin rules and return the number of rules removed
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer deleteAllAdminRules() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteAllAdminRulesRequestCreation().body(localVarReturnType);
    }

    /**
     *
     * Atomically deletes all admin rules and return the number of rules removed
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> deleteAllAdminRulesWithHttpInfo() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteAllAdminRulesRequestCreation().toEntity(localVarReturnType);
    }

    /**
     *
     * Atomically deletes all admin rules and return the number of rules removed
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteAllAdminRulesWithResponseSpec() throws RestClientResponseException {
        return deleteAllAdminRulesRequestCreation();
    }

    /**
     *
     *
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return List&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec findAdminRulesRequestCreation(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        Object postBody = adminRuleFilter;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nextCursor", nextCursor));

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/query",
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
     *
     *
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return List&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public List<AdminRule> findAdminRules(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findAdminRulesRequestCreation(limit, nextCursor, adminRuleFilter).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseEntity&lt;List&lt;AdminRule&gt;&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<AdminRule>> findAdminRulesWithHttpInfo(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findAdminRulesRequestCreation(limit, nextCursor, adminRuleFilter).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec findAdminRulesWithResponseSpec(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        return findAdminRulesRequestCreation(limit, nextCursor, adminRuleFilter);
    }

    /**
     *
     * Returns an (optionally) paginated list of admin rules.
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return List&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec findAllAdminRulesRequestCreation(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nextCursor", nextCursor));

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules",
                HttpMethod.GET,
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
     *
     * Returns an (optionally) paginated list of admin rules.
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return List&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public List<AdminRule> findAllAdminRules(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findAllAdminRulesRequestCreation(limit, nextCursor).body(localVarReturnType);
    }

    /**
     *
     * Returns an (optionally) paginated list of admin rules.
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return ResponseEntity&lt;List&lt;AdminRule&gt;&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<AdminRule>> findAllAdminRulesWithHttpInfo(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<AdminRule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findAllAdminRulesRequestCreation(limit, nextCursor).toEntity(localVarReturnType);
    }

    /**
     *
     * Returns an (optionally) paginated list of admin rules.
     * <p><b>200</b> - A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec findAllAdminRulesWithResponseSpec(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        return findAllAdminRulesRequestCreation(limit, nextCursor);
    }

    /**
     *
     * Finds the first rule that satisfies the query criteria
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec findFirstAdminRuleRequestCreation(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        Object postBody = adminRuleFilter;
        // verify the required parameter 'adminRuleFilter' is set
        if (adminRuleFilter == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'adminRuleFilter' when calling findFirstAdminRule",
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

        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/query/first",
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
     *
     * Finds the first rule that satisfies the query criteria
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminRule findFirstAdminRule(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findFirstAdminRuleRequestCreation(adminRuleFilter).body(localVarReturnType);
    }

    /**
     *
     * Finds the first rule that satisfies the query criteria
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseEntity&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminRule> findFirstAdminRuleWithHttpInfo(
            @jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter) throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findFirstAdminRuleRequestCreation(adminRuleFilter).toEntity(localVarReturnType);
    }

    /**
     *
     * Finds the first rule that satisfies the query criteria
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param adminRuleFilter The adminRuleFilter parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec findFirstAdminRuleWithResponseSpec(@jakarta.annotation.Nullable AdminRuleFilter adminRuleFilter)
            throws RestClientResponseException {
        return findFirstAdminRuleRequestCreation(adminRuleFilter);
    }

    /**
     *
     * Finds the AdminRule with the given priority
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec findOneAdminRuleByPriorityRequestCreation(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'priority' is set
        if (priority == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'priority' when calling findOneAdminRuleByPriority",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("priority", priority);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/query/one/priority/{priority}",
                HttpMethod.GET,
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
     *
     * Finds the AdminRule with the given priority
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminRule findOneAdminRuleByPriority(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findOneAdminRuleByPriorityRequestCreation(priority).body(localVarReturnType);
    }

    /**
     *
     * Finds the AdminRule with the given priority
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return ResponseEntity&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminRule> findOneAdminRuleByPriorityWithHttpInfo(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findOneAdminRuleByPriorityRequestCreation(priority).toEntity(localVarReturnType);
    }

    /**
     *
     * Finds the AdminRule with the given priority
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec findOneAdminRuleByPriorityWithResponseSpec(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        return findOneAdminRuleByPriorityRequestCreation(priority);
    }

    /**
     *
     * Returns the AdminRule with the given identifier
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAdminRuleByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling getAdminRuleById",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/id/{id}",
                HttpMethod.GET,
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
     *
     * Returns the AdminRule with the given identifier
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminRule getAdminRuleById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAdminRuleByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     *
     * Returns the AdminRule with the given identifier
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseEntity&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminRule> getAdminRuleByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getAdminRuleByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     *
     * Returns the AdminRule with the given identifier
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAdminRuleByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return getAdminRuleByIdRequestCreation(id);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of admin rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec shiftAdminRulesByPriorityRequestCreation(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'priorityStart' is set
        if (priorityStart == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'priorityStart' when calling shiftAdminRulesByPriority",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'offset' is set
        if (offset == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'offset' when calling shiftAdminRulesByPriority",
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

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "priorityStart", priorityStart));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "offset", offset));

        final String[] localVarAccepts = {"application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/shift",
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
     *
     *
     * <p><b>200</b> - The number of admin rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer shiftAdminRulesByPriority(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return shiftAdminRulesByPriorityRequestCreation(priorityStart, offset).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of admin rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> shiftAdminRulesByPriorityWithHttpInfo(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return shiftAdminRulesByPriorityRequestCreation(priorityStart, offset).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of admin rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec shiftAdminRulesByPriorityWithResponseSpec(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        return shiftAdminRulesByPriorityRequestCreation(priorityStart, offset);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The admin rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec swapAdminRulesRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling swapAdminRules",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'id2' is set
        if (id2 == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id2' when calling swapAdminRules",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);
        pathParams.put("id2", id2);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/id/{id}/swapwith/{id2}",
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
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The admin rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void swapAdminRules(@jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        swapAdminRulesRequestCreation(id, id2).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The admin rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> swapAdminRulesWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return swapAdminRulesRequestCreation(id, id2).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The admin rule identifier to swap priorities with
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec swapAdminRulesWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        return swapAdminRulesRequestCreation(id, id2);
    }

    /**
     *
     *
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Bad request body
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, tried to update the rule identifier properties to one that belongs to another rule
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param adminRule The adminRule parameter
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateAdminRuleRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull AdminRule adminRule)
            throws RestClientResponseException {
        Object postBody = adminRule;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling updateAdminRule",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'adminRule' is set
        if (adminRule == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'adminRule' when calling updateAdminRule",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/adminrules/id/{id}",
                HttpMethod.PATCH,
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
     *
     *
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Bad request body
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, tried to update the rule identifier properties to one that belongs to another rule
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param adminRule The adminRule parameter
     * @return AdminRule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public AdminRule updateAdminRule(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull AdminRule adminRule)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return updateAdminRuleRequestCreation(id, adminRule).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Bad request body
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, tried to update the rule identifier properties to one that belongs to another rule
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param adminRule The adminRule parameter
     * @return ResponseEntity&lt;AdminRule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AdminRule> updateAdminRuleWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull AdminRule adminRule)
            throws RestClientResponseException {
        ParameterizedTypeReference<AdminRule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return updateAdminRuleRequestCreation(id, adminRule).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Bad request body
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, tried to update the rule identifier properties to one that belongs to another rule
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param adminRule The adminRule parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateAdminRuleWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull AdminRule adminRule)
            throws RestClientResponseException {
        return updateAdminRuleRequestCreation(id, adminRule);
    }
}
