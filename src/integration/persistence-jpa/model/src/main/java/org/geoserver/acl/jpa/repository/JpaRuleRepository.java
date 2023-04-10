/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import org.geoserver.acl.jpa.model.Rule;
import org.geoserver.acl.jpa.model.RuleIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@TransactionSupported
public interface JpaRuleRepository
        extends JpaRepository<Rule, Long>,
                QuerydslPredicateExecutor<Rule>,
                PriorityRepository<Rule> {

    @Override
    Optional<Rule> findOneByPriority(long priority);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Rule SET priority = priority + :offset WHERE priority >= :priorityStart")
    int shiftPriority(@Param("priorityStart") long priorityStart, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Query("SELECT r.id FROM Rule r WHERE priority >= :priorityStart")
    Stream<Long> streamIdsByShiftPriority(@Param("priorityStart") long priorityStart);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Rule SET priority = priority + :offset WHERE priority BETWEEN :min AND :max")
    void shiftPrioritiesBetween(
            @Param("min") long min, @Param("max") long max, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Query("SELECT r.id FROM Rule r WHERE priority BETWEEN :min AND :max")
    Stream<Long> streamIdsByShiftPriorityBetween(@Param("min") long min, @Param("max") long max);

    @Override
    @Query("SELECT MAX(r.priority) FROM Rule r")
    Optional<Long> findMaxPriority();

    @Override
    @Query("SELECT MIN(r.priority) FROM Rule r")
    Optional<Long> findMinPriority();

    List<Rule> findAllByIdentifier(RuleIdentifier identifier);
}
