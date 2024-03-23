/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.core.types.Predicate;

import org.geoserver.acl.jpa.config.AclDataSourceConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.model.AdminGrantType;
import org.geoserver.acl.jpa.model.AdminRule;
import org.geoserver.acl.jpa.model.AdminRuleIdentifier;
import org.geoserver.acl.jpa.model.IPAddressRange;
import org.geoserver.acl.jpa.model.QAdminRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AclDataSourceConfiguration.class,
            AuthorizationJPAConfiguration.class
        })
@ActiveProfiles("test")
public class JpaAdminRuleRepositoryTest {

    private @Autowired JpaAdminRuleRepository repo;

    private @Autowired EntityManager em;

    private AdminRule entity;

    @BeforeEach
    void beforeEach() {
        entity = new AdminRule();
    }

    @Test
    void testDefaultValues() {
        AdminRule rule = new AdminRule();
        assertNotNull(rule.getIdentifier());
        AdminRuleIdentifier identifier = rule.getIdentifier();
        assertEquals("*", identifier.getRolename());
        assertEquals("*", identifier.getUsername());
        assertEquals("*", identifier.getWorkspace());
        assertEquals(AdminGrantType.USER, rule.getAccess());
        assertEquals(IPAddressRange.noData(), identifier.getAddressRange());

        rule = repo.saveAndFlush(rule);
        AdminRule saved = repo.getReferenceById(rule.getId());
        assertThat(saved).isEqualTo(rule);
    }

    @Test
    void testMandatoryProperties() {
        // non nullable attributes in RuleIdentified can't even be set to null
        AdminRuleIdentifier identifier = entity.getIdentifier();
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
        entity.setAccess(AdminGrantType.ADMIN)
                .getIdentifier()
                .setAddressRange(new IPAddressRange(1000L, 2000L, 32))
                .setRolename("ROLE_USER")
                .setWorkspace("workspace");

        testSaveDuplicateIdentifier(entity);
    }

    private void testSaveDuplicateIdentifier(AdminRule rule) {
        rule = rule.clone().setId(null);
        AdminRule duplicateKey = rule.clone().setPriority(rule.getPriority() + 1000);
        assertEquals(rule.getIdentifier(), duplicateKey.getIdentifier());

        repo.saveAndFlush(rule);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicateKey));
    }

    @Test
    void testSave_Identifier() {
        AdminRuleIdentifier expected =
                entity.getIdentifier()
                        .setAddressRange(new IPAddressRange(1000L, 2000L, 32))
                        .setRolename("ROLE_USER")
                        .setUsername("user")
                        .setWorkspace("workspace")
                        .clone();

        AdminRule saved = repo.saveAndFlush(entity);
        em.detach(saved);

        AdminRule found = repo.getReferenceById(saved.getId());
        assertThat(found.getIdentifier()).isNotSameAs(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void findAll() {
        List<AdminRule> expected = addSamplesInReverseNaturalOrder();
        List<AdminRule> actual = repo.findAll();
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
    }

    @Test
    void findAllNaturalOrderFiltered() {
        final List<AdminRule> all = addSamplesInReverseNaturalOrder();

        QAdminRule qadm = QAdminRule.adminRule;
        Predicate predicate = qadm.priority.gt(2L).and(qadm.identifier.workspace.eq("*"));

        List<AdminRule> expected =
                all.stream()
                        .filter(
                                r ->
                                        r.getPriority() > 2L
                                                && "*".equals(r.getIdentifier().getWorkspace()))
                        .toList();

        Iterable<AdminRule> res = repo.findAll(predicate, Sort.by("priority"));
        List<AdminRule> actual = new ArrayList<>();
        res.forEach(actual::add);
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    /** Adds sample rules in reverse natural order and returns them in natural order */
    private List<AdminRule> addSamplesInReverseNaturalOrder() {
        AdminRule rule = this.entity;
        List<AdminRule> expected = new ArrayList<>();

        rule.setAccess(AdminGrantType.ADMIN).getIdentifier();
        expected.add(rule.clone());

        rule.getIdentifier().setAddressRange(new IPAddressRange(1000L, 2000L, 32));
        expected.add(rule.clone());

        rule.getIdentifier().setRolename("rolename");
        expected.add(rule.clone());

        rule.getIdentifier().setUsername("user");
        expected.add(rule.clone());

        rule.getIdentifier().setWorkspace("workspace");
        expected.add(rule.clone());

        IntStream.range(0, expected.size()).forEach(p -> expected.get(p).setPriority(p));

        List<AdminRule> reversed = new ArrayList<>(expected);
        Collections.reverse(reversed);
        for (AdminRule r : reversed) {
            repo.saveAndFlush(r);
        }

        Collections.sort(expected, (r1, r2) -> Long.compare(r1.getPriority(), r2.getPriority()));
        return expected;
    }
}
