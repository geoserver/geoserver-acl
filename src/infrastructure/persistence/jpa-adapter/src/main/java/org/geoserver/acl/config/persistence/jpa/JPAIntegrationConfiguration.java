/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.persistence.jpa;

import jakarta.persistence.EntityManager;
import java.util.function.Consumer;
import org.geoserver.acl.domain.adminrules.AdminRuleEvent;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.persistence.jpa.adaptor.AdminRuleJpaMapper;
import org.geoserver.acl.persistence.jpa.adaptor.AdminRuleJpaMapperImpl;
import org.geoserver.acl.persistence.jpa.adaptor.AdminRuleRepositoryJpaAdaptor;
import org.geoserver.acl.persistence.jpa.adaptor.IPAddressRangeJpaMapperImpl;
import org.geoserver.acl.persistence.jpa.adaptor.RuleJpaMapper;
import org.geoserver.acl.persistence.jpa.adaptor.RuleJpaMapperImpl;
import org.geoserver.acl.persistence.jpa.adaptor.RuleRepositoryJpaAdaptor;
import org.geoserver.acl.persistence.jpa.domain.JpaAdminRuleRepository;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Central JPA integration configuration that assembles the persistence layer. Creates the
 * {@link RuleRepository} and {@link AdminRuleRepository} adaptor beans backed by JPA, wires event
 * publishing for cache invalidation, and instantiates MapStruct mapper implementations.
 *
 * <p>Imports:
 * <ul>
 *   <li>{@link AclDataSourceConfiguration} - datasource (JNDI or JDBC/HikariCP)
 *   <li>{@link AuthorizationJPAConfiguration} - EntityManagerFactory, TransactionManager, JPA repositories
 *   <li>{@link H2PgCompatConfiguration} - H2 shim for {@code pg_advisory_xact_lock}, used by the
 *       dev profile and H2-based integration tests
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@Import({AclDataSourceConfiguration.class, AuthorizationJPAConfiguration.class, H2PgCompatConfiguration.class})
public class JPAIntegrationConfiguration {

    @Bean
    RuleRepository aclRuleRepositoryJpaAdaptor(
            EntityManager em,
            JpaRuleRepository jpaRuleRepository,
            RuleJpaMapper modelMapper,
            ApplicationEventPublisher eventPublisher) {

        RuleRepositoryJpaAdaptor adaptor = new RuleRepositoryJpaAdaptor(em, jpaRuleRepository, modelMapper);
        Consumer<RuleEvent> publisher = eventPublisher::publishEvent;
        adaptor.setEventPublisher(publisher);
        return adaptor;
    }

    @Bean
    AdminRuleRepository aclAdminRuleRepositoryJpaAdaptor(
            EntityManager em,
            JpaAdminRuleRepository jpaAdminRuleRepo,
            AdminRuleJpaMapper modelMapper,
            ApplicationEventPublisher eventPublisher) {

        AdminRuleRepositoryJpaAdaptor adaptor = new AdminRuleRepositoryJpaAdaptor(em, jpaAdminRuleRepo, modelMapper);
        Consumer<AdminRuleEvent> publisher = eventPublisher::publishEvent;
        adaptor.setEventPublisher(publisher);
        return adaptor;
    }

    @Bean
    RuleJpaMapper ruleJpaMapper() {
        return new RuleJpaMapperImpl(new IPAddressRangeJpaMapperImpl());
    }

    @Bean
    AdminRuleJpaMapper adminRuleJpaMapper() {
        return new AdminRuleJpaMapperImpl(new IPAddressRangeJpaMapperImpl());
    }
}
