/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Domain event published when rules are created, updated, or deleted.
 *
 * <p>Published by {@link RuleAdminService} after successful mutations. Supports cache invalidation,
 * remote distribution in clustered deployments, and future audit logging.
 *
 * <p>Example:
 * <pre>{@code
 * // Register event listener
 * ruleAdminService.setEventPublisher(event -> {
 *     switch (event.getEventType()) {
 *         case CREATED, UPDATED, DELETED ->
 *             authorizationCache.invalidateAll();
 *     }
 * });
 *
 * // Events published automatically
 * Rule rule = ruleAdminService.insert(Rule.allow().withUsername("alice"));
 * // RuleEvent.created(rule) is published
 * }</pre>
 *
 * <p>Pure domain object with no framework dependencies. Integration layer adapts to Spring events,
 * message queues, or other event buses.
 *
 * @since 1.0
 * @see RuleAdminService#setEventPublisher(java.util.function.Consumer)
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleEvent {

    /**
     * Type of mutation that occurred.
     */
    public enum EventType {
        /** Rule(s) were created/inserted. */
        CREATED,

        /** Rule(s) were modified (properties or priority changed). */
        UPDATED,

        /** Rule(s) were deleted/removed. */
        DELETED
    }

    /** The type of event (CREATED, UPDATED, or DELETED). */
    private EventType eventType;

    /** IDs of the rules affected by this event. Never empty. */
    private Set<String> ruleIds;

    public static RuleEvent created(@NonNull Rule rule) {
        return new RuleEvent(EventType.CREATED, Set.of(rule.getId()));
    }

    public static RuleEvent updated(@NonNull Rule... rules) {
        return updated(Stream.of(rules).map(Rule::getId).collect(Collectors.toSet()));
    }

    public static RuleEvent updated(@NonNull String... ids) {
        return new RuleEvent(EventType.UPDATED, Set.of(ids));
    }

    public static RuleEvent updated(@NonNull Set<String> ids) {
        return new RuleEvent(EventType.UPDATED, ids);
    }

    public static RuleEvent deleted(@NonNull String... ids) {
        return new RuleEvent(EventType.DELETED, Set.of(ids));
    }

    @Override
    public String toString() {
        return "%s%s".formatted(eventType, ruleIds);
    }
}
