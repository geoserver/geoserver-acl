/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.rules;

import lombok.NonNull;

import org.geoserver.acl.model.filter.RuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.InsertPosition;
import org.geoserver.acl.model.rules.LayerDetails;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleLimits;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Reference {@link RuleRepository} implementation, only for tests
 *
 * @since 1.0
 */
public class MemoryRuleRepository extends MemoryPriorityRepository<Rule> implements RuleRepository {

    private final AtomicLong idseq = new AtomicLong();

    private final Map<String, LayerDetails> layerDetails = new ConcurrentHashMap<>();

    private final PriorityResolver<Rule> priorityResolver;

    public MemoryRuleRepository() {
        this.priorityResolver = new PriorityResolver<>(this);
    }

    public @Override long getPriority(Rule rule) {
        return rule.getPriority();
    }

    @Override
    public Optional<Rule> findById(String id) {
        return rules.stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    @Override
    public boolean existsById(String id) {
        return rules.stream().anyMatch(r -> id.equals(r.getId()));
    }

    @Override
    public Rule create(Rule rule, InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("Rule has id");
        checkNoDups(rule);
        rule = rule.withId(String.valueOf(idseq.incrementAndGet()));

        long finalPriority = priorityResolver.resolveFinalPriority(rule.getPriority(), position);
        rule = rule.withPriority(finalPriority);
        rules.add(rule);
        return rule;
    }

    @Override
    public Rule save(Rule rule) {
        if (null == rule.getId()) throw new IllegalArgumentException("Rule has no id");
        checkNoDups(rule);
        final Rule current = getOrThrow(rule.getId());

        final long finalPriority =
                priorityResolver.resolvePriorityUpdate(current.getPriority(), rule.getPriority());

        if (current.getPriority() != finalPriority) {
            rule = rule.withPriority(finalPriority);
            Optional<Rule> positionOccupied =
                    findOneByPriority(finalPriority)
                            .filter(r -> !r.getId().equals(current.getId()));
            if (positionOccupied.isPresent()) {
                Rule other = positionOccupied.get();
                rules.remove(current);
                save(other.withPriority(other.getPriority() + 1));
                rules.add(rule);
            } else {
                replace(current, rule);
            }
        } else {
            replace(current, rule);
        }
        return rule;
    }

    /**
     * @throws RuleIdentifierConflictException
     */
    private void checkNoDups(Rule rule) {
        rules.stream()
                .filter(
                        r ->
                                r.getIdentifier().getAccess() != GrantType.LIMIT
                                        && !r.getId().equals(rule.getId())
                                        && r.getIdentifier().equals(rule.getIdentifier()))
                .findFirst()
                .ifPresent(
                        duplicate -> {
                            throw new RuleIdentifierConflictException(
                                    "A Rule with the same identifier already exists: "
                                            + rule.getIdentifier().toShortString());
                        });
    }

    @Override
    public boolean deleteById(@NonNull String id) {
        layerDetails.remove(id);
        return rules.removeIf(r -> id.equals(r.getId()));
    }

    @Override
    public int count() {
        return rules.size();
    }

    @Override
    public Stream<Rule> findAll() {
        return List.copyOf(rules).stream();
    }

    @Override
    public int count(RuleFilter filter) {
        return (int) rules.stream().filter(filter).count();
    }

    @Override
    public Stream<Rule> findAll(RuleQuery<RuleFilter> query) {
        RuleFilter filter = query.getFilter().orElse(RuleFilter.any());
        Stream<Rule> matches = rules.stream().filter(filter);
        Integer page = query.getPageNumber();
        Integer size = query.getPageSize();
        if (page != null && size != null) {
            int offset = page * size;
            matches = matches.skip(offset).limit(size);
        }
        return matches;
    }

    @Override
    public Optional<Rule> findOneByPriority(long priority) {
        return rules.stream().filter(r -> r.getPriority() == priority).findFirst();
    }

    @Override
    public void swap(String id1, String id2) {
        Rule r1 = getOrThrow(id1);
        Rule r2 = getOrThrow(id2);

        Rule s1 = r1.withPriority(r2.getPriority());
        Rule s2 = r2.withPriority(r1.getPriority());
        rules.removeAll(List.of(r1, r2));
        rules.addAll(List.of(s1, s2));
    }

    private Rule getOrThrow(@NonNull String id) {
        try {
            Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id");
        }
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule " + id + " does not exist"));
    }

    @Override
    public void setAllowedStyles(String ruleId, Set<String> styles) {
        Rule rule = getOrThrow(ruleId);
        if (null == rule.getIdentifier().getLayer()) {
            throw new IllegalArgumentException("Rule has no layer, can't set allowed styles");
        }

        LayerDetails ld =
                findLayerDetailsByRuleId(ruleId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Rule has no details associated"));

        ld = ld.toBuilder().allowedStyles(styles == null ? Set.of() : styles).build();
        setLayerDetails(ruleId, ld);
    }

    @Override
    public void setLimits(String ruleId, RuleLimits limits) {
        Rule rule = getOrThrow(ruleId);
        if (limits != null && rule.getIdentifier().getAccess() != GrantType.LIMIT) {
            throw new IllegalArgumentException("Rule is not of LIMIT type");
        }
        replace(rule, rule.withRuleLimits(limits));
    }

    @Override
    public void setLayerDetails(String ruleId, LayerDetails detailsNew) {
        Rule rule = getOrThrow(ruleId);
        if (detailsNew == null) layerDetails.remove(ruleId);
        else {
            if (rule.getIdentifier().getAccess() != GrantType.ALLOW) {
                throw new IllegalArgumentException("Rule is not of ALLOW type");
            }
            if (rule.getIdentifier().getLayer() == null) {
                throw new IllegalArgumentException("Rule does not refer to a fixed layer");
            }
            layerDetails.put(ruleId, detailsNew);
        }
    }

    @Override
    public Optional<LayerDetails> findLayerDetailsByRuleId(String ruleId) {
        return Optional.ofNullable(layerDetails.get(getOrThrow(ruleId).getId()));
    }

    protected @Override Rule withPriority(Rule rule, long priority) {
        return rule.withPriority(priority);
    }

    protected @Override String getId(Rule rule) {
        return rule.getId();
    }
}
