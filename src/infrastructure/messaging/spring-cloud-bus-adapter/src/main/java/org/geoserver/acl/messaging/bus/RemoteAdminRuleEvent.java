/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.messaging.bus;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent.EventType;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.core.style.ToStringCreator;

/**
 * @since 2.0
 */
@SuppressWarnings("serial")
@EqualsAndHashCode(callSuper = true)
public class RemoteAdminRuleEvent extends RemoteApplicationEvent {

    @Getter
    private EventType eventType;

    @Getter
    private Set<String> ruleIds;

    protected RemoteAdminRuleEvent() {
        // for serialization libraries like Jackson
    }

    RemoteAdminRuleEvent(Object source, String originService, Destination destination, AdminRuleEvent local) {
        super(source, originService, destination);
        this.eventType = local.getEventType();
        this.ruleIds = local.getRuleIds();
    }

    public static RemoteAdminRuleEvent valueOf(
            Object source, String originService, Destination destination, AdminRuleEvent local) {
        return new RemoteAdminRuleEvent(source, originService, destination, local);
    }

    @NonNull
    public AdminRuleEvent toLocal() {
        return new LocalRemoteAdminRuleEvent(eventType, ruleIds);
    }

    public static boolean isRemote(AdminRuleEvent local) {
        return (local instanceof LocalRemoteAdminRuleEvent);
    }

    private static class LocalRemoteAdminRuleEvent extends AdminRuleEvent {
        public LocalRemoteAdminRuleEvent(EventType type, Set<String> ids) {
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
