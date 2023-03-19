/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.repository;

import lombok.Getter;

import org.geoserver.acl.jpa.repository.PriorityRepository;
import org.geoserver.acl.model.rules.InsertPosition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

class PriorityResolver<T> {

    private final PriorityRepository<T> jparepo;
    private final Function<T, Long> priorityExtractor;

    private final @Getter Set<Long> updatedIds = new TreeSet<>();

    PriorityResolver(PriorityRepository<T> jparepo, Function<T, Long> priorityExtractor) {
        this.jparepo = jparepo;
        this.priorityExtractor = priorityExtractor;
    }

    public long resolveFinalPriority(long priority, InsertPosition position) {
        long finalPriority;
        switch (position) {
            case FIXED:
                {
                    finalPriority = resolveFixedPriority(priority);
                    break;
                }
            case FROM_START:
                {
                    long positionFromStart = priority;
                    finalPriority = resolvePriorityFromStart(positionFromStart);
                    break;
                }
            case FROM_END:
                {
                    long positionFromEnd = priority;
                    finalPriority = resolvePriorityFromEnd(positionFromEnd);
                    break;
                }
            default:
                throw new IllegalStateException("Unknown InsertPosition " + position);
        }
        return finalPriority;
    }

    private long resolveFixedPriority(final long requestedPriority) {
        final boolean priorityUnset = 0L == requestedPriority;
        if (priorityUnset) {
            return jparepo.findMaxPriority().orElse(0L) + 1;
        }
        jparepo.findOneByPriority(requestedPriority)
                .ifPresent(
                        r -> {
                            jparepo.streamIdsByShiftPriority(requestedPriority)
                                    .forEach(updatedIds::add);
                            jparepo.shiftPriority(requestedPriority, 1);
                        });
        return requestedPriority;
    }

    /**
     * Used by updates when the priority's changed, to only move the affected rules. E.g. if the're
     * are rules with priorities 1 to 10, and rule with priority 5 is being updated to priority 2,
     * only 3 and 4 should be shifted by one, leaving the updated with priority 2, the others with 4
     * and 5, and the ones with priority > 5 not being shifted at all.
     *
     * @param currentPriority the priority the rule has in the database
     * @param requestedPriority the priority it's being updated to
     * @return the final priority, may differ from the requested one only if it's bellow the minimum
     *     or above the maximum existing priorities
     */
    public long resolvePriorityUpdate(final long currentPriority, final long requestedPriority) {
        if (currentPriority == requestedPriority) {
            return currentPriority;
        }
        if (0L == requestedPriority) {
            return jparepo.findMaxPriority().orElse(0L) + 1;
        }

        Long min = Math.min(currentPriority, requestedPriority);
        Long max = Math.max(currentPriority, requestedPriority) - 1;
        jparepo.streamIdsByShiftPriorityBetween(min, max).forEach(updatedIds::add);
        jparepo.shiftPrioritiesBetween(min, max, 1);
        return requestedPriority;
    }

    private long resolvePriorityFromStart(final long requestedPosition) {
        // find the rule at index $min + requestedPosition
        if (0 == requestedPosition) {
            Optional<Long> min = jparepo.findMinPriority();
            if (min.isPresent()) {
                jparepo.shiftPriority(min.get(), 1);
                return min.get();
            }
            return 1L;
        }
        Optional<Long> found = findNthPriorityByOrder(requestedPosition, Direction.ASC);
        if (found.isPresent()) {
            jparepo.shiftPriority(found.get(), 1);
            return found.get();
        }

        // there are not enough, get the max + 1
        return 1 + jparepo.findMaxPriority().orElse(0L);
    }

    private long resolvePriorityFromEnd(final long requestedPosition) {
        // find the rule at index $max - requestedPosition
        if (0 == requestedPosition) {
            return jparepo.findMaxPriority().map(max -> 1 + max).orElse(1L);
        }

        Optional<Long> found = findNthPriorityByOrder(requestedPosition, Direction.DESC);
        if (found.isPresent()) {
            jparepo.streamIdsByShiftPriority(found.get()).forEach(updatedIds::add);
            jparepo.shiftPriority(found.get(), 1);
            return found.get();
        }

        // there are not enough rules from the bottom, use the minimum one
        Optional<Long> min = jparepo.findMinPriority();
        min.ifPresent(
                minPriority -> {
                    jparepo.streamIdsByShiftPriority(minPriority).forEach(updatedIds::add);
                    jparepo.shiftPriority(minPriority, 1);
                });
        return min.orElse(1L);
    }

    /**
     * @param position 1-based position
     * @param direction the direction to sort by
     * @return the priority of the rule at the Nth position in the requested sort order, or empty if
     *     there's no rule at that position
     */
    private Optional<Long> findNthPriorityByOrder(long position, Direction direction) {
        if (position < 1) throw new IllegalArgumentException("find nth position is 1-based");
        int page = (int) position - 1;
        int pageSize = 1;
        Sort sort = Sort.by(direction, "priority");
        PageRequest singlePageReq = PageRequest.of(page, pageSize, sort);

        return jparepo.findAll(singlePageReq).getContent().stream()
                .findFirst()
                .map(priorityExtractor);
    }
}
