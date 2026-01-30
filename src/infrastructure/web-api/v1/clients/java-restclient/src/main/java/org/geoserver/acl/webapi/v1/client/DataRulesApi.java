package org.geoserver.acl.webapi.v1.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.geoserver.acl.webapi.v1.model.LayerDetails;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.geoserver.acl.webapi.v1.model.RuleFilter;
import org.geoserver.acl.webapi.v1.model.RuleLimits;
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
public class DataRulesApi {
    private ApiClient apiClient;

    public DataRulesApi() {
        this(new ApiClient());
    }

    public DataRulesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Count all data rules
     * Returns the total number of data access rules currently configured in the system.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec countAllRulesRequestCreation() throws RestClientResponseException {
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
                "/rules/query/count",
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
     * Count all data rules
     * Returns the total number of data access rules currently configured in the system.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer countAllRules() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAllRulesRequestCreation().body(localVarReturnType);
    }

    /**
     * Count all data rules
     * Returns the total number of data access rules currently configured in the system.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> countAllRulesWithHttpInfo() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countAllRulesRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Count all data rules
     * Returns the total number of data access rules currently configured in the system.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec countAllRulesWithResponseSpec() throws RestClientResponseException {
        return countAllRulesRequestCreation();
    }

    /**
     * Count rules matching filter criteria
     * Returns the number of data rules that match the provided filter criteria. Useful for determining result set size before paginating through query results.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param ruleFilter The ruleFilter parameter
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec countRulesRequestCreation(@jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        Object postBody = ruleFilter;
        // verify the required parameter 'ruleFilter' is set
        if (ruleFilter == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'ruleFilter' when calling countRules",
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
                "/rules/query/count",
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
     * Count rules matching filter criteria
     * Returns the number of data rules that match the provided filter criteria. Useful for determining result set size before paginating through query results.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param ruleFilter The ruleFilter parameter
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer countRules(@jakarta.annotation.Nullable RuleFilter ruleFilter) throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countRulesRequestCreation(ruleFilter).body(localVarReturnType);
    }

    /**
     * Count rules matching filter criteria
     * Returns the number of data rules that match the provided filter criteria. Useful for determining result set size before paginating through query results.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param ruleFilter The ruleFilter parameter
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> countRulesWithHttpInfo(@jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return countRulesRequestCreation(ruleFilter).toEntity(localVarReturnType);
    }

    /**
     * Count rules matching filter criteria
     * Returns the number of data rules that match the provided filter criteria. Useful for determining result set size before paginating through query results.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @param ruleFilter The ruleFilter parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec countRulesWithResponseSpec(@jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        return countRulesRequestCreation(ruleFilter);
    }

    /**
     * Create a new data rule
     * Creates a new data access rule. The rule will be inserted at the specified priority position. If a rule with the same identifier (same combination of user, role, service, request, workspace, layer, etc.) already exists, the operation will fail with a 409 Conflict.  The priority determines evaluation order - lower numbers are evaluated first. Use the position parameter to control how the priority value is interpreted (fixed value, from start, or from end of the list).
     * <p><b>201</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request. Common causes include: - Providing a rule with a non-null id (id is auto-generated) - Invalid field values or combinations
     * <p><b>409</b> - A rule with the same identifier already exists. Rules are uniquely identified by the combination of: user, role, service, request, subfield, workspace, layer, and addressRange. If you need to modify an existing rule, use the PATCH endpoint instead.
     * @param rule The rule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createRuleRequestCreation(
            @jakarta.annotation.Nonnull Rule rule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        Object postBody = rule;
        // verify the required parameter 'rule' is set
        if (rule == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'rule' when calling createRule",
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

        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules",
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
     * Create a new data rule
     * Creates a new data access rule. The rule will be inserted at the specified priority position. If a rule with the same identifier (same combination of user, role, service, request, workspace, layer, etc.) already exists, the operation will fail with a 409 Conflict.  The priority determines evaluation order - lower numbers are evaluated first. Use the position parameter to control how the priority value is interpreted (fixed value, from start, or from end of the list).
     * <p><b>201</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request. Common causes include: - Providing a rule with a non-null id (id is auto-generated) - Invalid field values or combinations
     * <p><b>409</b> - A rule with the same identifier already exists. Rules are uniquely identified by the combination of: user, role, service, request, subfield, workspace, layer, and addressRange. If you need to modify an existing rule, use the PATCH endpoint instead.
     * @param rule The rule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Rule createRule(@jakarta.annotation.Nonnull Rule rule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return createRuleRequestCreation(rule, position).body(localVarReturnType);
    }

    /**
     * Create a new data rule
     * Creates a new data access rule. The rule will be inserted at the specified priority position. If a rule with the same identifier (same combination of user, role, service, request, workspace, layer, etc.) already exists, the operation will fail with a 409 Conflict.  The priority determines evaluation order - lower numbers are evaluated first. Use the position parameter to control how the priority value is interpreted (fixed value, from start, or from end of the list).
     * <p><b>201</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request. Common causes include: - Providing a rule with a non-null id (id is auto-generated) - Invalid field values or combinations
     * <p><b>409</b> - A rule with the same identifier already exists. Rules are uniquely identified by the combination of: user, role, service, request, subfield, workspace, layer, and addressRange. If you need to modify an existing rule, use the PATCH endpoint instead.
     * @param rule The rule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return ResponseEntity&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Rule> createRuleWithHttpInfo(
            @jakarta.annotation.Nonnull Rule rule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return createRuleRequestCreation(rule, position).toEntity(localVarReturnType);
    }

    /**
     * Create a new data rule
     * Creates a new data access rule. The rule will be inserted at the specified priority position. If a rule with the same identifier (same combination of user, role, service, request, workspace, layer, etc.) already exists, the operation will fail with a 409 Conflict.  The priority determines evaluation order - lower numbers are evaluated first. Use the position parameter to control how the priority value is interpreted (fixed value, from start, or from end of the list).
     * <p><b>201</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request. Common causes include: - Providing a rule with a non-null id (id is auto-generated) - Invalid field values or combinations
     * <p><b>409</b> - A rule with the same identifier already exists. Rules are uniquely identified by the combination of: user, role, service, request, subfield, workspace, layer, and addressRange. If you need to modify an existing rule, use the PATCH endpoint instead.
     * @param rule The rule parameter
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createRuleWithResponseSpec(
            @jakarta.annotation.Nonnull Rule rule, @jakarta.annotation.Nullable InsertPosition position)
            throws RestClientResponseException {
        return createRuleRequestCreation(rule, position);
    }

    /**
     * Delete all data rules
     * Removes all data access rules from the system. This operation is atomic - either all rules are deleted successfully, or none are deleted. Returns the count of rules that were removed. Use with caution as this operation cannot be undone.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteAllRulesRequestCreation() throws RestClientResponseException {
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
                "/rules",
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
     * Delete all data rules
     * Removes all data access rules from the system. This operation is atomic - either all rules are deleted successfully, or none are deleted. Returns the count of rules that were removed. Use with caution as this operation cannot be undone.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer deleteAllRules() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteAllRulesRequestCreation().body(localVarReturnType);
    }

    /**
     * Delete all data rules
     * Removes all data access rules from the system. This operation is atomic - either all rules are deleted successfully, or none are deleted. Returns the count of rules that were removed. Use with caution as this operation cannot be undone.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> deleteAllRulesWithHttpInfo() throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteAllRulesRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Delete all data rules
     * Removes all data access rules from the system. This operation is atomic - either all rules are deleted successfully, or none are deleted. Returns the count of rules that were removed. Use with caution as this operation cannot be undone.
     * <p><b>200</b> - The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteAllRulesWithResponseSpec() throws RestClientResponseException {
        return deleteAllRulesRequestCreation();
    }

    /**
     * Delete a data rule
     * Permanently removes the specified data rule from the system.
     * <p><b>200</b> - Rule successfully deleted
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteRuleByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling deleteRuleById",
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
                "/rules/id/{id}",
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
     * Delete a data rule
     * Permanently removes the specified data rule from the system.
     * <p><b>200</b> - Rule successfully deleted
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void deleteRuleById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        deleteRuleByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     * Delete a data rule
     * Permanently removes the specified data rule from the system.
     * <p><b>200</b> - Rule successfully deleted
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> deleteRuleByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return deleteRuleByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete a data rule
     * Permanently removes the specified data rule from the system.
     * <p><b>200</b> - Rule successfully deleted
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteRuleByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return deleteRuleByIdRequestCreation(id);
    }

    /**
     * Find rule by priority value
     * Retrieves the data rule with the specified priority value. If multiple rules somehow have the same priority (which shouldn&#39;t normally occur), returns a 409 Conflict error.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec findOneRuleByPriorityRequestCreation(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'priority' is set
        if (priority == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'priority' when calling findOneRuleByPriority",
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

        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/query/one/priority/{priority}",
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
     * Find rule by priority value
     * Retrieves the data rule with the specified priority value. If multiple rules somehow have the same priority (which shouldn&#39;t normally occur), returns a 409 Conflict error.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Rule findOneRuleByPriority(@jakarta.annotation.Nonnull Long priority) throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findOneRuleByPriorityRequestCreation(priority).body(localVarReturnType);
    }

    /**
     * Find rule by priority value
     * Retrieves the data rule with the specified priority value. If multiple rules somehow have the same priority (which shouldn&#39;t normally occur), returns a 409 Conflict error.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return ResponseEntity&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Rule> findOneRuleByPriorityWithHttpInfo(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return findOneRuleByPriorityRequestCreation(priority).toEntity(localVarReturnType);
    }

    /**
     * Find rule by priority value
     * Retrieves the data rule with the specified priority value. If multiple rules somehow have the same priority (which shouldn&#39;t normally occur), returns a 409 Conflict error.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - Not found
     * <p><b>409</b> - Conflict, there&#39;s more than one rule with the requested priority
     * @param priority The rule priority to search for
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec findOneRuleByPriorityWithResponseSpec(@jakarta.annotation.Nonnull Long priority)
            throws RestClientResponseException {
        return findOneRuleByPriorityRequestCreation(priority);
    }

    /**
     *
     * Returns the LayerDetails for the Rule with the given identifier
     * <p><b>200</b> - Layer-specific access control details. These settings provide fine-grained control over what data and styles can be accessed for a specific layer.
     * <p><b>204</b> - The rule has a layer set but does not have LayerDetails set
     * <p><b>400</b> - Bad request if the rule does not have a layer name set
     * <p><b>404</b> - Not found if the rule does not exist
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return LayerDetails
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLayerDetailsByRuleIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling getLayerDetailsByRuleId",
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

        ParameterizedTypeReference<LayerDetails> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}/layer-details",
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
     * Returns the LayerDetails for the Rule with the given identifier
     * <p><b>200</b> - Layer-specific access control details. These settings provide fine-grained control over what data and styles can be accessed for a specific layer.
     * <p><b>204</b> - The rule has a layer set but does not have LayerDetails set
     * <p><b>400</b> - Bad request if the rule does not have a layer name set
     * <p><b>404</b> - Not found if the rule does not exist
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return LayerDetails
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public LayerDetails getLayerDetailsByRuleId(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<LayerDetails> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getLayerDetailsByRuleIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     *
     * Returns the LayerDetails for the Rule with the given identifier
     * <p><b>200</b> - Layer-specific access control details. These settings provide fine-grained control over what data and styles can be accessed for a specific layer.
     * <p><b>204</b> - The rule has a layer set but does not have LayerDetails set
     * <p><b>400</b> - Bad request if the rule does not have a layer name set
     * <p><b>404</b> - Not found if the rule does not exist
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseEntity&lt;LayerDetails&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<LayerDetails> getLayerDetailsByRuleIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<LayerDetails> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getLayerDetailsByRuleIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     *
     * Returns the LayerDetails for the Rule with the given identifier
     * <p><b>200</b> - Layer-specific access control details. These settings provide fine-grained control over what data and styles can be accessed for a specific layer.
     * <p><b>204</b> - The rule has a layer set but does not have LayerDetails set
     * <p><b>400</b> - Bad request if the rule does not have a layer name set
     * <p><b>404</b> - Not found if the rule does not exist
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLayerDetailsByRuleIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return getLayerDetailsByRuleIdRequestCreation(id);
    }

    /**
     * Get rule by ID
     * Retrieves a specific data rule by its unique identifier.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRuleByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling getRuleById",
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

        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}",
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
     * Get rule by ID
     * Retrieves a specific data rule by its unique identifier.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Rule getRuleById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getRuleByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     * Get rule by ID
     * Retrieves a specific data rule by its unique identifier.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseEntity&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Rule> getRuleByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getRuleByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Get rule by ID
     * Retrieves a specific data rule by its unique identifier.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>404</b> - No rule exists with the specified ID
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRuleByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return getRuleByIdRequestCreation(id);
    }

    /**
     * List all data rules
     * Retrieves all data access rules in priority order (lowest priority number first). Supports cursor-based pagination for large rule sets. Use the limit parameter to control page size and the nextCursor parameter to fetch subsequent pages.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRulesRequestCreation(
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

        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules",
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
     * List all data rules
     * Retrieves all data access rules in priority order (lowest priority number first). Supports cursor-based pagination for large rule sets. Use the limit parameter to control page size and the nextCursor parameter to fetch subsequent pages.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public List<Rule> getRules(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getRulesRequestCreation(limit, nextCursor).body(localVarReturnType);
    }

    /**
     * List all data rules
     * Retrieves all data access rules in priority order (lowest priority number first). Supports cursor-based pagination for large rule sets. Use the limit parameter to control page size and the nextCursor parameter to fetch subsequent pages.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return ResponseEntity&lt;List&lt;Rule&gt;&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<Rule>> getRulesWithHttpInfo(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return getRulesRequestCreation(limit, nextCursor).toEntity(localVarReturnType);
    }

    /**
     * List all data rules
     * Retrieves all data access rules in priority order (lowest priority number first). Supports cursor-based pagination for large rule sets. Use the limit parameter to control page size and the nextCursor parameter to fetch subsequent pages.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRulesWithResponseSpec(
            @jakarta.annotation.Nullable Integer limit, @jakarta.annotation.Nullable String nextCursor)
            throws RestClientResponseException {
        return getRulesRequestCreation(limit, nextCursor);
    }

    /**
     * Query data rules with filters
     * Searches for data rules matching the provided filter criteria. Supports cursor-based pagination. The filter allows matching on user, roles, service, request, workspace, layer, and source IP address. Empty filters or wildcard values match any value for that property.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param ruleFilter The ruleFilter parameter
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec queryRulesRequestCreation(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        Object postBody = ruleFilter;
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

        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/query",
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
     * Query data rules with filters
     * Searches for data rules matching the provided filter criteria. Supports cursor-based pagination. The filter allows matching on user, roles, service, request, workspace, layer, and source IP address. Empty filters or wildcard values match any value for that property.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param ruleFilter The ruleFilter parameter
     * @return List&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public List<Rule> queryRules(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return queryRulesRequestCreation(limit, nextCursor, ruleFilter).body(localVarReturnType);
    }

    /**
     * Query data rules with filters
     * Searches for data rules matching the provided filter criteria. Supports cursor-based pagination. The filter allows matching on user, roles, service, request, workspace, layer, and source IP address. Empty filters or wildcard values match any value for that property.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param ruleFilter The ruleFilter parameter
     * @return ResponseEntity&lt;List&lt;Rule&gt;&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<Rule>> queryRulesWithHttpInfo(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        ParameterizedTypeReference<List<Rule>> localVarReturnType = new ParameterizedTypeReference<>() {};
        return queryRulesRequestCreation(limit, nextCursor, ruleFilter).toEntity(localVarReturnType);
    }

    /**
     * Query data rules with filters
     * Searches for data rules matching the provided filter criteria. Supports cursor-based pagination. The filter allows matching on user, roles, service, request, workspace, layer, and source IP address. Empty filters or wildcard values match any value for that property.
     * <p><b>200</b> - A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.
     * @param ruleFilter The ruleFilter parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec queryRulesWithResponseSpec(
            @jakarta.annotation.Nullable Integer limit,
            @jakarta.annotation.Nullable String nextCursor,
            @jakarta.annotation.Nullable RuleFilter ruleFilter)
            throws RestClientResponseException {
        return queryRulesRequestCreation(limit, nextCursor, ruleFilter);
    }

    /**
     * Check if rule exists
     * Checks whether a data rule with the specified ID exists in the system.
     * <p><b>200</b> - Returns true if the rule exists, false otherwise
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec ruleExistsByIdRequestCreation(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling ruleExistsById",
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
                "/rules/id/{id}/exists",
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
     * Check if rule exists
     * Checks whether a data rule with the specified ID exists in the system.
     * <p><b>200</b> - Returns true if the rule exists, false otherwise
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Boolean ruleExistsById(@jakarta.annotation.Nonnull String id) throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {};
        return ruleExistsByIdRequestCreation(id).body(localVarReturnType);
    }

    /**
     * Check if rule exists
     * Checks whether a data rule with the specified ID exists in the system.
     * <p><b>200</b> - Returns true if the rule exists, false otherwise
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Boolean> ruleExistsByIdWithHttpInfo(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {};
        return ruleExistsByIdRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Check if rule exists
     * Checks whether a data rule with the specified ID exists in the system.
     * <p><b>200</b> - Returns true if the rule exists, false otherwise
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec ruleExistsByIdWithResponseSpec(@jakarta.annotation.Nonnull String id)
            throws RestClientResponseException {
        return ruleExistsByIdRequestCreation(id);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param requestBody The requestBody parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setRuleAllowedStylesRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable Set<String> requestBody)
            throws RestClientResponseException {
        Object postBody = requestBody;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling setRuleAllowedStyles",
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
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}/styles",
                HttpMethod.PUT,
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
     * @param requestBody The requestBody parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void setRuleAllowedStyles(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable Set<String> requestBody)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        setRuleAllowedStylesRequestCreation(id, requestBody).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param requestBody The requestBody parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> setRuleAllowedStylesWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable Set<String> requestBody)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return setRuleAllowedStylesRequestCreation(id, requestBody).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param requestBody The requestBody parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setRuleAllowedStylesWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable Set<String> requestBody)
            throws RestClientResponseException {
        return setRuleAllowedStylesRequestCreation(id, requestBody);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param layerDetails The layerDetails parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setRuleLayerDetailsRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable LayerDetails layerDetails)
            throws RestClientResponseException {
        Object postBody = layerDetails;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling setRuleLayerDetails",
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
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}/layer-details",
                HttpMethod.PUT,
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
     * @param layerDetails The layerDetails parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void setRuleLayerDetails(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable LayerDetails layerDetails)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        setRuleLayerDetailsRequestCreation(id, layerDetails).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param layerDetails The layerDetails parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> setRuleLayerDetailsWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable LayerDetails layerDetails)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return setRuleLayerDetailsRequestCreation(id, layerDetails).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param layerDetails The layerDetails parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setRuleLayerDetailsWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable LayerDetails layerDetails)
            throws RestClientResponseException {
        return setRuleLayerDetailsRequestCreation(id, layerDetails);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>400</b> - Bad request, the rule is not of LIMIT type
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param ruleLimits The ruleLimits parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setRuleLimitsRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable RuleLimits ruleLimits)
            throws RestClientResponseException {
        Object postBody = ruleLimits;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling setRuleLimits",
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
        final String[] localVarContentTypes = {"application/json", "application/x-jackson-smile"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}/limits",
                HttpMethod.PUT,
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
     * <p><b>400</b> - Bad request, the rule is not of LIMIT type
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param ruleLimits The ruleLimits parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void setRuleLimits(@jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable RuleLimits ruleLimits)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        setRuleLimitsRequestCreation(id, ruleLimits).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>400</b> - Bad request, the rule is not of LIMIT type
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param ruleLimits The ruleLimits parameter
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> setRuleLimitsWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable RuleLimits ruleLimits)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return setRuleLimitsRequestCreation(id, ruleLimits).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>400</b> - Bad request, the rule is not of LIMIT type
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param ruleLimits The ruleLimits parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setRuleLimitsWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nullable RuleLimits ruleLimits)
            throws RestClientResponseException {
        return setRuleLimitsRequestCreation(id, ruleLimits);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec shiftRulesByPriorityRequestCreation(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'priorityStart' is set
        if (priorityStart == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'priorityStart' when calling shiftRulesByPriority",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'offset' is set
        if (offset == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'offset' when calling shiftRulesByPriority",
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

        final String[] localVarAccepts = {"application/json", "application/x-jackson-smile"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"basicAuth"};

        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/shift",
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
     * <p><b>200</b> - The number of rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return Integer
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Integer shiftRulesByPriority(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return shiftRulesByPriorityRequestCreation(priorityStart, offset).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return ResponseEntity&lt;Integer&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Integer> shiftRulesByPriorityWithHttpInfo(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<>() {};
        return shiftRulesByPriorityRequestCreation(priorityStart, offset).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>200</b> - The number of rules whose priority was shifted by the required offset
     * <p><b>404</b> - Not found
     * @param priorityStart The minimum priority to start shifting at (inclusive)
     * @param offset The priority offset to apply to all rules from priorityStart onwards
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec shiftRulesByPriorityWithResponseSpec(
            @jakarta.annotation.Nonnull Long priorityStart, @jakarta.annotation.Nonnull Long offset)
            throws RestClientResponseException {
        return shiftRulesByPriorityRequestCreation(priorityStart, offset);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec swapRulesRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling swapRules",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'id2' is set
        if (id2 == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id2' when calling swapRules",
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
                "/rules/id/{id}/swapwith/{id2}",
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
     * @param id2 The rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public void swapRules(@jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        swapRulesRequestCreation(id, id2).body(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The rule identifier to swap priorities with
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> swapRulesWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<>() {};
        return swapRulesRequestCreation(id, id2).toEntity(localVarReturnType);
    }

    /**
     *
     *
     * <p><b>204</b> - Updated
     * <p><b>404</b> - Not found
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param id2 The rule identifier to swap priorities with
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec swapRulesWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull String id2)
            throws RestClientResponseException {
        return swapRulesRequestCreation(id, id2);
    }

    /**
     * Update an existing data rule
     * Updates an existing data rule. You can modify any property of the rule, including its priority. If you change the rule&#39;s identifier properties (user, role, service, etc.) to match another existing rule, the operation will fail with a 409 Conflict.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request body or field values
     * <p><b>404</b> - No rule exists with the specified ID
     * <p><b>409</b> - The updated rule identifier properties would conflict with another existing rule. Rules must have unique combinations of user, role, service, request, workspace, layer, etc.
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param rule The rule parameter
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateRuleByIdRequestCreation(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull Rule rule)
            throws RestClientResponseException {
        Object postBody = rule;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'id' when calling updateRuleById",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    null,
                    null,
                    null);
        }
        // verify the required parameter 'rule' is set
        if (rule == null) {
            throw new RestClientResponseException(
                    "Missing the required parameter 'rule' when calling updateRuleById",
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

        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return apiClient.invokeAPI(
                "/rules/id/{id}",
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
     * Update an existing data rule
     * Updates an existing data rule. You can modify any property of the rule, including its priority. If you change the rule&#39;s identifier properties (user, role, service, etc.) to match another existing rule, the operation will fail with a 409 Conflict.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request body or field values
     * <p><b>404</b> - No rule exists with the specified ID
     * <p><b>409</b> - The updated rule identifier properties would conflict with another existing rule. Rules must have unique combinations of user, role, service, request, workspace, layer, etc.
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param rule The rule parameter
     * @return Rule
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Rule updateRuleById(@jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull Rule rule)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return updateRuleByIdRequestCreation(id, rule).body(localVarReturnType);
    }

    /**
     * Update an existing data rule
     * Updates an existing data rule. You can modify any property of the rule, including its priority. If you change the rule&#39;s identifier properties (user, role, service, etc.) to match another existing rule, the operation will fail with a 409 Conflict.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request body or field values
     * <p><b>404</b> - No rule exists with the specified ID
     * <p><b>409</b> - The updated rule identifier properties would conflict with another existing rule. Rules must have unique combinations of user, role, service, request, workspace, layer, etc.
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param rule The rule parameter
     * @return ResponseEntity&lt;Rule&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Rule> updateRuleByIdWithHttpInfo(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull Rule rule)
            throws RestClientResponseException {
        ParameterizedTypeReference<Rule> localVarReturnType = new ParameterizedTypeReference<>() {};
        return updateRuleByIdRequestCreation(id, rule).toEntity(localVarReturnType);
    }

    /**
     * Update an existing data rule
     * Updates an existing data rule. You can modify any property of the rule, including its priority. If you change the rule&#39;s identifier properties (user, role, service, etc.) to match another existing rule, the operation will fail with a 409 Conflict.
     * <p><b>200</b> - A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.
     * <p><b>400</b> - Invalid request body or field values
     * <p><b>404</b> - No rule exists with the specified ID
     * <p><b>409</b> - The updated rule identifier properties would conflict with another existing rule. Rules must have unique combinations of user, role, service, request, workspace, layer, etc.
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.
     * @param rule The rule parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateRuleByIdWithResponseSpec(
            @jakarta.annotation.Nonnull String id, @jakarta.annotation.Nonnull Rule rule)
            throws RestClientResponseException {
        return updateRuleByIdRequestCreation(id, rule);
    }
}
