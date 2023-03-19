package org.geoserver.acl.integration.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.repository.JpaRuleRepository;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.CatalogMode;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.IPAddressRange;
import org.geoserver.acl.model.rules.InsertPosition;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleLimits;
import org.geoserver.acl.model.rules.SpatialFilterType;
import org.geoserver.acl.rules.RuleIdentifierConflictException;
import org.geoserver.acl.rules.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AuthorizationJPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
class RuleRepositoryJpaAdaptorTest {

    private static final String WORLD =
            "SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired RuleRepository repo;
    private @Autowired JpaRuleRepository jpaRepo;

    @BeforeEach
    void setup() {
        jpaRepo.deleteAll();
    }

    @Test
    void create_fixedPriorityPosition() {
        Rule r1 = Rule.allow().withPriority(1).withInstanceName("default-gs");

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
        testCreateDuplicateIdentifier(r = r.withPriority(1).withInstanceName("default-gs"));
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
        Rule r1 = Rule.allow().withPriority(1).withInstanceName("default-gs");

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

    @Test
    void streamAll() {
        List<Rule> all =
                IntStream.rangeClosed(1, 100).mapToObj(this::addFull).collect(Collectors.toList());
        List<Rule> result = repo.findAll(RuleQuery.of()).collect(Collectors.toList());
        assertThat(result).isEqualTo(all);
    }

    private Rule addFull(int priority) {
        return repo.create(createFull(priority), InsertPosition.FIXED);
    }

    private Rule createFull(int priority) {
        return Rule.limit().toBuilder()
                .priority(priority)
                .name("p" + priority)
                .description("desc " + priority)
                .extId("extId-" + priority)
                .identifier(
                        Rule.limit().getIdentifier().toBuilder()
                                .addressRange(IPAddressRange.fromCidrSignature("10.1.1.1/32"))
                                .layer("layer-" + priority)
                                .workspace("ws-" + priority)
                                .build())
                .ruleLimits(limits())
                .build();
    }

    private RuleLimits limits() {
        return RuleLimits.builder()
                .allowedArea((MultiPolygon<?>) Wkt.fromWkt(WORLD))
                .catalogMode(CatalogMode.CHALLENGE)
                .spatialFilterType(SpatialFilterType.CLIP)
                .build();
    }
}
