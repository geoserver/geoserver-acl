/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

@NoRepositoryBean
public interface PriorityRepository<T> extends PagingAndSortingRepository<T, Long> {

    Optional<T> findOneByPriority(long priority);

    int shiftPriority(long priorityStart, long offset);

    Optional<Long> findMaxPriority();

    Optional<Long> findMinPriority();

    void shiftPrioritiesBetween(long min, long max, long offset);
}
