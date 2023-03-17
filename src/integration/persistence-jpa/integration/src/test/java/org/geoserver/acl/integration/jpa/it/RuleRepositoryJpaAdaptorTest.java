package org.geoserver.acl.integration.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.model.GeoServerInstance;
import org.geoserver.acl.jpa.repository.JpaGeoServerInstanceRepository;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.IPAddressRange;
import org.geoserver.acl.model.rules.InsertPosition;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.rules.RuleIdentifierConflictException;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AuthorizationJPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
class RuleRepositoryJpaAdaptorTest {

    private static final String WORLD =
            "MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired JpaGeoServerInstanceRepository jpaInstances;

    private @Autowired RuleRepository repo;
    private @Autowired JpaRuleRepository jpaRepo;

    private String geoserverInstanceName;

    @BeforeEach
    void setup() {
        jpaRepo.deleteAll();
        jpaInstances.deleteAll();

        GeoServerInstance jpaGs =
                new GeoServerInstance()
                        .setName("defaultInstance")
                        .setBaseURL("http://localhost")
                        .setUsername("admin")
                        .setPassword("gs");
        jpaInstances.saveAndFlush(jpaGs);
        this.geoserverInstanceName = jpaGs.getName();
    }

    @Test
    void create_fixedPriorityPosition() {
        Rule r1 = Rule.allow().withPriority(1).withInstanceName(geoserverInstanceName);

        Rule r1Created = repo.create(r1, InsertPosition.FIXED);
        assertThat(repo.count()).isOne();
        assertThat(r1Created).isNotNull();
        assertThat(r1Created.getId()).isNotNull();
        assertThat(r1Created.withId(null)).isEqualTo(r1);
    }

    @Test
    void create_duplicateKey() {
        Rule r = Rule.allow();
        testCreateDuplicateIdentifier(r);
        testCreateDuplicateIdentifier(
                r = r.withPriority(1).withInstanceName(geoserverInstanceName));
        testCreateDuplicateIdentifier(r = r.withPriority(2).withUsername("user"));
        testCreateDuplicateIdentifier(r = r.withPriority(3).withRolename("role"));
        testCreateDuplicateIdentifier(r = r.withPriority(4).withService("WMS"));
        testCreateDuplicateIdentifier(r = r.withPriority(5).withRequest("GetCapabilities"));
        testCreateDuplicateIdentifier(
                r =
                        r.withPriority(6)
                                .withAddressRange(IPAddressRange.fromCidrSignature("10.0.0.1/24")));
        testCreateDuplicateIdentifier(r = r.withPriority(7).withSubfield("subfield"));
        testCreateDuplicateIdentifier(r = r.withPriority(8).withWorkspace("ws"));
        testCreateDuplicateIdentifier(r = r.withPriority(9).withLayer("layer"));
        testCreateDuplicateIdentifier(r = r.withPriority(10).withAccess(GrantType.DENY));
    }

    private void testCreateDuplicateIdentifier(Rule r1) {
        assertNotNull(repo.create(r1, InsertPosition.FIXED));

        assertThrows(
                RuleIdentifierConflictException.class, () -> repo.create(r1, InsertPosition.FIXED));
    }

    @Test
    void count() {
        assertThat(repo.count()).isZero();
        Rule r1 = Rule.allow().withPriority(1).withInstanceName(geoserverInstanceName);

        r1 = repo.create(r1, InsertPosition.FIXED);
        assertThat(repo.count()).isOne();

        Rule r2 = r1.withId(null).withPriority(2).withAccess(GrantType.LIMIT);
        r2 = repo.create(r2, InsertPosition.FIXED);
        assertThat(repo.count()).isEqualTo(2);

        assertThat(repo.findAll().count()).isEqualTo(2);

        repo.findById(r1.getId());
        repo.findById(r2.getId());

        List<Rule> collect = repo.findAll().collect(Collectors.toList());
        assertEquals(2, collect.size());
    }
}
