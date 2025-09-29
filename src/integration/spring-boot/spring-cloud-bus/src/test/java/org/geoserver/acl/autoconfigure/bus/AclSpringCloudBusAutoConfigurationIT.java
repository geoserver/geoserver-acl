/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.geoserver.acl.bus.bridge.RemoteAdminRuleEvent;
import org.geoserver.acl.bus.bridge.RemoteRuleEvent;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @see {@literal src/test/resources/application-it.yml}
 */
@Testcontainers
@Slf4j
class AclSpringCloudBusAutoConfigurationIT {

    @Container
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management");

    @Configuration
    @EnableAutoConfiguration
    static class EventCapture {

        List<RuleEvent> ruleEvents = new ArrayList<>();
        List<RemoteRuleEvent> remoteRuleEvents = new ArrayList<>();

        List<AdminRuleEvent> adminRuleEvents = new ArrayList<>();
        List<RemoteAdminRuleEvent> remoteAdminRuleEvents = new ArrayList<>();

        void clear() {
            ruleEvents.clear();
            remoteRuleEvents.clear();
            adminRuleEvents.clear();
            remoteAdminRuleEvents.clear();
        }

        @EventListener(RuleEvent.class)
        void capture(RuleEvent event) {
            ruleEvents.add(event);
        }

        @EventListener(RemoteRuleEvent.class)
        void capture(RemoteRuleEvent event) {
            log.info("captured {}", event);
            remoteRuleEvents.add(event);
        }

        @EventListener(AdminRuleEvent.class)
        void capture(AdminRuleEvent event) {
            adminRuleEvents.add(event);
        }

        @EventListener(RemoteAdminRuleEvent.class)
        void capture(RemoteAdminRuleEvent event) {
            remoteAdminRuleEvents.add(event);
        }
    }

    private ConfigurableApplicationContext app1Context;
    private ConfigurableApplicationContext app2Context;
    private EventCapture app1CapturedEvents;
    private EventCapture app2CapturedEvents;

    @BeforeEach
    void beforeEeach() {
        app1Context = newApplicationContext("app:1");
        app1CapturedEvents = app1Context.getBean(EventCapture.class);

        app2Context = newApplicationContext("app:2");
        app2CapturedEvents = app2Context.getBean(EventCapture.class);
    }

    @AfterEach
    void afterEach() {
        if (app1Context != null) {
            app1Context.close();
        }
        if (app2Context != null) {
            app2Context.close();
        }
    }

    @Test
    void testRuleEvent() throws InterruptedException {
        RuleEvent ruleEvent = RuleEvent.deleted("r1", "r2", "r3");
        // publish on app1
        app1Context.publishEvent(ruleEvent);
        // capture on app2
        List<RemoteRuleEvent> app2Captured = app2CapturedEvents.remoteRuleEvents;
        List<RuleEvent> app2Local = app2CapturedEvents.ruleEvents;

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(app2Captured)
                .singleElement());
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(app2Local)
                .isNotEmpty());

        RemoteRuleEvent capturedConverted = app2Captured.get(0);
        assertThat(capturedConverted.toLocal()).isEqualTo(ruleEvent);

        // RemoteAclRuleEventsBridge shall have re-published the remote event as a local event
        RuleEvent publishedAsLocal = app2Local.get(0);
        assertThat(publishedAsLocal).isEqualTo(ruleEvent);
    }

    @Test
    void testAdminRuleEvent() throws InterruptedException {
        AdminRuleEvent adminEvent = AdminRuleEvent.deleted("a1", "a2", "a3");
        // publish on app1
        app1Context.publishEvent(adminEvent);
        // capture on app2
        List<RemoteAdminRuleEvent> app2Captured = app2CapturedEvents.remoteAdminRuleEvents;
        List<AdminRuleEvent> app2Local = app2CapturedEvents.adminRuleEvents;

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(app2Captured)
                .singleElement());
        Awaitility.await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(app2Local)
                .isNotEmpty());

        RemoteAdminRuleEvent capturedConverted = app2Captured.get(0);
        assertThat(capturedConverted.toLocal()).isEqualTo(adminEvent);

        // RemoteAclRuleEventsBridge shall have re-published the remote event as a local event
        AdminRuleEvent publishedAsLocal = app2Local.get(0);
        assertThat(publishedAsLocal).isEqualTo(adminEvent);
    }

    private static ConfigurableApplicationContext newApplicationContext(String appName) {
        String host = rabbitMQContainer.getHost();
        Integer amqpPort = rabbitMQContainer.getAmqpPort();
        log.info("#".repeat(100));
        log.info("Initializing application context {}, rabbit host: {}, port: {}", appName, host, amqpPort);
        SpringApplicationBuilder remoteAppBuilder = new SpringApplicationBuilder(EventCapture.class)
                .profiles("it") // also load config from application-it.yml
                .properties(
                        "spring.rabbitmq.host=" + host,
                        "spring.rabbitmq.port=" + amqpPort,
                        "spring.cloud.bus.id=" + appName);
        return remoteAppBuilder.run();
    }
}
