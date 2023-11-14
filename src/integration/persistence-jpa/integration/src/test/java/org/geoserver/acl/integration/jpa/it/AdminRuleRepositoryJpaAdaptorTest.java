package org.geoserver.acl.integration.jpa.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifierConflictException;
import org.geoserver.acl.domain.adminrules.AdminRuleRepository;
import org.geoserver.acl.domain.adminrules.InsertPosition;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.geoserver.acl.jpa.repository.JpaAdminRuleRepository;
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
            JPAIntegrationConfiguration.class
        })
@ActiveProfiles("test") // see config props in src/test/resource/application-test.yaml
class AdminRuleRepositoryJpaAdaptorTest {

    private static final String WORLD =
            "SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired AdminRuleRepository repo;
    private @Autowired JpaAdminRuleRepository jpaRepo;

    @BeforeEach
    void setup() {
        jpaRepo.deleteAll();
    }

    @Test
    void create_fixedPriorityPosition() {
        AdminRule r1 = AdminRule.user().withPriority(1);

        AdminRule r1Created = repo.create(r1, InsertPosition.FIXED);
        assertThat(repo.count()).isOne();
        assertThat(r1Created).isNotNull();
        assertThat(r1Created.getId()).isNotNull();
        assertThat(r1Created.withId(null)).isEqualTo(r1);
    }

    @Test
    void create_duplicateKey() {
        AdminRule r = AdminRule.user();
        testCreateDuplicateIdentifier(r);
        testCreateDuplicateIdentifier(r = r.withPriority(2).withUsername("user"));
        testCreateDuplicateIdentifier(r = r.withPriority(3).withRolename("role"));
        testCreateDuplicateIdentifier(r = r.withPriority(6).withAddressRange("10.0.0.1/24"));
        testCreateDuplicateIdentifier(r = r.withPriority(8).withWorkspace("ws"));
    }

    private void testCreateDuplicateIdentifier(AdminRule r1) {
        assertNotNull(repo.create(r1, InsertPosition.FIXED));

        assertThrows(
                AdminRuleIdentifierConflictException.class,
                () -> repo.create(r1, InsertPosition.FIXED));
    }

    @Test
    void update_duplicateKey() {
        AdminRule r1 = AdminRule.admin().withPriority(1).withRolename("role").withUsername("user1");
        r1 = repo.create(r1, InsertPosition.FIXED);

        AdminRule r2 =
                repo.create(
                        r1.withId(null).withPriority(2).withUsername("user2"),
                        InsertPosition.FIXED);

        AdminRule r1dup = r2.withUsername("user1");
        String message =
                assertThrows(AdminRuleIdentifierConflictException.class, () -> repo.save(r1dup))
                        .getMessage();
        assertThat(message).contains(r1.toShortString());

        AdminRule r2dup = r1.withUsername("user2");
        message =
                assertThrows(AdminRuleIdentifierConflictException.class, () -> repo.save(r2dup))
                        .getMessage();
        assertThat(message).contains(r2.toShortString());
    }

    @Test
    void count() {
        assertThat(repo.count()).isZero();
        AdminRule r1 = AdminRule.admin().withPriority(1);

        r1 = repo.create(r1, InsertPosition.FIXED);
        assertThat(repo.count()).isOne();

        AdminRule r2 = r1.withId(null).withPriority(2).withWorkspace("w1");
        r2 = repo.create(r2, InsertPosition.FIXED);
        assertThat(repo.count()).isEqualTo(2);

        assertThat(repo.findAll().count()).isEqualTo(2);

        repo.findById(r1.getId());
        repo.findById(r2.getId());

        List<AdminRule> collect = repo.findAll().collect(Collectors.toList());
        assertEquals(2, collect.size());
    }

    @Test
    void streamAll() {
        List<AdminRule> all =
                IntStream.rangeClosed(1, 100).mapToObj(this::addFull).collect(Collectors.toList());
        List<AdminRule> result = repo.findAll(RuleQuery.of()).collect(Collectors.toList());
        assertThat(result).isEqualTo(all);
    }

    @Test
    void deleteById() {
        AdminRule r1 = repo.create(AdminRule.user().withWorkspace("w1"), InsertPosition.FIXED);
        AdminRule r2 = repo.create(AdminRule.admin().withWorkspace("w2"), InsertPosition.FIXED);

        assertThat(repo.count()).isEqualTo(2);

        assertThat(repo.deleteById(r2.getId())).isTrue();
        assertThat(repo.count()).isOne();
        assertThat(repo.deleteById(r2.getId())).isFalse();
        assertThat(repo.count()).isOne();

        assertThat(repo.deleteById(r1.getId())).isTrue();
        assertThat(repo.count()).isZero();
        assertThat(repo.deleteById(r1.getId())).isFalse();
        assertThat(repo.count()).isZero();
    }

    private AdminRule addFull(int priority) {
        return repo.create(createFull(priority), InsertPosition.FIXED);
    }

    private AdminRule createFull(int priority) {
        return AdminRule.user().toBuilder()
                .priority(priority)
                .name("p" + priority)
                .description("desc " + priority)
                .extId("extId-" + priority)
                .identifier(
                        AdminRuleIdentifier.builder()
                                .addressRange("10.1.1.1/32")
                                .workspace("ws-" + priority)
                                .rolename("ROLE_1")
                                .username("user-" + priority)
                                .build())
                .build();
    }
}
