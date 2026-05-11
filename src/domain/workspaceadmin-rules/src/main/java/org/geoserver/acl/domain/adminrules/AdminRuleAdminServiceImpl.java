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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.geoserver.acl.domain.filter.RuleQuery;

/**
 * Default implementation of {@link AdminRuleAdminService}.
 *
 * <p>Manages workspace admin rules with thread-safe priority operations and event publishing for
 * rule changes.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan (adapted to GeoServer ACL)
 */
@RequiredArgsConstructor
public class AdminRuleAdminServiceImpl implements AdminRuleAdminService {

    private final @NonNull AdminRuleRepository repository;

    @Setter
    private @NonNull Consumer<AdminRuleEvent> eventPublisher = r -> {
        // no-op
    };

    // =========================================================================
    // Basic operations
    // =========================================================================

    @Override
    public AdminRule insert(AdminRule rule) {
        return insert(rule, InsertPosition.FIXED);
    }

    @Override
    public AdminRule insert(AdminRule rule, InsertPosition position) {
        AdminRule created = repository.create(rule, position);
        eventPublisher.accept(AdminRuleEvent.created(created));
        return created;
    }

    @Override
    public AdminRule update(AdminRule rule) {
        if (null == rule.id()) {
            throw new IllegalArgumentException("AdminRule has no id");
        }

        AdminRule updated = repository.save(rule);
        eventPublisher.accept(AdminRuleEvent.updated(updated));
        return updated;
    }

    @Override
    public int shift(long priorityStart, long offset) {
        return repository.shiftPriority(priorityStart, offset);
    }

    @Override
    public void swap(@NonNull String id1, @NonNull String id2) {
        repository.swap(id1, id2);
        eventPublisher.accept(AdminRuleEvent.updated(id1, id2));
    }

    @Override
    public Optional<AdminRule> get(@NonNull String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<AdminRule> getFirstMatch(AdminRuleFilter filter) {
        return repository.findFirst(filter);
    }

    @Override
    public boolean delete(@NonNull String id) {
        boolean deleted = repository.deleteById(id);
        if (deleted) eventPublisher.accept(AdminRuleEvent.deleted(id));
        return deleted;
    }

    @Override
    public int deleteAll() {
        return repository.deleteAll();
    }

    @Override
    public Stream<AdminRule> getAll() {
        return repository.findAll();
    }

    @Override
    public Stream<AdminRule> getAll(RuleQuery<AdminRuleFilter> query) {
        return repository.findAll(query);
    }

    @Override
    public Optional<AdminRule> getRuleByPriority(long priority) {
        return repository.findOneByPriority(priority);
    }

    @Override
    public int count() {
        return repository.count();
    }

    @Override
    public int count(AdminRuleFilter filter) {
        return repository.count(filter);
    }

    @Override
    public boolean exists(@NonNull String id) {
        return repository.findById(id).isPresent();
    }
}
