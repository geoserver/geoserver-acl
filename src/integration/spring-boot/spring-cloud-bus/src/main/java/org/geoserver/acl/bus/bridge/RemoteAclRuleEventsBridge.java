/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.bus.bridge;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

/**
 * @since 2.0
 */
@RequiredArgsConstructor
@Slf4j(topic = "org.geoserver.acl.bus.bridge")
public class RemoteAclRuleEventsBridge {
    /** Constant indicating a remote event is destined to all services */
    private static final String DESTINATION_ALL_SERVICES = "**";

    private final @NonNull ApplicationEventPublisher publisher;
    private final @NonNull ServiceMatcher serviceMatcher;
    private final @NonNull Destination.Factory destinationFactory;

    private Destination destinationService() {
        return destinationFactory.getDestination(DESTINATION_ALL_SERVICES);
    }

    @EventListener(RuleEvent.class)
    public void publishRemoteRuleEvent(RuleEvent event) {
        if (isLocal(event)) {
            String busId = serviceMatcher.getBusId();
            Destination destination = destinationService();
            var remote = RemoteRuleEvent.valueOf(this, busId, destination, event);
            log.debug("RuleEvent produced on this instance, publishing {}", remote);
            publisher.publishEvent(remote);
        }
    }

    @EventListener(RemoteRuleEvent.class)
    public void publishLocalRuleEvent(RemoteRuleEvent remote) {
        if (!serviceMatcher.isFromSelf(remote)) {
            RuleEvent local = remote.toLocal();
            log.debug("Publishing RuleEvent from incoming {}", remote);
            publisher.publishEvent(local);
        }
    }

    @EventListener(AdminRuleEvent.class)
    public void publishRemoteAdminRuleEvent(AdminRuleEvent event) {
        if (isLocal(event)) {
            String busId = serviceMatcher.getBusId();
            Destination destination = destinationService();
            var remote = RemoteAdminRuleEvent.valueOf(this, busId, destination, event);
            log.debug("AdminRuleEvent produced on this instance, publishing {}", remote);
            publisher.publishEvent(remote);
        }
    }

    @EventListener(RemoteAdminRuleEvent.class)
    public void publishLocalAdminRuleEvent(RemoteAdminRuleEvent remote) {
        if (!serviceMatcher.isFromSelf(remote)) {
            AdminRuleEvent local = remote.toLocal();
            log.debug("Publishing AdminRuleEvent from incoming {}", remote);
            publisher.publishEvent(local);
        }
    }

    private boolean isLocal(AdminRuleEvent local) {
        return !RemoteAdminRuleEvent.isRemote(local);
    }

    private boolean isLocal(RuleEvent local) {
        return !RemoteRuleEvent.isRemote(local);
    }
}
