/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.adminrules;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.filter.AdminRuleFilter;
import org.geoserver.acl.model.filter.RuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.InsertPosition;

import java.util.List;
import java.util.Optional;

/**
 * Operations on {@link AdminRule AdminRule}s.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Slf4j
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

    /**
     * Return a single Rule according to the filter.
     *
     * <p>Search for a precise rule match. No ANY filter is allowed. Name/id specification with
     * default inclusion is not allowed.
     *
     * @return the matching rule or null if not found
     * @throws IllegalArgumentException if a wildcard type is used in filter or matches more than
     *     one rule
     */
    public Optional<AdminRule> getRule(AdminRuleFilter filter) {
        return repository.findOne(filter);
    }

    public Optional<AdminRule> getFirstMatch(AdminRuleFilter filter) {
        return repository.findFirst(filter);
    }

    public boolean delete(@NonNull String id) {
        return repository.deleteById(id);
    }

    public int deleteRulesByUser(@NonNull String username) {
        AdminRuleFilter filter = new AdminRuleFilter();
        filter.setUser(username);
        filter.getUser().setIncludeDefault(false);
        int deletedCount = repository.delete(filter);
        log.info("Removed {} AdminRules for user {}", deletedCount, username);
        return deletedCount;
    }

    public int deleteRulesByRole(String rolename) {
        AdminRuleFilter filter = new AdminRuleFilter();
        filter.setRole(rolename);
        filter.getRole().setIncludeDefault(false);
        int deletedCount = repository.delete(filter);
        log.info("Removed {} AdminRules for role {}", deletedCount, rolename);
        return deletedCount;
    }

    public int deleteRulesByInstance(long instanceId) {
        AdminRuleFilter filter = new AdminRuleFilter();
        filter.setInstance(instanceId);
        filter.getInstance().setIncludeDefault(false);
        int deletedCount = repository.delete(filter);
        log.info("Removed {} AdminRules for instance {}", deletedCount, instanceId);
        return deletedCount;
    }

    public int deleteRules(@NonNull AdminRuleFilter filter) {
        int deletedCount = repository.delete(filter);
        log.info("Removed {} AdminRules for matching {}", deletedCount, filter);
        return deletedCount;
    }

    public List<AdminRule> getAll() {
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
    public List<AdminRule> getList(AdminRuleFilter filter, Integer page, Integer entries) {

        RuleQuery<AdminRuleFilter> query =
                RuleQuery.of(filter).setPageNumber(page).setPageSize(entries);
        return repository.findAll(query);
    }

    /**
     * Return the Rules according to the priority.
     *
     * <p>Returns the rules having priority greater or equal to <code>priority</code>
     *
     * @param page used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>entries</TT> should be defined.
     * @param entries used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>page</TT> should be defined.
     */
    public List<AdminRule> getRulesByPriority(long priority, Integer page, Integer entries) {

        RuleQuery<AdminRuleFilter> query = RuleQuery.of();
        query.setPriorityOffset(priority).setPageNumber(page).setPageSize(entries);

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

    public int getCountAll() {
        return repository.count();
    }

    /** Return the Rules count according to the filter. */
    public int count(AdminRuleFilter filter) {
        return repository.count(filter);
    }

    public boolean exists(@NonNull String id) {
        return repository.findById(id).isPresent();
    }

    // =========================================================================
    // Search stuff

    // private Search buildRuleSearch(RuleFilter filter) {
    // Search searchCriteria = new Search(AdminRule.class);
    //
    // if (filter != null) {
    // addStringCriteria(searchCriteria, "username", filter.getUser());
    // addStringCriteria(searchCriteria, "rolename", filter.getRole());
    // addCriteria(searchCriteria, "instance", filter.getInstance());
    //
    // addStringCriteria(searchCriteria, "workspace", filter.getWorkspace());
    // }
    //
    // return searchCriteria;
    // }

    // =========================================================================

    // private Search buildFixedRuleSearch(RuleFilter filter) {
    // Search searchCriteria = new Search(AdminRule.class);
    //
    // if (filter != null) {
    // addFixedStringCriteria(searchCriteria, "username", filter.getUser());
    // addFixedStringCriteria(searchCriteria, "rolename", filter.getRole());
    // addFixedCriteria(searchCriteria, "instance", filter.getInstance());
    //
    // addFixedStringCriteria(searchCriteria, "workspace", filter.getWorkspace());
    // }
    //
    // return searchCriteria;
    // }

    // ==========================================================================

}
