/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.bus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.geoserver.acl.bus.bridge.RemoteAclRuleEventsBridge;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.BusBridge;
import org.springframework.cloud.bus.PathServiceMatcherAutoConfiguration;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.PathDestinationFactory;

class AclSpringCloudBusAutoConfigurationTest {

    private Destination.Factory destinationFactory = new PathDestinationFactory();
    private BusBridge mockBusBridge = mock(BusBridge.class);

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(Destination.Factory.class, () -> destinationFactory)
            .withBean(BusBridge.class, () -> mockBusBridge)
            .withConfiguration(AutoConfigurations.of(
                    BusAutoConfiguration.class,
                    PathServiceMatcherAutoConfiguration.class,
                    AclSpringCloudBusAutoConfiguration.class));

    @Test
    void testDisabledByDefault() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(RemoteAclRuleEventsBridge.class);
        });
    }

    @Test
    void testEnabled() {
        runner.withPropertyValues("geoserver.bus.enabled=true").run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(RemoteAclRuleEventsBridge.class);
        });
    }
}
