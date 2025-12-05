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
 * RabbitMQ and Spring Cloud Stream autoconfigurations are disabled in {@literal application.yml};
 * this auto configuration enables them when the {@literal geoserver.bus.enabled} configuration
 * property is {@code true}.
 *
 * <p>Imports:
 *
 * <ul>
 *   <li>{@link org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration} - RabbitMQ
 *       connection factory
 *   <li>{@link org.springframework.cloud.stream.config.BindingServiceConfiguration} - Spring Cloud
 *       Stream binding service
 *   <li>{@link org.springframework.cloud.stream.function.FunctionConfiguration} - Spring Cloud
 *       Stream function support
 *   <li>{@link org.springframework.cloud.stream.config.BindersHealthIndicatorAutoConfiguration} -
 *       Spring Cloud Stream health indicators
 * </ul>
 */
@AutoConfiguration(before = AclSpringCloudBusAutoConfiguration.class)
@ConditionalOnProperty(name = "geoserver.bus.enabled", havingValue = "true", matchIfMissing = false)
@Import({
    org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration.class,
    org.springframework.cloud.stream.config.BindingServiceConfiguration.class,
    org.springframework.cloud.stream.function.FunctionConfiguration.class,
    org.springframework.cloud.stream.config.BindersHealthIndicatorAutoConfiguration.class
})
@Slf4j
public class RabbitAutoConfiguration {

    @PostConstruct
    void log() {
        log.info("Loading RabbitMQ bus bridge");
    }
}
