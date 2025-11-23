/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.messaging.bus;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent.EventType;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.core.style.ToStringCreator;

/**
 * @since 2.0
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = true)
public class RemoteRuleEvent extends RemoteApplicationEvent {

    @Getter
    private EventType eventType;

    @Getter
    private Set<String> ruleIds;

    protected RemoteRuleEvent() {
        // for serialization libraries like Jackson
    }

    RemoteRuleEvent(Object source, String origin, Destination destination, RuleEvent local) {
        super(source, origin, destination);
        this.eventType = local.getEventType();
        this.ruleIds = local.getRuleIds();
    }

    public static RemoteRuleEvent valueOf(
            @NonNull Object source,
            @NonNull String originService,
            @NonNull Destination destination,
            @NonNull RuleEvent local) {

        return new RemoteRuleEvent(source, originService, destination, local);
    }

    @NonNull
    public RuleEvent toLocal() {
        return new LocalRemoteRuleEvent(eventType, ruleIds);
    }

    public static boolean isRemote(RuleEvent event) {
        return (event instanceof LocalRemoteRuleEvent);
    }

    private static class LocalRemoteRuleEvent extends RuleEvent {
        public LocalRemoteRuleEvent(EventType type, Set<String> ids) {
            super(type, ids);
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("eventType", eventType)
                .append("ruleIds", ruleIds)
                .append("id", getId())
                .append("originService", getOriginService())
                .append("destinationService", getDestinationService())
                .toString();
    }
}
