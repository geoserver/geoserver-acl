/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.geoserver.acl.domain.filter.RuleQuery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operations on {@link Rule Rule}s.
 *
 * <p><B>Note:</B> <TT>service</TT> and <TT>request</TT> params are usually set by the client, and
 * by OGC specs they are not case sensitive, so we're going to turn all of them uppercase. See also
 * {@link org.geoserver.acl.authorization.AuthorizationService}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@RequiredArgsConstructor
public class RuleAdminServiceImpl implements RuleAdminService {

    private final @NonNull RuleRepository ruleRepository;

    @Setter
    private @NonNull Consumer<RuleEvent> eventPublisher =
            r -> {
                // no-op
            };

    // =========================================================================
    // Basic operations
    // =========================================================================

    /**
     * @throws RuleIdentifierConflictException if trying to insert a rule with the same {@link
     *     RuleIdentifier} than an existing one
     * @return the rule as created, with sanitized (converted to upper case) {@literal service} and
     *     {@literal request} identifier property values.
     */
    @Override
    public Rule insert(@NonNull Rule rule) {
        return insert(rule, InsertPosition.FIXED);
    }

    /**
     * @throws IllegalArgumentException if the rule has an {@link Rule#getId() id} set
     * @throws RuleIdentifierConflictException if trying to insert a rule with the same {@link
     *     RuleIdentifier} than an existing one
     * @return the rule as created, with sanitized (converted to upper case) {@literal service} and
     *     {@literal request} identifier property values.
     */
    @Override
    public Rule insert(@NonNull Rule rule, @NonNull InsertPosition position) {
        if (null != rule.getId())
            throw new IllegalArgumentException("a new Rule must not have id, got " + rule.getId());

        rule = sanitizeFields(rule);
        Rule created = ruleRepository.create(rule, position);
        eventPublisher.accept(RuleEvent.created(created));
        return created;
    }

    /**
     * @throws IllegalArgumentException if the rule has {@code null} {@link Rule#getId() id} or does
     *     not exist
     * @throws RuleIdentifierConflictException if trying to update a rule with the same {@link
     *     RuleIdentifier} than an existing one
     */
    @Override
    public Rule update(@NonNull Rule rule) {
        if (null == rule.getId()) {
            throw new IllegalArgumentException("Rule has no id");
        }

        rule = sanitizeFields(rule);
        Rule updated = ruleRepository.save(rule);
        eventPublisher.accept(RuleEvent.updated(updated));
        return updated;
    }

    /**
     * Shifts the priority of the rules having <TT>priority &gt;= priorityStart</TT> down by
     * <TT>offset</TT>.
     *
     * <p>The shift will not be performed if there are no Rules with priority: <br>
     * <tt> startPriority &lt;= priority &lt; startPriority + offset </TT>
     *
     * @return the number of rules updated, or -1 if no need to shift.
     */
    @Override
    public int shift(long priorityStart, long offset) {
        if (offset <= 0) {
            throw new IllegalArgumentException("Positive offset required");
        }
        return ruleRepository.shift(priorityStart, offset);
    }

    /**
     * Swaps the priorities of two rules.
     *
     * @throws IllegalArgumentException if either rules does not exist
     */
    @Override
    public void swapPriority(String id1, String id2) {
        ruleRepository.swap(id1, id2);
        eventPublisher.accept(RuleEvent.updated(id1, id2));
    }

    /**
     * <TT>service</TT> and <TT>request</TT> params are usually set by the client, and by OGC specs
     * they are not case sensitive, so we're going to turn all of them uppercase. See also {@link
     * AuthorizationService}.
     */
    protected Rule sanitizeFields(Rule rule) {
        if (rule.getPriority() < 0)
            throw new IllegalArgumentException(
                    "Negative priority is not allowed: " + rule.getPriority());

        // read class' javadoc
        RuleIdentifier identifier = rule.getIdentifier();
        if (identifier.getService() != null) {
            identifier = identifier.withService(identifier.getService().toUpperCase());
        }
        if (identifier.getRequest() != null) {
            identifier = identifier.withRequest(identifier.getRequest().toUpperCase());
        }
        return rule.withIdentifier(identifier);
    }

    @Override
    public Optional<Rule> get(@NonNull String id) {
        return ruleRepository.findById(id);
    }

    @Override
    public boolean delete(@NonNull String id) {
        boolean deleted = ruleRepository.deleteById(id);
        if (deleted) {
            eventPublisher.accept(RuleEvent.deleted(id));
        }
        return deleted;
    }

    @Override
    public int deleteAll() {
        return ruleRepository.deleteAll();
    }

    @Override
    public Stream<Rule> getAll() {
        return ruleRepository.findAll();
    }

    /**
     * Return the Rules according to the query.
     *
     * @param query provides a filter predicate, paging, and priority offset
     */
    @Override
    public Stream<Rule> getAll(@NonNull RuleQuery<RuleFilter> query) {
        return ruleRepository.findAll(query);
    }

    /**
     * Return a single Rule according to the filter.
     *
     * <p>Search for a precise rule match. No ANY filter is allowed. Name/id specification with
     * default inclusion is not allowed.
     *
     * @return the matching rule or null if not found
     * @throws BadRequestServiceEx if a wildcard type is used in filter
     */
    @Override
    public Optional<Rule> getRule(@NonNull RuleFilter filter) throws IllegalArgumentException {
        RuleQuery<RuleFilter> query = RuleQuery.of(filter).setLimit(2);
        List<Rule> found = ruleRepository.findAll(query).collect(Collectors.toList());
        if (found.size() > 1) {
            throw new IllegalArgumentException(
                    "Unexpected rule count for filter " + filter + " : " + found.size());
        }

        return Optional.ofNullable(found.isEmpty() ? null : found.get(0));
    }

    /**
     * Search a Rule by priority.
     *
     * <p>Returns the rule having the requested priority, or null if none found.
     */
    @Override
    public Optional<Rule> getRuleByPriority(long priority) throws IllegalArgumentException {
        return ruleRepository.findOneByPriority(priority);
    }

    @Override
    public int count() {
        return ruleRepository.count();
    }

    /** Return the Rules count according to the filter. */
    @Override
    public int count(@NonNull RuleFilter filter) {
        return ruleRepository.count(filter);
    }

    // =========================================================================
    // Limits
    // =========================================================================

    /**
     * @param ruleId
     * @param limits
     * @throws IllegalArgumentException if Rule does not exist or is not of {@link GrantType#LIMIT
     *     LIMIT} type
     */
    @Override
    public void setLimits(@NonNull String ruleId, RuleLimits limits)
            throws IllegalArgumentException {

        ruleRepository.setLimits(ruleId, limits);
        eventPublisher.accept(RuleEvent.updated(ruleId));
    }

    // =========================================================================
    // Details
    // =========================================================================

    /**
     * @see #getLayerDetails(String)
     */
    @Override
    public Optional<LayerDetails> getLayerDetails(@NonNull Rule rule) {
        Objects.requireNonNull(rule.getId());
        return getLayerDetails(rule.getId());
    }

    /**
     * @return The {@link LayerDetails} (possibly {@link Optional#empty() empty}) for the rule as
     *     long as the rule has {@link RuleIdentifier#getLayer() layer}
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    @Override
    public Optional<LayerDetails> getLayerDetails(@NonNull String ruleId) {
        return ruleRepository.findLayerDetailsByRuleId(ruleId);
    }

    /**
     * @throws IllegalArgumentException if the rule does not exist, or {@code detailsNew} is not
     *     null but the Rule's {@link RuleIdentifier#getAccess() access} is not {@link
     *     GrantType#ALLOW}
     */
    @Override
    public void setLayerDetails(@NonNull String ruleId, LayerDetails detailsNew) {
        ruleRepository.setLayerDetails(ruleId, detailsNew);
        eventPublisher.accept(RuleEvent.updated(ruleId));
    }

    /**
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    @Override
    public void setAllowedStyles(@NonNull String ruleId, Set<String> styles) {
        ruleRepository.setAllowedStyles(ruleId, styles);
        eventPublisher.accept(RuleEvent.updated(ruleId));
    }

    /**
     * @return The {@link LayerDetails#getAllowedStyles() layer allowed styles} (possibly empty) for
     *     the rule as long as the rule has {@link RuleIdentifier#getLayer() layer}
     * @throws IllegalArgumentException if the rule does not exist or has no {@link
     *     RuleIdentifier#getLayer() layer} set
     */
    @Override
    public Set<String> getAllowedStyles(@NonNull String ruleId) {
        return getLayerDetails(ruleId).map(LayerDetails::getAllowedStyles).orElse(Set.of());
    }

    // ==========================================================================

}
