/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.rules;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.geoserver.acl.domain.filter.RuleQuery;

/**
 * Domain service for managing data access rules.
 *
 * <p>Extends {@link RuleRepository} with business logic:
 * <ul>
 *   <li>Business validation and constraint enforcement
 *   <li>Field sanitization (service/request names to uppercase for OGC compliance)
 *   <li>Priority management (unique priorities, automatic shifting)
 *   <li>Event publishing for cache invalidation and distributed updates
 *   <li>Concurrency control (uses {@code ReentrantLock} for priority operations)
 * </ul>
 *
 * <p>After mutations (insert, update, delete), publishes {@link RuleEvent} to registered listeners
 * for cache invalidation and remote distribution in clustered deployments.
 *
 * <p>Example:
 * <pre>{@code
 * // Configure event publishing
 * ruleAdminService.setEventPublisher(event -> {
 *     if (event.getType() == RuleEvent.EventType.UPDATED) {
 *         authorizationCache.invalidate(event.getRuleIds());
 *     }
 * });
 *
 * // Insert rule
 * Rule newRule = Rule.allow()
 *     .withUsername("alice")
 *     .withWorkspace("topp");
 * Rule created = ruleAdminService.insert(newRule, InsertPosition.FROM_START);
 * }</pre>
 *
 * @since 1.0
 * @see Rule
 * @see RuleRepository
 * @see RuleEvent
 */
public interface RuleAdminService {

    /**
     * Sets the event publisher for rule change notifications.
     *
     * <p>Events are published after successful mutations (insert, update, delete). If the consumer
     * throws an exception, it will be logged and ignored to prevent the operation from failing.
     *
     * @param eventPublisher consumer that receives rule events
     */
    void setEventPublisher(Consumer<RuleEvent> eventPublisher);

    /**
     * Creates a new rule with its specified priority.
     *
     * <p>Service and request names in the rule identifier are normalized to uppercase.
     *
     * @param rule the rule to create (must not have an id)
     * @return the created rule with assigned id and normalized identifiers
     * @throws RuleIdentifierConflictException if a rule with the same identifier already exists
     */
    Rule insert(Rule rule);

    /**
     * Creates a new rule at the specified position in the priority order.
     *
     * <p>Service and request names in the rule identifier are normalized to uppercase.
     *
     * @param rule the rule to create (must not have an id)
     * @param position where to insert the rule in priority order
     * @return the created rule with assigned id, priority, and normalized identifiers
     * @throws IllegalArgumentException if the rule has an id set
     * @throws RuleIdentifierConflictException if a rule with the same identifier already exists
     */
    Rule insert(Rule rule, InsertPosition position);

    /**
     * Updates an existing rule.
     *
     * <p>Service and request names in the rule identifier are normalized to uppercase.
     *
     * @param rule the rule to update (must have an id)
     * @return the updated rule with normalized identifiers
     * @throws IllegalArgumentException if the rule has no id or does not exist
     * @throws RuleIdentifierConflictException if the update would create an identifier conflict
     */
    Rule update(Rule rule);

    /**
     * Shifts rule priorities down to make room for insertions.
     *
     * <p>Increases the priority of rules with {@code priority >= priorityStart} by the specified
     * offset. This operation does nothing if there are no rules in the range that would be affected.
     *
     * @param priorityStart starting priority for the shift
     * @param offset how much to increase priorities by
     * @return number of rules updated, or -1 if no shift was needed
     */
    int shift(long priorityStart, long offset);

    /**
     * Swaps the priorities of two rules.
     *
     * @param id1 first rule id
     * @param id2 second rule id
     * @throws IllegalArgumentException if either rule does not exist
     */
    void swapPriority(String id1, String id2);

    /**
     * Finds a rule by id.
     *
     * @param id the rule id
     * @return the rule, or empty if not found
     */
    Optional<Rule> get(String id);

    /**
     * Deletes a rule.
     *
     * @param id the rule id
     * @return true if the rule was deleted, false if it didn't exist
     */
    boolean delete(String id);

    /**
     * Returns all rules.
     *
     * @return stream of all rules
     */
    Stream<Rule> getAll();

    /**
     * Returns rules matching the query criteria.
     *
     * @param query filter, pagination, and ordering criteria
     * @return stream of matching rules
     */
    Stream<Rule> getAll(RuleQuery<RuleFilter> query);

    /**
     * Finds a rule matching exact filter criteria.
     *
     * <p>Requires precise matching - wildcards (ANY filters) and default inclusions are not allowed.
     *
     * @param filter exact matching criteria
     * @return the matching rule, or empty if not found
     * @throws IllegalArgumentException if wildcard or default inclusion is used in the filter
     */
    Optional<Rule> getRule(RuleFilter filter) throws IllegalArgumentException;

    /**
     * Finds a rule by its priority.
     *
     * @param priority the priority to search for
     * @return the rule at that priority, or empty if none exists
     */
    Optional<Rule> getRuleByPriority(long priority);

    /**
     * Counts all rules.
     *
     * @return total number of rules
     */
    int count();

    /**
     * Counts rules matching the filter criteria.
     *
     * @param filter matching criteria
     * @return number of matching rules
     */
    int count(RuleFilter filter);

    /**
     * Sets access limits for a LIMIT rule.
     *
     * @param ruleId the rule id
     * @param limits spatial and access restrictions to apply
     * @throws IllegalArgumentException if the rule does not exist or is not a LIMIT rule
     */
    void setLimits(String ruleId, RuleLimits limits);

    /**
     * Gets layer-specific access details for a rule.
     *
     * @param rule the rule
     * @return layer details if the rule has a layer specified, empty otherwise
     */
    Optional<LayerDetails> getLayerDetails(Rule rule);

    /**
     * Gets layer-specific access details for a rule.
     *
     * @param ruleId the rule id
     * @return layer details if the rule has a layer specified, empty otherwise
     * @throws IllegalArgumentException if the rule does not exist or has no layer specified
     */
    Optional<LayerDetails> getLayerDetails(String ruleId);

    /**
     * Sets layer-specific access details for a rule.
     *
     * @param ruleId the rule id
     * @param detailsNew layer access details (attributes, CQL, styles, spatial filters)
     * @throws IllegalArgumentException if the rule does not exist, or if details are provided but
     *     the rule is not an ALLOW rule
     */
    void setLayerDetails(String ruleId, LayerDetails detailsNew);

    /**
     * Sets the allowed styles for a layer rule.
     *
     * @param ruleId the rule id
     * @param styles set of style names to allow
     * @throws IllegalArgumentException if the rule does not exist or has no layer specified
     */
    void setAllowedStyles(String ruleId, Set<String> styles);

    /**
     * Gets the allowed styles for a layer rule.
     *
     * @param ruleId the rule id
     * @return set of allowed style names, empty if no restrictions
     * @throws IllegalArgumentException if the rule does not exist or has no layer specified
     */
    Set<String> getAllowedStyles(String ruleId);

    /**
     * Deletes all rules.
     *
     * @return number of rules deleted
     */
    int deleteAll();
}
