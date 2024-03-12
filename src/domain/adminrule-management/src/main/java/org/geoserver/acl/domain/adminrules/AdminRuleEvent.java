/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;

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
        return new AdminRuleEvent(EventType.CREATED, Set.of(rule.getId()));
    }

    public static AdminRuleEvent updated(@NonNull AdminRule rule) {
        return new AdminRuleEvent(EventType.UPDATED, Set.of(rule.getId()));
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
