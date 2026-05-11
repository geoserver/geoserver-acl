/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.NonNull;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.MemoryPriorityRepository;
import org.geoserver.acl.domain.rules.PriorityResolver;
import org.geoserver.acl.domain.rules.PriorityResolver.Position;

/**
 * Reference {@link AdminRuleRepository} implementation, only for tests
 *
 * @since 1.0
 */
public class MemoryAdminRuleRepository extends MemoryPriorityRepository<AdminRule> implements AdminRuleRepository {

    private final AtomicLong idseq = new AtomicLong();

    private final PriorityResolver<AdminRule> priorityResolver;

    public MemoryAdminRuleRepository() {
        this.priorityResolver = new PriorityResolver<>(this);
    }

    public @Override long getPriority(AdminRule rule) {
        return rule.priority();
    }

    @Override
    public AdminRule create(AdminRule rule, InsertPosition position) {
        priorityLock.lock();
        try {
            if (null != rule.id()) throw new IllegalArgumentException("Rule has id");
            checkNoDups(rule);
            rule = rule.withId(String.valueOf(idseq.incrementAndGet()));

            long finalPriority = priorityResolver.resolveFinalPriority(rule.priority(), map(position));
            rule = rule.withPriority(finalPriority);
            rules.add(rule);
            return rule;
        } finally {
            priorityLock.unlock();
        }
    }

    private Position map(InsertPosition position) {
        switch (position) {
            case FIXED:
                return Position.FIXED;
            case FROM_END:
                return Position.FROM_END;
            case FROM_START:
                return Position.FROM_START;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public AdminRule save(AdminRule rule) {
        priorityLock.lock();
        try {
            if (null == rule.id()) throw new IllegalArgumentException("Rule has no id");
            checkNoDups(rule);
            final AdminRule current = getOrThrow(rule.id());

            final long finalPriority = priorityResolver.resolvePriorityUpdate(current.priority(), rule.priority());

            if (current.priority() != finalPriority) {
                rule = rule.withPriority(finalPriority);
                Optional<AdminRule> positionOccupied =
                        findOneByPriority(finalPriority).filter(r -> !r.id().equals(current.id()));
                if (positionOccupied.isPresent()) {
                    AdminRule other = positionOccupied.get();
                    rules.remove(current);
                    save(other.withPriority(other.priority() + 1));
                    rules.add(rule);
                } else {
                    replace(current, rule);
                }
            } else {
                replace(current, rule);
            }
            return rule;
        } finally {
            priorityLock.unlock();
        }
    }

    private AdminRule getOrThrow(@NonNull String id) {
        try {
            Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid id");
        }
        return findById(id).orElseThrow(() -> new IllegalArgumentException("AdminRule " + id + " does not exist"));
    }

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    private void checkNoDups(AdminRule rule) {
        rules.stream()
                .filter(r -> !r.id().equals(rule.id())
                        && r.access().equals(rule.access())
                        && r.identifier().equals(rule.identifier()))
                .findFirst()
                .ifPresent(duplicate -> {
                    throw new AdminRuleIdentifierConflictException(
                            "An AdminRule with the same identifier already exists: " + rule.toShortString());
                });
    }

    @Override
    public Optional<AdminRule> findById(String id) {
        return rules.stream().filter(r -> id.equals(r.id())).findFirst();
    }

    @Override
    public Stream<AdminRule> findAll() {
        return List.copyOf(rules).stream();
    }

    @Override
    public Stream<AdminRule> findAll(RuleQuery<AdminRuleFilter> query) {
        Stream<AdminRule> matches = findAll();
        if (query.getFilter().isPresent()) {
            AdminRuleFilter filter = query.getFilter().orElseThrow();
            matches = matches.filter(filter);
        }
        String nextId = query.getNextId();
        if (nextId != null) {
            final AtomicBoolean nextIdFound = new AtomicBoolean();
            matches = matches.peek(r -> {
                        if (r.id().equals(nextId)) {
                            nextIdFound.set(true);
                        }
                    })
                    .filter(r -> nextIdFound.get());
        }
        Integer limit = query.getLimit();
        if (limit != null) {
            matches = matches.limit(limit);
        }
        return matches;
    }

    @Override
    public Optional<AdminRule> findFirst(AdminRuleFilter adminRuleFilter) {
        return findAll(RuleQuery.of(adminRuleFilter).setLimit(1)).findFirst();
    }

    @Override
    public int count() {
        return rules.size();
    }

    @Override
    public int count(AdminRuleFilter filter) {
        return (int) findAll(RuleQuery.of(filter)).count();
    }

    @Override
    public int shiftPriority(long priorityStart, long offset) {
        priorityLock.lock();
        try {
            return super.shift(priorityStart, offset);
        } finally {
            priorityLock.unlock();
        }
    }

    @Override
    public void swap(String id1, String id2) {
        priorityLock.lock();
        try {
            AdminRule r1 = getOrThrow(id1);
            AdminRule r2 = getOrThrow(id2);

            AdminRule s1 = r1.withPriority(r2.priority());
            AdminRule s2 = r2.withPriority(r1.priority());
            rules.removeAll(List.of(r1, r2));
            rules.addAll(List.of(s1, s2));
        } finally {
            priorityLock.unlock();
        }
    }

    @Override
    public boolean deleteById(String id) {
        return rules.removeIf(r -> r.id().equals(id));
    }

    @Override
    public int deleteAll() {
        int count = rules.size();
        rules.clear();
        return count;
    }

    @Override
    public Optional<AdminRule> findOneByPriority(long priority) {
        return rules.stream().filter(r -> r.priority() == priority).findFirst();
    }

    @Override
    protected AdminRule withPriority(AdminRule r, long p) {
        return r.withPriority(p);
    }

    @Override
    protected String getId(AdminRule rule) {
        return rule.id();
    }
}
