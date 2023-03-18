/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.adminrules;

import lombok.NonNull;

import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.filter.AdminRuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.InsertPosition;
import org.geoserver.acl.rules.MemoryPriorityRepository;
import org.geoserver.acl.rules.PriorityResolver;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reference {@link AdminRuleRepository} implementation, only for tests
 *
 * @since 4.0
 */
public class MemoryAdminRuleRepository extends MemoryPriorityRepository<AdminRule>
        implements AdminRuleRepository {

    private final AtomicLong idseq = new AtomicLong();

    private final PriorityResolver<AdminRule> priorityResolver;

    public MemoryAdminRuleRepository() {
        this.priorityResolver = new PriorityResolver<>(this);
    }

    public @Override long getPriority(AdminRule rule) {
        return rule.getPriority();
    }

    @Override
    public AdminRule create(AdminRule rule, InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("Rule has id");
        checkNoDups(rule);
        rule = rule.withId(String.valueOf(idseq.incrementAndGet()));

        long finalPriority = priorityResolver.resolveFinalPriority(rule.getPriority(), position);
        rule = rule.withPriority(finalPriority);
        rules.add(rule);
        return rule;
    }

    @Override
    public AdminRule save(AdminRule rule) {
        if (null == rule.getId()) throw new IllegalArgumentException("Rule has no id");
        checkNoDups(rule);
        final AdminRule current = getOrThrow(rule.getId());

        final long finalPriority =
                priorityResolver.resolvePriorityUpdate(current.getPriority(), rule.getPriority());

        if (current.getPriority() != finalPriority) {
            rule = rule.withPriority(finalPriority);
            Optional<AdminRule> positionOccupied =
                    findOneByPriority(finalPriority)
                            .filter(r -> !r.getId().equals(current.getId()));
            if (positionOccupied.isPresent()) {
                AdminRule other = positionOccupied.get();
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

    private AdminRule getOrThrow(@NonNull String id) {
        try {
            Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id");
        }
        return findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("AdminRule " + id + " does not exist"));
    }

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    private void checkNoDups(AdminRule rule) {
        rules.stream()
                .filter(
                        r ->
                                !r.getId().equals(rule.getId())
                                        && r.getAccess().equals(rule.getAccess())
                                        && r.getIdentifier().equals(rule.getIdentifier()))
                .findFirst()
                .ifPresent(
                        duplicate -> {
                            throw new AdminRuleIdentifierConflictException(
                                    "An AdminRule with the same identifier already exists: "
                                            + rule.getIdentifier().toShortString());
                        });
    }

    @Override
    public Optional<AdminRule> findById(String id) {
        return rules.stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    @Override
    public List<AdminRule> findAll() {
        return List.copyOf(rules);
    }

    @Override
    public List<AdminRule> findAll(AdminRuleFilter filter) {
        return findAll(RuleQuery.of(filter));
    }

    @Override
    public List<AdminRule> findAll(RuleQuery<AdminRuleFilter> query) {
        return streamAll(query).collect(Collectors.toList());
    }

    private Stream<AdminRule> streamAll(RuleQuery<AdminRuleFilter> query) {
        Stream<AdminRule> matches = rules.stream();
        if (query.getFilter().isPresent()) {
            AdminRuleFilter filter = query.getFilter().orElseThrow();
            matches = matches.filter(filter);
        }
        Integer page = query.getPageNumber();
        Integer size = query.getPageSize();
        if (page != null && size != null) {
            int offset = page * size;
            matches = matches.skip(offset).limit(size);
        }
        return matches;
    }

    @Override
    public Optional<AdminRule> findFirst(AdminRuleFilter adminRuleFilter) {
        return streamAll(RuleQuery.of(adminRuleFilter)).findFirst();
    }

    @Override
    public int count() {
        return rules.size();
    }

    @Override
    public int count(AdminRuleFilter filter) {
        return (int) streamAll(RuleQuery.of(filter)).count();
    }

    @Override
    public int shiftPriority(long priorityStart, long offset) {
        return super.shift(priorityStart, offset);
    }

    @Override
    public void swap(String id1, String id2) {
        AdminRule r1 = getOrThrow(id1);
        AdminRule r2 = getOrThrow(id2);

        AdminRule s1 = r1.withPriority(r2.getPriority());
        AdminRule s2 = r2.withPriority(r1.getPriority());
        rules.removeAll(List.of(r1, r2));
        rules.addAll(List.of(s1, s2));
    }

    @Override
    public boolean deleteById(String id) {
        return rules.removeIf(r -> r.getId().equals(id));
    }

    @Override
    public Optional<AdminRule> findOneByPriority(long priority) {
        return rules.stream().filter(r -> r.getPriority() == priority).findFirst();
    }

    @Override
    protected AdminRule withPriority(AdminRule r, long p) {
        return r.withPriority(p);
    }

    @Override
    protected String getId(AdminRule rule) {
        return rule.getId();
    }
}
