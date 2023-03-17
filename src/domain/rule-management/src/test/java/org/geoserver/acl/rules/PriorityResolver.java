/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.rules;

import lombok.NonNull;

import org.geoserver.acl.model.rules.InsertPosition;

import java.util.Optional;

public class PriorityResolver<T> {

    private final MemoryPriorityRepository<T> repo;

    public PriorityResolver(@NonNull MemoryPriorityRepository<T> repo) {
        this.repo = repo;
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
            return repo.maxPriority().orElse(0L) + 1;
        }
        repo.findByPriority(requestedPriority)
                .ifPresent(
                        r -> {
                            repo.shift(requestedPriority, 1);
                        });
        return requestedPriority;
    }

    public long resolvePriorityUpdate(final long currentPriority, final long requestedPriority) {
        if (currentPriority == requestedPriority) {
            return currentPriority;
        }
        if (0L == requestedPriority) {
            return repo.maxPriority().orElse(0L) + 1;
        }
        return requestedPriority;
    }

    private long resolvePriorityFromStart(final long requestedPosition) {
        // find the rule at index $min + requestedPosition
        if (0 == requestedPosition) {
            Optional<Long> min = repo.minPriority();
            if (min.isPresent()) {
                repo.shift(min.get(), 1);
                return min.get();
            }
            return 1L;
        }

        boolean descending = false;
        Optional<Long> found = repo.findNthPriorityByOrder(requestedPosition, descending);
        if (found.isPresent()) {
            repo.shift(found.get(), 1);
            return found.get();
        }

        // there are not enough, get the max + 1
        return 1 + repo.maxPriority().orElse(0L);
    }

    private long resolvePriorityFromEnd(final long requestedPosition) {
        // find the rule at index $max - requestedPosition
        if (0 == requestedPosition) {
            return repo.maxPriority().map(max -> 1 + max).orElse(1L);
        }

        boolean descending = true;
        Optional<Long> found = repo.findNthPriorityByOrder(requestedPosition, descending);
        if (found.isPresent()) {
            repo.shift(found.get(), 1);
            return found.get();
        }

        // there are not enough rules from the bottom, use the minimum one
        Optional<Long> min = repo.minPriority();
        min.ifPresent(minPriority -> repo.shift(minPriority, 1));
        return min.orElse(1L);
    }
}
