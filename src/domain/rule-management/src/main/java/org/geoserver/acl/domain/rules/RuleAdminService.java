/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.rules;

import org.geoserver.acl.domain.filter.RuleQuery;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface RuleAdminService {

    void setEventPublisher(Consumer<RuleEvent> eventPublisher);

    /**
     * @throws RuleIdentifierConflictException if trying to insert a rule with the same {@link
     *     RuleIdentifier} than an existing one
     * @return the rule as created, with sanitized (converted to upper case) {@literal service} and
     *     {@literal request} identifier property values.
     */
    Rule insert(Rule rule);

    /**
     * @throws IllegalArgumentException if the rule has an {@link Rule#getId() id} set
     * @throws RuleIdentifierConflictException if trying to insert a rule with the same {@link
     *     RuleIdentifier} than an existing one
     * @return the rule as created, with sanitized (converted to upper case) {@literal service} and
     *     {@literal request} identifier property values.
     */
    Rule insert(Rule rule, InsertPosition position);

    /**
     * @throws IllegalArgumentException if the rule has {@code null} {@link Rule#getId() id} or does
     *     not exist
     * @throws RuleIdentifierConflictException if trying to update a rule with the same {@link
     *     RuleIdentifier} than an existing one
     */
    Rule update(Rule rule);

    /**
     * Shifts the priority of the rules having <TT>priority &gt;= priorityStart</TT> down by
     * <TT>offset</TT>.
     *
     * <p>The shift will not be performed if there are no Rules with priority: <br>
     * <tt> startPriority &lt;= priority &lt; startPriority + offset </TT>
     *
     * @return the number of rules updated, or -1 if no need to shift.
     */
    int shift(long priorityStart, long offset);

    /**
     * Swaps the priorities of two rules.
     *
     * @throws IllegalArgumentException if either rules does not exist
     */
    void swapPriority(String id1, String id2);

    Optional<Rule> get(String id);

    boolean delete(String id);

    Stream<Rule> getAll();

    /**
     * Return the Rules according to the query.
     *
     * @param query provides a filter predicate, paging, and priority offset
     */
    Stream<Rule> getAll(RuleQuery<RuleFilter> query);

    /**
     * Return a single Rule according to the filter.
     *
     * <p>Search for a precise rule match. No ANY filter is allowed. Name/id specification with
     * default inclusion is not allowed.
     *
     * @return the matching rule or null if not found
     * @throws BadRequestServiceEx if a wildcard type is used in filter
     */
    Optional<Rule> getRule(RuleFilter filter) throws IllegalArgumentException;

    /**
     * Search a Rule by priority.
     *
     * <p>Returns the rule having the requested priority, or null if none found.
     */
    Optional<Rule> getRuleByPriority(long priority) throws IllegalArgumentException;

    int count();

    /** Return the Rules count according to the filter. */
    int count(RuleFilter filter);

    /**
     * @param ruleId
     * @param limits
     * @throws IllegalArgumentException if Rule does not exist or is not of {@link GrantType#LIMIT
     *     LIMIT} type
     */
    void setLimits(String ruleId, RuleLimits limits) throws IllegalArgumentException;

    /**
     * @see #getLayerDetails(String)
     */
    Optional<LayerDetails> getLayerDetails(Rule rule);

    /**
     * @return The {@link LayerDetails} (possibly {@link Optional#empty() empty}) for the rule as
     *     long as the rule has {@link RuleIdentifier#getLayer() layer}
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    Optional<LayerDetails> getLayerDetails(String ruleId);

    /**
     * @throws IllegalArgumentException if the rule does not exist, or {@code detailsNew} is not
     *     null but the Rule's {@link RuleIdentifier#getAccess() access} is not {@link
     *     GrantType#ALLOW}
     */
    void setLayerDetails(String ruleId, LayerDetails detailsNew);

    /**
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    void setAllowedStyles(String ruleId, Set<String> styles);

    /**
     * @return The {@link LayerDetails#getAllowedStyles() layer allowed styles} (possibly empty) for
     *     the rule as long as the rule has {@link RuleIdentifier#getLayer() layer}
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    Set<String> getAllowedStyles(String ruleId);
}
