package org.geoserver.acl.webapi.v1.server;

import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Optional;
import org.geoserver.acl.webapi.v1.model.AdminRule;
import org.geoserver.acl.webapi.v1.model.AdminRuleFilter;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A delegate to be called by the {@link WorkspaceAdminRulesApiController}}.
 * Implement this interface with a {@link org.springframework.stereotype.Service} annotated class.
 */
@Generated(
        value = "org.openapitools.codegen.languages.SpringCodegen",
        date = "2026-01-29T18:51:14.805992-03:00[America/Argentina/Cordoba]",
        comments = "Generator version: 7.17.0")
public interface WorkspaceAdminRulesApiDelegate {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * GET /adminrules/id/{id}/exists
     * Returns whether the AdminRule with the given identifier exists
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return boolean indicating whether the admin rule with the provided identifier exists (status code 200)
     * @see WorkspaceAdminRulesApi#adminRuleExistsById
     */
    default ResponseEntity<Boolean> adminRuleExistsById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /adminrules/query/count
     * Returns the number of rules that matches the search criteria
     *
     * @param adminRuleFilter  (required)
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see WorkspaceAdminRulesApi#countAdminRules
     */
    default ResponseEntity<Integer> countAdminRules(AdminRuleFilter adminRuleFilter) {
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
     * GET /adminrules/query/count
     * Returns the total number of rules
     *
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see WorkspaceAdminRulesApi#countAllAdminRules
     */
    default ResponseEntity<Integer> countAllAdminRules() {
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
     * POST /adminrules
     *
     * @param adminRule  (required)
     * @param position Controls how the rule&#39;s priority value should be interpreted when inserting a new rule. See the InsertPosition schema for detailed explanation of FIXED, FROM_START, and FROM_END options. Defaults to FIXED if not specified.  (optional)
     * @return A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.  (status code 201)
     * @see WorkspaceAdminRulesApi#createAdminRule
     */
    default ResponseEntity<AdminRule> createAdminRule(AdminRule adminRule, InsertPosition position) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }";
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
     * DELETE /adminrules/id/{id}
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return OK (status code 200)
     *         or Not found (status code 404)
     * @see WorkspaceAdminRulesApi#deleteAdminRuleById
     */
    default ResponseEntity<Void> deleteAdminRuleById(String id) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * DELETE /adminrules
     * Atomically deletes all admin rules and return the number of rules removed
     *
     * @return The total number of rules that match the query criteria. This count represents the full result set, not just the current page if pagination is being used.  (status code 200)
     * @see WorkspaceAdminRulesApi#deleteAllAdminRules
     */
    default ResponseEntity<Integer> deleteAllAdminRules() {
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
     * POST /adminrules/query
     *
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.  (optional)
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.  (optional)
     * @param adminRuleFilter  (optional)
     * @return A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.  (status code 200)
     * @see WorkspaceAdminRulesApi#findAdminRules
     */
    default ResponseEntity<List<AdminRule>> findAdminRules(
            Integer limit, String nextCursor, AdminRuleFilter adminRuleFilter) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "[ { \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }, { \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /adminrules
     * Returns an (optionally) paginated list of admin rules.
     *
     * @param limit Maximum number of items to return in a single page. If more items match the query than this limit, the response will include an X-ACL-NEXTCURSOR header with a cursor value to fetch the next page. Use in combination with nextCursor to paginate through large result sets.  (optional)
     * @param nextCursor Cursor for pagination. When a response includes an X-ACL-NEXTCURSOR header, use that value here to fetch the next page of results. Continue until X-ACL-NEXTCURSOR is null or absent, indicating no more results are available. This implements efficient cursor-based pagination that works well with large datasets.  (optional)
     * @return A paginated list of workspace admin rules matching the query criteria. Rules are returned in priority order (lowest priority number first). If more rules exist beyond this page, check the X-ACL-NEXTCURSOR header to fetch the next page. An empty array indicates no rules match or you&#39;ve reached the end of results.  (status code 200)
     * @see WorkspaceAdminRulesApi#findAllAdminRules
     */
    default ResponseEntity<List<AdminRule>> findAllAdminRules(Integer limit, String nextCursor) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "[ { \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }, { \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" } ]";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /adminrules/query/first
     * Finds the first rule that satisfies the query criteria
     *
     * @param adminRuleFilter  (required)
     * @return A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Not found (status code 404)
     * @see WorkspaceAdminRulesApi#findFirstAdminRule
     */
    default ResponseEntity<AdminRule> findFirstAdminRule(AdminRuleFilter adminRuleFilter) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }";
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
     * GET /adminrules/query/one/priority/{priority}
     * Finds the AdminRule with the given priority
     *
     * @param priority The rule priority to search for (required)
     * @return A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Not found (status code 404)
     *         or Conflict, there&#39;s more than one rule with the requested priority (status code 409)
     * @see WorkspaceAdminRulesApi#findOneAdminRuleByPriority
     */
    default ResponseEntity<AdminRule> findOneAdminRuleByPriority(Long priority) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }";
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
     * GET /adminrules/id/{id}
     * Returns the AdminRule with the given identifier
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @return A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Not found (status code 404)
     * @see WorkspaceAdminRulesApi#getAdminRuleById
     */
    default ResponseEntity<AdminRule> getAdminRuleById(String id) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }";
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
     * POST /adminrules/shift
     *
     * @param priorityStart The minimum priority to start shifting at (inclusive) (required)
     * @param offset The priority offset to apply to all rules from priorityStart onwards (required)
     * @return The number of admin rules whose priority was shifted by the required offset (status code 200)
     *         or Not found (status code 404)
     * @see WorkspaceAdminRulesApi#shiftAdminRulesByPriority
     */
    default ResponseEntity<Integer> shiftAdminRulesByPriority(Long priorityStart, Long offset) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * POST /adminrules/id/{id}/swapwith/{id2}
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param id2 The admin rule identifier to swap priorities with (required)
     * @return Updated (status code 204)
     *         or Not found (status code 404)
     * @see WorkspaceAdminRulesApi#swapAdminRules
     */
    default ResponseEntity<Void> swapAdminRules(String id, String id2) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PATCH /adminrules/id/{id}
     *
     * @param id Unique identifier for the rule. This is a system-generated ID that uniquely identifies a specific rule instance.  (required)
     * @param adminRule  (required)
     * @return A workspace administration rule. Returned after successfully creating, retrieving, or updating an admin rule. The response includes the complete rule definition with its system-generated ID.  (status code 200)
     *         or Bad request body (status code 400)
     *         or Not found (status code 404)
     *         or Conflict, tried to update the rule identifier properties to one that belongs to another rule (status code 409)
     * @see WorkspaceAdminRulesApi#updateAdminRule
     */
    default ResponseEntity<AdminRule> updateAdminRule(String id, AdminRule adminRule) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType : MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString =
                            "{ \"workspace\" : \"workspace\", \"access\" : \"ADMIN\", \"role\" : \"role\", \"name\" : \"Grant admin access to cartography workspace\", \"addressRange\" : \"192.168.1.0/24\", \"description\" : \"description\", \"id\" : \"id\", \"extId\" : \"extId\", \"priority\" : 50, \"user\" : \"user\" }";
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
