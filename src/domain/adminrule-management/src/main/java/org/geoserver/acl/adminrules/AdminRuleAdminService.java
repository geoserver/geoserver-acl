/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.adminrules;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.filter.AdminRuleFilter;
import org.geoserver.acl.model.filter.RuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.InsertPosition;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Operations on {@link AdminRule AdminRule}s.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@RequiredArgsConstructor
public class AdminRuleAdminService {

    private final @NonNull AdminRuleRepository repository;

    // =========================================================================
    // Basic operations
    // =========================================================================

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    public AdminRule insert(AdminRule rule) {
        return insert(rule, InsertPosition.FIXED);
    }

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    public AdminRule insert(AdminRule rule, InsertPosition position) {
        try {
            return repository.create(rule, position);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    public AdminRule update(AdminRule rule) {
        if (null == rule.getId()) {
            throw new IllegalArgumentException("AdminRule has no id");
        }

        return repository.save(rule);
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
    public int shift(long priorityStart, long offset) {
        return repository.shiftPriority(priorityStart, offset);
    }

    public void swap(@NonNull String id1, @NonNull String id2) {
        repository.swap(id1, id2);
    }

    /**
     * @throws IllegalArgumentException if the filter produces more than one result
     */
    public Optional<AdminRule> get(@NonNull String id) {
        return repository.findById(id);
    }

    public Optional<AdminRule> getFirstMatch(AdminRuleFilter filter) {
        return repository.findFirst(filter);
    }

    public boolean delete(@NonNull String id) {
        return repository.deleteById(id);
    }

    public Stream<AdminRule> getAll() {
        return repository.findAll();
    }

    /**
     * Return the Rules according to the filter.
     *
     * @param page used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>entries</TT> should be defined.
     * @param entries used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>page</TT> should be defined.
     * @see RuleReaderService#getMatchingRules(RuleFilter)
     */
    public Stream<AdminRule> getAll(RuleQuery<AdminRuleFilter> query) {
        return repository.findAll(query);
    }

    /**
     * Search a Rule by priority.
     *
     * <p>Returns the rule having the requested priority, or empty if none found.
     */
    public Optional<AdminRule> getRuleByPriority(long priority) {
        return repository.findOneByPriority(priority);
    }

    public int count() {
        return repository.count();
    }

    /** Return the Rules count according to the filter. */
    public int count(AdminRuleFilter filter) {
        return repository.count(filter);
    }

    public boolean exists(@NonNull String id) {
        return repository.findById(id).isPresent();
    }
}
