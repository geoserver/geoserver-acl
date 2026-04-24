/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.messaging.bus.RemoteRuleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Verify the application works with spring cloud bus enabled through {@code geoserver.bus.enabled=true} and incoming remote events are processed
 */
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"geoserver.bus.enabled=true"},
        classes = {AccesControlListApplication.class, AccesControlListApplicationBusTest.Capture.class})
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("dev")
@Slf4j
class AccesControlListApplicationBusTest {

    @Container
    private static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(
                    DockerImageName.parse("rabbitmq:4-management-alpine"))
            // Permit the deprecated `queue_master_locator` feature so RabbitMQ 4.x
            // accepts the `x-queue-master-locator` argument that spring-amqp 4.0.0
            // AnonymousQueue adds to Spring Cloud Bus consumer queues.
            // TODO: remove once spring-amqp/spring-cloud-bus stops emitting the argument.
            .withRabbitMQConfig(MountableFile.forClasspathResource("rabbitmq.conf"));

    @Autowired
    WebApplicationContext appContext;

    @Autowired
    ServiceMatcher serviceMatcher;

    @Autowired
    Destination.Factory destinationFactory;

    @Autowired
    Capture capture;

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
    }

    @BeforeEach
    void beforeEeach() {
        capture.clear();
    }

    @Configuration
    static class Capture {

        List<RemoteRuleEvent> remoteEvents = new ArrayList<>();
        List<RuleEvent> localEvents = new ArrayList<>();

        @EventListener(RemoteRuleEvent.class)
        void captureRemoteRuleEvent(RemoteRuleEvent event) {
            System.err.println("--- " + event);
            remoteEvents.add(event);
        }

        @EventListener(RuleEvent.class)
        void captureRuleEvent(RuleEvent event) {
            System.err.println("--- " + event);
            localEvents.add(event);
        }

        public void clear() {
            localEvents.clear();
            remoteEvents.clear();
        }
    }

    @Test
    void incomingEvent() {
        RuleEvent remoteEventPayload = RuleEvent.deleted("r1", "r2", "r3");

        // simulate an incoming remote event
        String originBusId = "acl-service:test:0";
        Destination destination = destinationFactory.getDestination("**");
        RemoteRuleEvent remote = RemoteRuleEvent.valueOf(this, originBusId, destination, remoteEventPayload);
        appContext.publishEvent(remote);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(capture.remoteEvents).singleElement());

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(capture.localEvents).singleElement());

        RuleEvent ruleEvent = capture.localEvents.get(0);
        assertThat(ruleEvent).isNotSameAs(remoteEventPayload).isEqualTo(remoteEventPayload);
    }

    @Test
    void outgoingEvent() {
        RuleEvent localEvent = RuleEvent.deleted("r1", "r2", "r3");
        appContext.publishEvent(localEvent);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(capture.localEvents).singleElement());

        RuleEvent ruleEvent = capture.localEvents.get(0);
        assertThat(ruleEvent).isSameAs(localEvent);

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(capture.remoteEvents).singleElement());

        RemoteRuleEvent remoteEvent = capture.remoteEvents.get(0);
        assertThat(serviceMatcher.isFromSelf(remoteEvent)).isTrue();
    }
}
