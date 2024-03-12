/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.bus;

import org.geoserver.acl.bus.bridge.RemoteAclRuleEventsBridge;
import org.geoserver.acl.bus.bridge.RemoteRuleEvent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.ConditionalOnBusEnabled;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * @since 2.0
 */
@AutoConfiguration
@AutoConfigureAfter(BusAutoConfiguration.class)
@ConditionalOnBusEnabled
@ConditionalOnProperty(name = "geoserver.bus.enabled", havingValue = "true", matchIfMissing = false)
@RemoteApplicationEventScan(basePackageClasses = {RemoteRuleEvent.class})
public class AclSpringCloudBusAutoConfiguration {

    @Bean
    RemoteAclRuleEventsBridge remoteAclRuleEventsBridge(
            ApplicationEventPublisher publisher,
            ServiceMatcher serviceMatcher,
            Destination.Factory destinationFactory) {

        return new RemoteAclRuleEventsBridge(publisher, serviceMatcher, destinationFactory);
    }
}
