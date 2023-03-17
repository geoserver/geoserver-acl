/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import org.geoserver.acl.jpa.model.Rule;
import org.geoserver.acl.jpa.model.RuleIdentifier;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@TransactionSupported
public interface JpaRuleRepository
        extends JpaRepository<Rule, Long>,
                QuerydslPredicateExecutor<Rule>,
                PriorityRepository<Rule> {

    Sort naturalOrder = Sort.by("priority");

    @TransactionRequired
    @Modifying
    @Query("delete from Rule r where r.id=:id")
    int deleteById(@Param("id") long id);

    @Query("SELECT r FROM Rule r ORDER BY priority")
    List<Rule> findAllNaturalOrder();

    default List<Rule> findAllNaturalOrder(com.querydsl.core.types.Predicate predicate) {

        Iterable<Rule> matches = findAll(predicate, naturalOrder);

        if (matches instanceof List) return (List<Rule>) matches;

        return StreamSupport.stream(matches.spliterator(), false).collect(Collectors.toList());
    }

    @Query("SELECT r FROM Rule r ORDER BY priority")
    Page<Rule> findAllNaturalOrder(Pageable pageable);

    default Page<Rule> findAllNaturalOrder(
            com.querydsl.core.types.Predicate predicate, Pageable pageable) {

        if (pageable.isPaged()) {
            PageRequest sortingPageRequest =
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), naturalOrder);
            return findAll(predicate, sortingPageRequest);
        }
        Iterable<Rule> matches = findAll(predicate, naturalOrder);
        List<Rule> contents;
        if (matches instanceof List) contents = (List<Rule>) matches;
        else
            contents =
                    StreamSupport.stream(matches.spliterator(), false).collect(Collectors.toList());
        return new PageImpl<>(contents);
    }

    @Override
    Optional<Rule> findOneByPriority(long priority);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Rule SET priority = priority + :offset WHERE priority >= :priorityStart")
    int shiftPriority(@Param("priorityStart") long priorityStart, @Param("offset") long offset);

    @Override
    @TransactionRequired
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Rule SET priority = priority + :offset WHERE priority BETWEEN :min AND :max")
    void shiftPrioritiesBetween(
            @Param("min") long min, @Param("max") long max, @Param("offset") long offset);

    @Override
    @Query("SELECT MAX(r.priority) FROM Rule r")
    Optional<Long> findMaxPriority();

    @Override
    @Query("SELECT MIN(r.priority) FROM Rule r")
    Optional<Long> findMinPriority();

    List<Rule> findAllByIdentifier(RuleIdentifier identifier);
}
