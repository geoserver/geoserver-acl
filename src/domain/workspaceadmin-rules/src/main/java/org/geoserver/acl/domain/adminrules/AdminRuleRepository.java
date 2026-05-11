/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import java.util.Optional;
import java.util.stream.Stream;
import org.geoserver.acl.domain.filter.RuleQuery;

/**
 * Persistence port for {@link AdminRule}s.
 *
 * <p>Repository abstraction in the hexagonal architecture: the domain defines the contract,
 * infrastructure modules (for example the JPA adapter) provide the implementation. Exposes
 * CRUD, priority-ordered queries, priority management (shift, swap), and filtered lookups
 * driven by {@link AdminRuleFilter}. {@link AdminRuleAdminService} sits on top of this port
 * and adds business rules and event publishing.
 *
 * @see AdminRule
 * @see AdminRuleAdminService
 */
public interface AdminRuleRepository {

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    AdminRule create(AdminRule rule, InsertPosition position);

    /**
     * @param rule
     * @return
     * @throws IllegalArgumentException if the rule does not exist
     * @throws AdminRuleIdentifierConflictException
     */
    AdminRule save(AdminRule rule);

    Optional<AdminRule> findById(String id);

    Stream<AdminRule> findAll();

    Stream<AdminRule> findAll(RuleQuery<AdminRuleFilter> query);

    Optional<AdminRule> findFirst(AdminRuleFilter adminRuleFilter);

    int count();

    int count(AdminRuleFilter filter);

    int shiftPriority(long priorityStart, long offset);

    void swap(String id1, String id2);

    boolean deleteById(String id);

    int deleteAll();

    Optional<AdminRule> findOneByPriority(long priority);
}
