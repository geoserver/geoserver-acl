/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import org.geoserver.acl.jpa.model.AdminRule;
import org.geoserver.acl.jpa.model.AdminRuleIdentifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@TransactionSupported
public interface JpaAdminRuleRepository
        extends JpaRepository<AdminRule, Long>,
                QuerydslPredicateExecutor<AdminRule>,
                PriorityRepository<AdminRule> {

    Sort naturalOrder = Sort.by("priority");

    @TransactionRequired
    @Modifying
    @Query("delete from Rule r where r.id=:id")
    int deleteById(@Param("id") long id);

    @Query("SELECT r FROM AdminRule r ORDER BY priority")
    List<AdminRule> findAllNaturalOrder();

    default List<AdminRule> findAllNaturalOrder(com.querydsl.core.types.Predicate predicate) {

        Iterable<AdminRule> matches = findAll(predicate, naturalOrder);

        if (matches instanceof List) return (List<AdminRule>) matches;

        return StreamSupport.stream(matches.spliterator(), false).collect(Collectors.toList());
    }

    @Query("SELECT r FROM AdminRule r ORDER BY priority")
    Page<AdminRule> findAllNaturalOrder(Pageable pageable);

    default Page<AdminRule> findAllNaturalOrder(
            com.querydsl.core.types.Predicate predicate, Pageable pageable) {

        if (pageable.isPaged()) {
            PageRequest sortingPageRequest =
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), naturalOrder);
            return findAll(predicate, sortingPageRequest);
        }
        Iterable<AdminRule> matches = findAll(predicate, naturalOrder);
        List<AdminRule> contents;
        if (matches instanceof List) contents = (List<AdminRule>) matches;
        else
            contents =
                    StreamSupport.stream(matches.spliterator(), false).collect(Collectors.toList());
        return new PageImpl<>(contents);
    }

    @Override
    Optional<AdminRule> findOneByPriority(long priority);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE AdminRule SET priority = priority + :offset WHERE priority >= :priorityStart")
    int shiftPriority(@Param("priorityStart") long priorityStart, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            "UPDATE AdminRule SET priority = priority + :offset WHERE priority BETWEEN :min AND :max")
    void shiftPrioritiesBetween(
            @Param("min") long min, @Param("max") long max, @Param("offset") long offset);

    @Override
    @Query("SELECT MAX(r.priority) FROM AdminRule r")
    Optional<Long> findMaxPriority();

    @Override
    @Query("SELECT MIN(r.priority) FROM AdminRule r")
    Optional<Long> findMinPriority();

    List<AdminRule> findAllByIdentifier(AdminRuleIdentifier identifier);
}
