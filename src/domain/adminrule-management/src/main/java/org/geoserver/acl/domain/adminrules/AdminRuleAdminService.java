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
import org.geoserver.acl.domain.filter.RuleQuery;

public interface AdminRuleAdminService {

    void setEventPublisher(Consumer<AdminRuleEvent> eventPublisher);

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    AdminRule insert(AdminRule rule);

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    AdminRule insert(AdminRule rule, InsertPosition position);

    /**
     * @throws AdminRuleIdentifierConflictException
     */
    AdminRule update(AdminRule rule);

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

    void swap(String id1, String id2);

    /**
     * @throws IllegalArgumentException if the filter produces more than one result
     */
    Optional<AdminRule> get(String id);

    Optional<AdminRule> getFirstMatch(AdminRuleFilter filter);

    boolean delete(String id);

    Stream<AdminRule> getAll();

    /**
     * Return the Rules according to the filter.
     *
     * @param page used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>entries</TT> should be defined.
     * @param entries used for retrieving paged data, may be null if not used. If not null, also
     *     <TT>page</TT> should be defined.
     */
    Stream<AdminRule> getAll(RuleQuery<AdminRuleFilter> query);

    /**
     * Search a Rule by priority.
     *
     * <p>Returns the rule having the requested priority, or empty if none found.
     */
    Optional<AdminRule> getRuleByPriority(long priority);

    int count();

    /** Return the Rules count according to the filter. */
    int count(AdminRuleFilter filter);

    boolean exists(String id);

    int deleteAll();
}
