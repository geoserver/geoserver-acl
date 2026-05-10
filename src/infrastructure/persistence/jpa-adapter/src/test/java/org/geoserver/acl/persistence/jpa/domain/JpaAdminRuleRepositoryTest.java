/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.geoserver.acl.config.persistence.jpa.AclDataSourceConfiguration;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAConfiguration;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAPropertiesTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AclDataSourceConfiguration.class,
            AuthorizationJPAConfiguration.class
        })
@ActiveProfiles("test")
@SuppressWarnings("java:S5786") // must be public, it's extended from other packages
public class JpaAdminRuleRepositoryTest {

    private @Autowired JpaAdminRuleRepository repo;

    private @Autowired EntityManager em;

    private JpaAdminRule entity;

    @BeforeEach
    void beforeEach() {
        entity = new JpaAdminRule();
    }

    @Test
    void testDefaultValues() {
        JpaAdminRule rule = new JpaAdminRule();
        assertNotNull(rule.getIdentifier());
        JpaAdminRuleIdentifier identifier = rule.getIdentifier();
        assertEquals("*", identifier.getRolename());
        assertEquals("*", identifier.getUsername());
        assertEquals("*", identifier.getWorkspace());
        assertEquals(JpaAdminGrantType.USER, rule.getAccess());
        assertEquals(JpaIPAddressRange.noData(), identifier.getAddressRange());

        rule = repo.saveAndFlush(rule);
        JpaAdminRule saved = repo.getReferenceById(rule.getId());
        assertThat(saved).isEqualTo(rule);
    }

    @Test
    void testMandatoryProperties() {
        // non nullable attributes in RuleIdentified can't even be set to null
        JpaAdminRuleIdentifier identifier = entity.getIdentifier();
        assertThrows(NullPointerException.class, () -> identifier.setAddressRange(null));
        assertThrows(NullPointerException.class, () -> identifier.setRolename(null));
        assertThrows(NullPointerException.class, () -> identifier.setUsername(null));
        assertThrows(NullPointerException.class, () -> identifier.setWorkspace(null));
        assertThrows(NullPointerException.class, () -> entity.setAccess(null));
    }

    @Test
    void testSaveDuplicateIdentifier_default_values() {
        testSaveDuplicateIdentifier(entity);
    }

    @Test
    void testSaveDuplicateIdentifier() {
        entity.setAccess(JpaAdminGrantType.ADMIN)
                .getIdentifier()
                .setAddressRange(new JpaIPAddressRange(1000L, 2000L, 32))
                .setRolename("ROLE_USER")
                .setWorkspace("workspace");

        testSaveDuplicateIdentifier(entity);
    }

    private void testSaveDuplicateIdentifier(JpaAdminRule rule) {
        rule = new JpaAdminRule(rule).setId(null);
        JpaAdminRule duplicateKey = new JpaAdminRule(rule).setPriority(rule.getPriority() + 1000);
        assertEquals(rule.getIdentifier(), duplicateKey.getIdentifier());

        repo.saveAndFlush(rule);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicateKey));
    }

    @Test
    void testSave_Identifier() {
        JpaAdminRuleIdentifier expected = new JpaAdminRuleIdentifier(entity.getIdentifier()
                .setAddressRange(new JpaIPAddressRange(1000L, 2000L, 32))
                .setRolename("ROLE_USER")
                .setUsername("user")
                .setWorkspace("workspace"));

        JpaAdminRule saved = repo.saveAndFlush(entity);
        em.detach(saved);

        JpaAdminRule found = repo.getReferenceById(saved.getId());
        assertThat(found.getIdentifier()).isNotSameAs(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void findAll() {
        List<JpaAdminRule> expected = addSamplesInReverseNaturalOrder();
        List<JpaAdminRule> actual = repo.findAll();
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
    }

    @Test
    void findAllNaturalOrderFiltered() {
        final List<JpaAdminRule> all = addSamplesInReverseNaturalOrder();

        QJpaAdminRule qadm = QJpaAdminRule.jpaAdminRule;
        Predicate predicate = qadm.priority.gt(2L).and(qadm.identifier.workspace.eq("*"));

        List<JpaAdminRule> expected = all.stream()
                .filter(r ->
                        r.getPriority() > 2L && "*".equals(r.getIdentifier().getWorkspace()))
                .toList();

        Iterable<JpaAdminRule> res = repo.findAll(predicate, Sort.by("priority"));
        List<JpaAdminRule> actual = new ArrayList<>();
        res.forEach(actual::add);
        assertThat(actual).hasSameSizeAs(expected).isEqualTo(expected);
    }

    /** Adds sample rules in reverse natural order and returns them in natural order */
    private List<JpaAdminRule> addSamplesInReverseNaturalOrder() {
        JpaAdminRule rule = this.entity;
        List<JpaAdminRule> expected = new ArrayList<>();

        rule.setAccess(JpaAdminGrantType.ADMIN).getIdentifier();
        expected.add(new JpaAdminRule(rule));

        rule.getIdentifier().setAddressRange(new JpaIPAddressRange(1000L, 2000L, 32));
        expected.add(new JpaAdminRule(rule));

        rule.getIdentifier().setRolename("rolename");
        expected.add(new JpaAdminRule(rule));

        rule.getIdentifier().setUsername("user");
        expected.add(new JpaAdminRule(rule));

        rule.getIdentifier().setWorkspace("workspace");
        expected.add(new JpaAdminRule(rule));

        IntStream.range(0, expected.size()).forEach(p -> expected.get(p).setPriority(p));

        List<JpaAdminRule> reversed = new ArrayList<>(expected);
        Collections.reverse(reversed);
        for (JpaAdminRule r : reversed) {
            repo.saveAndFlush(r);
        }

        Collections.sort(expected, (r1, r2) -> Long.compare(r1.getPriority(), r2.getPriority()));
        return expected;
    }
}
