package org.geoserver.acl.webapi.v1.server;

import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.geoserver.acl.webapi.v1.model.LayerDetails;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.geoserver.acl.webapi.v1.model.RuleFilter;
import org.geoserver.acl.webapi.v1.model.RuleLimits;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link DataRulesApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:51:14.805992-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.17.0")
public interface DataRulesApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /rules/query/count : Count all data rules
     * Returns the total number of data access rules currently configured in the system.
     *
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see DataRulesApi#countAllRules
     */
    default ResponseEntity<Integer> countAllRules() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "42";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /rules/query/count : Count rules matching filter criteria
     * Returns the number of data rules that match the provided filter criteria. Useful for determining result set size before paginating through query results.
     *
     * @param ruleFilter  (required)
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see DataRulesApi#countRules
     */
    default ResponseEntity<Integer> countRules(RuleFilter ruleFilter) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "42";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /rules : Create a new data rule
     * Creates a new data access rule. The rule will be inserted at the specified priority position. If a rule with the same identifier (same combination of user, role, service, request, workspace, layer, etc.) already exists, the operation will fail with a 409 Conflict.  The priority determines evaluation order - lower numbers are evaluated first. Use the position parameter to control how the priority value is interpreted (fixed value, from start, or from end of the list).
     *
     * @param rule  (required)
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.  (optional)
     * @return A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.  (status code 201)
     *         or Invalid request. Common causes include: - Providing a rule with a non-null id (id is auto-generated) - Invalid field values or combinations  (status code 400)
     *         or A rule with the same identifier already exists. Rules are uniquely identified by the combination of: user, role, service, request, subfield, workspace, layer, and addressRange. If you need to modify an existing rule, use the PATCH endpoint instead.  (status code 409)
     * @see DataRulesApi#createRule
     */
    default ResponseEntity<Rule> createRule(Rule rule, InsertPosition position) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" }";
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
     * DELETE /rules : Delete all data rules
     * Removes all data access rules from the system. This operation is atomic - either all rules are deleted successfully, or none are deleted. Returns the count of rules that were removed. Use with caution as this operation cannot be undone.
     *
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see DataRulesApi#deleteAllRules
     */
    default ResponseEntity<Integer> deleteAllRules() {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "42";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * DELETE /rules/id/{id} : Delete a data rule
     * Permanently removes the specified data rule from the system.
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return Rule successfully deleted (status code 200)
     *         or No rule exists with the specified ID (status code 404)
     * @see DataRulesApi#deleteRuleById
     */
    default ResponseEntity<Void> deleteRuleById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /rules/query/one/priority/{priority} : Find rule by priority value
     * Retrieves the data rule with the specified priority value. If multiple rules somehow have the same priority (which shouldn&#39;t normally occur), returns a 409 Conflict error.
     *
     * @param priority The rule priority to search for (required)
     * @return A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Not found (status code 404)
     *         or Conflict, there&#39;s more than one rule with the requested priority (status code 409)
     * @see DataRulesApi#findOneRuleByPriority
     */
    default ResponseEntity<Rule> findOneRuleByPriority(Long priority) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" }";
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
     * GET /rules/id/{id}/layer-details
     * Returns the LayerDetails for the Rule with the given identifier
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return Layer-specific access control details. These settings provide fine-grained control over what data and styles can be accessed for a specific layer.  (status code 200)
     *         or The rule has a layer set but does not have LayerDetails set (status code 204)
     *         or Bad request if the rule does not have a layer name set (status code 400)
     *         or Not found if the rule does not exist (status code 404)
     * @see DataRulesApi#getLayerDetailsByRuleId
     */
    default ResponseEntity<LayerDetails> getLayerDetailsByRuleId(String id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"cqlFilterWrite\" : \"cqlFilterWrite\", \"allowedArea\" : { \"wkt\" : \"SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))\" }, \"cqlFilterRead\" : \"cqlFilterRead\", \"defaultStyle\" : \"defaultStyle\", \"allowedStyles\" : [ \"allowedStyles\", \"allowedStyles\" ], \"catalogMode\" : \"HIDE\", \"type\" : \"VECTOR\", \"spatialFilterType\" : \"INTERSECT\", \"layerAttributes\" : [ { \"access\" : \"NONE\", \"dataType\" : \"dataType\", \"name\" : \"name\" }, { \"access\" : \"NONE\", \"dataType\" : \"dataType\", \"name\" : \"name\" } ] }";
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
     * GET /rules/id/{id} : Get rule by ID
     * Retrieves a specific data rule by its unique identifier.
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or No rule exists with the specified ID (status code 404)
     * @see DataRulesApi#getRuleById
     */
    default ResponseEntity<Rule> getRuleById(String id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" }";
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
     * GET /rules : List all data rules
     * Retrieves all data access rules in priority order (lowest priority number first). Supports cursor-based pagination for large rule sets. Use the limit parameter to control page size and the nextCursor parameter to fetch subsequent pages.
     *
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.  (optional)
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.  (optional)
     * @return A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.  (status code 200)
     * @see DataRulesApi#getRules
     */
    default ResponseEntity<List<Rule>> getRules(Integer limit, String nextCursor) {
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
     * POST /rules/query : Query data rules with filters
     * Searches for data rules matching the provided filter criteria. Supports cursor-based pagination. The filter allows matching on user, roles, service, request, workspace, layer, and source IP address. Empty filters or wildcard values match any value for that property.
     *
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.  (optional)
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.  (optional)
     * @param ruleFilter  (optional)
     * @return A paginated list of data access rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.  (status code 200)
     * @see DataRulesApi#queryRules
     */
    default ResponseEntity<List<Rule>> queryRules(Integer limit, String nextCursor, RuleFilter ruleFilter) {
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
     * GET /rules/id/{id}/exists : Check if rule exists
     * Checks whether a data rule with the specified ID exists in the system.
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return Returns true if the rule exists, false otherwise (status code 200)
     * @see DataRulesApi#ruleExistsById
     */
    default ResponseEntity<Boolean> ruleExistsById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /rules/id/{id}/styles
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param requestBody  (optional)
     * @return Updated (status code 204)
     *         or Not found (status code 404)
     * @see DataRulesApi#setRuleAllowedStyles
     */
    default ResponseEntity<Void> setRuleAllowedStyles(String id, Set<String> requestBody) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /rules/id/{id}/layer-details
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param layerDetails  (optional)
     * @return Updated (status code 204)
     *         or Not found (status code 404)
     * @see DataRulesApi#setRuleLayerDetails
     */
    default ResponseEntity<Void> setRuleLayerDetails(String id, LayerDetails layerDetails) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /rules/id/{id}/limits
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param ruleLimits  (optional)
     * @return Updated (status code 204)
     *         or Bad request, the rule is not of LIMIT type (status code 400)
     *         or Not found (status code 404)
     * @see DataRulesApi#setRuleLimits
     */
    default ResponseEntity<Void> setRuleLimits(String id, RuleLimits ruleLimits) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /rules/shift
     *
     * @param priorityStart The minimum priority to start shifting at (inclusive) (required)
     * @param offset The priority offset to apply to all rules from priorityStart onwards (required)
     * @return The number of rules whose priority was shifted by the required offset (status code 200)
     *         or Not found (status code 404)
     * @see DataRulesApi#shiftRulesByPriority
     */
    default ResponseEntity<Integer> shiftRulesByPriority(Long priorityStart, Long offset) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /rules/id/{id}/swapwith/{id2}
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param id2 The rule identifier to swap priorities with (required)
     * @return Updated (status code 204)
     *         or Not found (status code 404)
     * @see DataRulesApi#swapRules
     */
    default ResponseEntity<Void> swapRules(String id, String id2) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PATCH /rules/id/{id} : Update an existing data rule
     * Updates an existing data rule. You can modify any property of the rule, including its priority. If you change the rule&#39;s identifier properties (user, role, service, etc.) to match another existing rule, the operation will fail with a 409 Conflict.
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param rule  (required)
     * @return A data access rule. Returned after successfully creating, retrieving, or updating a rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Invalid request body or field values (status code 400)
     *         or No rule exists with the specified ID (status code 404)
     *         or The updated rule identifier properties would conflict with another existing rule. Rules must have unique combinations of user, role, service, request, workspace, layer, etc.  (status code 409)
     * @see DataRulesApi#updateRuleById
     */
    default ResponseEntity<Rule> updateRuleById(String id, Rule rule) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"priority\" : 100, \"access\" : \"ALLOW\", \"role\" : \"ROLE_VIEWER\", \"service\" : \"WMS\", \"request\" : \"GetMap\", \"workspace\" : \"topp\", \"layer\" : \"roads\", \"name\" : \"Allow WMS GetMap for roads layer\" }";
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
