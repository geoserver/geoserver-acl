/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.messaging.bus;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * {@link org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration} is disabled in
 * {@literal application.yml}; this auto configuration enables it when the {@literal
 * geoserver.bus.enabled} configuration property is {@code true}.
 */
@AutoConfiguration(before = AclSpringCloudBusAutoConfiguration.class)
@ConditionalOnProperty(name = "geoserver.bus.enabled", havingValue = "true", matchIfMissing = false)
@Import(org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class)
@Slf4j
public class RabbitAutoConfiguration {

    @PostConstruct
    void log() {
        log.info("Loading RabbitMQ bus bridge");
    }
}
