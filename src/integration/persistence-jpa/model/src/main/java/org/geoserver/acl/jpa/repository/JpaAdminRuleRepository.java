/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.geoserver.acl.jpa.model.AdminRule;
import org.geoserver.acl.jpa.model.AdminRuleIdentifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@TransactionSupported
public interface JpaAdminRuleRepository
        extends JpaRepository<AdminRule, Long>, QuerydslPredicateExecutor<AdminRule>, PriorityRepository<AdminRule> {

    Sort naturalOrder = Sort.by("priority");

    @TransactionRequired
    @Modifying
    @Query("delete from AdminRule r where r.id=:id")
    int deleteById(@Param("id") long id);

    @Override
    Optional<AdminRule> findOneByPriority(long priority);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE AdminRule SET priority = priority + :offset WHERE priority >= :priorityStart")
    int shiftPriority(@Param("priorityStart") long priorityStart, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Query("SELECT r.id FROM AdminRule r WHERE priority >= :priorityStart")
    Stream<Long> streamIdsByShiftPriority(@Param("priorityStart") long priorityStart);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE AdminRule SET priority = priority + :offset WHERE priority BETWEEN :min AND :max")
    void shiftPrioritiesBetween(@Param("min") long min, @Param("max") long max, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Query("SELECT r.id FROM AdminRule r WHERE priority BETWEEN :min AND :max")
    Stream<Long> streamIdsByShiftPriorityBetween(@Param("min") long min, @Param("max") long max);

    @Override
    @Query("SELECT MAX(r.priority) FROM AdminRule r")
    Optional<Long> findMaxPriority();

    @Override
    @Query("SELECT MIN(r.priority) FROM AdminRule r")
    Optional<Long> findMinPriority();

    List<AdminRule> findAllByIdentifier(AdminRuleIdentifier identifier);
}
