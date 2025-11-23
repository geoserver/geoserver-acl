/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.RuleEvent.created;
import static org.geoserver.acl.domain.rules.RuleEvent.updated;

import java.util.List;
import org.geoserver.acl.config.domain.DomainServicesConfiguration;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.config.persistence.jpa.JPAIntegrationConfiguration;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceIT;
import org.geoserver.acl.domain.rules.RuleEvent;
import org.geoserver.acl.persistence.jpa.domain.JpaRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = {
            DomainServicesConfiguration.class,
            AuthorizationJPAPropertiesTestConfiguration.class,
            JPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
@DirtiesContext
public class RuleAdminServiceJpaIT extends RuleAdminServiceIT {

    private @Autowired JpaRuleRepository jpaRepo;
    private @Autowired RuleEventCollector eventCollector;
    private @Autowired RuleAdminService service;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        jpaRepo.deleteAll();
        eventCollector.getRuleEvents().clear();
        eventCollector.getAdminRuleEvents().clear();
        super.setUp();
    }

    @Override
    protected RuleAdminService getRuleAdminService() {
        return service;
    }

    @Test
    void testEvents_create_no_collateral() {
        RuleAdminService service = super.ruleAdminService;
        List<RuleEvent> captured = eventCollector.getRuleEvents();

        Rule allow = service.insert(Rule.allow());
        assertThat(captured.size()).isEqualTo(1);
        assertThat(captured).contains(created(allow));

        captured.clear();
        Rule limit1 = service.insert(Rule.limit().withLayer("l1"));
        Rule limit2 = service.insert(Rule.limit().withLayer("l2"));
        Rule limit3 = service.insert(Rule.limit().withLayer("l3"));
        assertThat(captured.size()).isEqualTo(3);

        assertThat(captured).contains(created(limit1));
        assertThat(captured).contains(created(limit2));
        assertThat(captured).contains(created(limit3));
    }

    @Test
    void testEvents_collateral_due_to_insert() {
        RuleAdminService service = super.ruleAdminService;
        List<RuleEvent> captured = eventCollector.getRuleEvents();

        Rule allow = service.insert(Rule.allow());
        assertThat(captured).isEqualTo(List.of(created(allow)));

        captured.clear();
        Rule limit1 = service.insert(Rule.limit().withLayer("l1").withPriority(1));
        List<RuleEvent> expected = List.of(updated(allow), created(limit1));
        assertThat(captured).isEqualTo(expected);

        captured.clear();
        Rule limit2 = service.insert(Rule.limit().withLayer("l2").withPriority(1));
        expected = List.of(updated(allow, limit1), created(limit2));
        assertThat(captured).isEqualTo(expected);

        captured.clear();
        Rule limit3 = service.insert(Rule.limit().withLayer("l3").withPriority(2));
        expected = List.of(updated(allow, limit1), created(limit3));
        assertThat(captured).isEqualTo(expected);
    }

    @Test
    void testEvents_collateral_due_to_shift() {
        RuleAdminService service = super.ruleAdminService;
        List<RuleEvent> captured = eventCollector.getRuleEvents();

        Rule allow = service.insert(Rule.allow().withPriority(1));
        Rule limit1 = service.insert(Rule.limit().withLayer("l1").withPriority(2));
        Rule limit2 = service.insert(Rule.limit().withLayer("l2").withPriority(3));
        Rule limit3 = service.insert(Rule.limit().withLayer("l3").withPriority(4));
        Rule deny = service.insert(Rule.deny().withPriority(5));

        captured.clear();
        service.shift(1, 10);
        List<RuleEvent> expected = List.of(updated(allow, limit1, limit2, limit3, deny));
        assertThat(captured).isEqualTo(expected);

        captured.clear();
        service.shift(13, 1);
        expected = List.of(updated(limit2, limit3, deny));
        assertThat(captured).isEqualTo(expected);

        captured.clear();
        service.shift(100, 1);
        assertThat(captured).isEmpty();
    }
}
