/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.domain;

import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface PriorityRepository<T> extends PagingAndSortingRepository<T, Long> {

    Optional<T> findOneByPriority(long priority);

    Optional<Long> findMaxPriority();

    Optional<Long> findMinPriority();

    int shiftPriority(long priorityStart, long offset);

    /**
     * Find the rule ids that'd be affected by a call to {@link #shiftPriority(long, long)} with the
     * same {@code priorityStart}
     */
    Stream<Long> streamIdsByShiftPriority(@Param("priorityStart") long priorityStart);

    void shiftPrioritiesBetween(long min, long max, long offset);

    /**
     * Find the rule ids that'd be affected by a call to {@link #shiftPrioritiesBetween(long, long,
     * long)} with the same {@code min} and {@code max} parameters
     */
    Stream<Long> streamIdsByShiftPriorityBetween(@Param("min") long min, @Param("max") long max);
}
