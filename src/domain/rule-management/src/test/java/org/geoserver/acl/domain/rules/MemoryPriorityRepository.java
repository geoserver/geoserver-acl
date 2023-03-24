/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MemoryPriorityRepository<R> {

    private final Comparator<R> comparator =
            (r1, r2) -> {
                long p1 = getPriority(r1);
                long p2 = getPriority(r2);
                return Long.compare(p1, p2);
            };

    protected SortedSet<R> rules = new TreeSet<>(comparator);

    public abstract long getPriority(R rule);

    public Optional<Long> maxPriority() {
        return rules.stream().reduce((first, second) -> second).map(this::getPriority);
    }

    public Optional<Long> minPriority() {
        return rules.stream().findFirst().map(this::getPriority);
    }

    /**
     * @param position 1-based position
     * @param descending search in reverse priority order
     * @return the priority of the rule at the Nth position in the requested sort order, or empty if
     *     there's no rule at that position
     */
    public Optional<Long> findNthPriorityByOrder(long position, boolean descending) {
        if (position < 1) throw new IllegalArgumentException("find nth position is 1-based");

        final int index = (int) position - 1;
        Stream<R> stream;
        if (descending) {
            ArrayList<R> list = new ArrayList<>(rules);
            Collections.reverse(list);
            stream = list.stream();
        } else {
            stream = rules.stream();
        }
        return stream.skip(index).findFirst().map(this::getPriority);
    }

    public int shift(long priorityStart, long offset) {
        Optional<Long> max = maxPriority();
        if (max.isEmpty()) return -1;
        long stopIncl = max.get().longValue();
        return shiftPrioritiesBetween(priorityStart, stopIncl, offset);
    }

    public abstract Optional<R> findOneByPriority(long priority);

    protected int shiftPrioritiesBetween(long min, long max, long offset) {
        List<R> matches =
                rules.stream()
                        .filter(
                                r -> {
                                    long p = getPriority(r);
                                    return p >= min && p <= max;
                                })
                        .collect(Collectors.toList());

        rules.removeAll(matches);
        matches.forEach(
                r -> {
                    final long priority = getPriority(r) + offset;
                    R offseted = withPriority(r, priority);
                    boolean add = rules.add(offseted);
                    if (!add) {
                        throw new IllegalStateException(
                                "Rule with priority " + priority + " already exists");
                    }
                });
        return matches.size() == 0 ? -1 : matches.size();
    }

    protected abstract R withPriority(R r, long l);

    protected abstract String getId(R rule);

    protected void replace(R rOld, R rNew) {
        if (!getId(rOld).equals(getId(rNew))) {
            throw new IllegalStateException("replace should get the same rule id");
        }
        if (!rules.remove(rOld)) {
            throw new IllegalStateException("Tried to remove non existing old rule " + rOld);
        }
        if (!rules.add(rNew)) {
            throw new IllegalStateException(
                    "Can't replace rule, another one with the same priority already exists: "
                            + rNew);
        }
    }
}
