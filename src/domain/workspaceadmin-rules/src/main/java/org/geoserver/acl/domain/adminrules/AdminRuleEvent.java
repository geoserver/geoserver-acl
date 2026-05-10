/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Domain event published when {@link AdminRule}s are created, updated, or deleted.
 *
 * <p>Emitted by {@link AdminRuleAdminService} after a successful mutation. Listeners typically
 * use these events to invalidate authorization caches and to propagate changes across nodes in
 * clustered deployments. The event carries the {@link EventType} and the affected rule ids.
 *
 * @see AdminRuleAdminService#setEventPublisher(java.util.function.Consumer)
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRuleEvent {

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    private EventType eventType;
    private Set<String> ruleIds;

    public static AdminRuleEvent created(@NonNull AdminRule rule) {
        return new AdminRuleEvent(EventType.CREATED, Set.of(rule.id()));
    }

    public static AdminRuleEvent updated(@NonNull AdminRule rule) {
        return new AdminRuleEvent(EventType.UPDATED, Set.of(rule.id()));
    }

    public static AdminRuleEvent updated(@NonNull String... ids) {
        return new AdminRuleEvent(EventType.UPDATED, Set.of(ids));
    }

    public static AdminRuleEvent updated(@NonNull Set<String> ids) {
        return new AdminRuleEvent(EventType.UPDATED, ids);
    }

    public static AdminRuleEvent deleted(@NonNull String... ids) {
        return new AdminRuleEvent(EventType.DELETED, Set.of(ids));
    }
}
