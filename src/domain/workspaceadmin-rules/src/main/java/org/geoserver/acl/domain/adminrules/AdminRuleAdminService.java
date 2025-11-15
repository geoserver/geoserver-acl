/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.adminrules;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.geoserver.acl.domain.filter.RuleQuery;

/**
 * Domain service for managing workspace {@link AdminRule}s.
 *
 * <p>Provides operations to create, update, query, and delete rules that control administrative
 * access to GeoServer workspaces. Admin rules determine whether users can modify workspace
 * configuration (layers, stores, etc.) as opposed to accessing data.
 *
 * <p>Like data access rules, admin rules are maintained in priority order, where lower priority
 * values take precedence during evaluation.
 */
public interface AdminRuleAdminService {

    /**
     * Sets the event publisher for rule change notifications.
     *
     * <p>Events are published after successful mutations (insert, update, delete). If the consumer
     * throws an exception, it will be logged and ignored to prevent the operation from failing.
     *
     * @param eventPublisher consumer that receives admin rule events
     */
    void setEventPublisher(Consumer<AdminRuleEvent> eventPublisher);

    /**
     * Creates a new admin rule with the specified priority.
     *
     * @param rule the rule to create (must not have an id)
     * @return the created rule with assigned id
     * @throws AdminRuleIdentifierConflictException if a rule with the same identifier already exists
     */
    AdminRule insert(AdminRule rule);

    /**
     * Creates a new admin rule at the specified position in the priority order.
     *
     * @param rule the rule to create (must not have an id)
     * @param position where to insert the rule in priority order
     * @return the created rule with assigned id and priority
     * @throws AdminRuleIdentifierConflictException if a rule with the same identifier already exists
     */
    AdminRule insert(AdminRule rule, InsertPosition position);

    /**
     * Updates an existing admin rule.
     *
     * @param rule the rule to update (must have an id)
     * @return the updated rule
     * @throws AdminRuleIdentifierConflictException if the update would create an identifier conflict
     * @throws IllegalArgumentException if the rule has no id or does not exist
     */
    AdminRule update(AdminRule rule);

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
     */
    void swap(String id1, String id2);

    /**
     * Finds an admin rule by id.
     *
     * @param id the rule id
     * @return the rule, or empty if not found
     */
    Optional<AdminRule> get(String id);

    /**
     * Finds the first rule matching the filter criteria.
     *
     * @param filter matching criteria
     * @return the first matching rule, or empty if none match
     */
    Optional<AdminRule> getFirstMatch(AdminRuleFilter filter);

    /**
     * Deletes an admin rule.
     *
     * @param id the rule id
     * @return true if the rule was deleted, false if it didn't exist
     */
    boolean delete(String id);

    /**
     * Returns all admin rules.
     *
     * @return stream of all rules
     */
    Stream<AdminRule> getAll();

    /**
     * Returns admin rules matching the query criteria.
     *
     * @param query filter and pagination criteria
     * @return stream of matching rules
     */
    Stream<AdminRule> getAll(RuleQuery<AdminRuleFilter> query);

    /**
     * Finds a rule by its priority.
     *
     * @param priority the priority to search for
     * @return the rule at that priority, or empty if none exists
     */
    Optional<AdminRule> getRuleByPriority(long priority);

    /**
     * Counts all admin rules.
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
    int count(AdminRuleFilter filter);

    /**
     * Checks whether a rule exists.
     *
     * @param id the rule id
     * @return true if the rule exists
     */
    boolean exists(String id);

    /**
     * Deletes all admin rules.
     *
     * @return number of rules deleted
     */
    int deleteAll();
}
