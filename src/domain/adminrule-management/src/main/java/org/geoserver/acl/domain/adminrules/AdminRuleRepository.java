/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import org.geoserver.acl.domain.filter.RuleQuery;

import java.util.Optional;
import java.util.stream.Stream;

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
