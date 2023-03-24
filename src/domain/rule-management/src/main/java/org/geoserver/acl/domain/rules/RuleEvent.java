/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import lombok.NonNull;
import lombok.Value;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class RuleEvent {

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    private EventType eventType;
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
}
