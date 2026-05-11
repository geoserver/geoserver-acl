/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geoserver.acl.config.persistence.jpa.AclDataSourceConfiguration;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAConfiguration;
import org.geoserver.acl.config.persistence.jpa.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.persistence.jpa.domain.JpaLayerAttribute.AccessType;
import org.geoserver.acl.persistence.jpa.domain.JpaLayerDetails.LayerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@SuppressWarnings("java:S5786")
public class JpaRuleRepositoryTest {

    private static final String WORLD = "MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired JpaRuleRepository repo;

    private @Autowired EntityManager em;

    private JpaRule entity;

    @BeforeEach
    void beforeEach() {
        entity = new JpaRule();
    }

    @Test
    void testIdentifierDefaultValues() {
        JpaRule rule = new JpaRule();
        assertNotNull(rule.getIdentifier());
        JpaRuleIdentifier identifier = rule.getIdentifier();
        assertEquals("*", identifier.getLayer());
        assertEquals("*", identifier.getRequest());
        assertEquals("*", identifier.getRolename());
        assertEquals("*", identifier.getService());
        assertEquals("*", identifier.getSubfield());
        assertEquals("*", identifier.getUsername());
        assertEquals("*", identifier.getWorkspace());
        assertEquals(JpaGrantType.DENY, identifier.getAccess());
        assertEquals(JpaIPAddressRange.noData(), identifier.getAddressRange());
    }

    @Test
    void testIdentifierDoesNotAllowNull() {
        // non nullable attributes in RuleIdentified can't even be set to null
        JpaRuleIdentifier identifier = entity.getIdentifier();
        assertThrows(NullPointerException.class, () -> identifier.setAccess(null));
        assertThrows(NullPointerException.class, () -> identifier.setAddressRange(null));
        assertThrows(NullPointerException.class, () -> identifier.setLayer(null));
        assertThrows(NullPointerException.class, () -> identifier.setRequest(null));
        assertThrows(NullPointerException.class, () -> identifier.setRolename(null));
        assertThrows(NullPointerException.class, () -> identifier.setService(null));
        assertThrows(NullPointerException.class, () -> identifier.setSubfield(null));
        assertThrows(NullPointerException.class, () -> identifier.setUsername(null));
        assertThrows(NullPointerException.class, () -> identifier.setWorkspace(null));
    }

    @Test
    void testSave_Identifier_defaultValues() {

        JpaRuleIdentifier expected = new JpaRuleIdentifier(entity.getIdentifier());

        JpaRule saved = repo.saveAndFlush(entity);
        assertSame(entity, saved);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void testSave_Identifier() {
        JpaRuleIdentifier expected = new JpaRuleIdentifier(entity.getIdentifier()
                .setAccess(JpaGrantType.DENY)
                .setAddressRange(new JpaIPAddressRange(1000L, 2000L, 32))
                .setLayer("layer")
                .setRequest("GetCapabilities")
                .setRolename("ROLE_USER")
                .setService("WCS")
                .setSubfield("subfield")
                .setUsername("user")
                .setWorkspace("workspace"));

        JpaRule saved = repo.saveAndFlush(entity);
        em.detach(saved);

        JpaRule found = repo.getReferenceById(saved.getId());
        assertThat(found.getIdentifier()).isNotSameAs(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void testRuleLimits() {
        JpaRule rule = repo.saveAndFlush(entity);
        assertNull(rule.getRuleLimits());

        rule.setRuleLimits(new JpaRuleLimits()
                .setAllowedArea(geom(WORLD))
                .setCatalogMode(JpaCatalogMode.MIXED)
                .setSpatialFilterType(JpaSpatialFilterType.CLIP));

        JpaRuleLimits expected = new JpaRuleLimits(rule.getRuleLimits());

        repo.saveAndFlush(rule);
        rule = repo.findById(rule.getId()).orElseThrow();

        assertNotNull(rule.getRuleLimits());
        assertThat(rule.getRuleLimits()).isEqualTo(expected);
    }

    @Test
    void testLayerDetails() {

        final MultiPolygon<?> area = geom(WORLD);

        Set<JpaLayerAttribute> attributes = Set.of(latt("att1"), latt("att2"));
        final JpaLayerDetails details = new JpaLayerDetails(new JpaLayerDetails()
                .setAllowedStyles(Set.of("s1", "s2"))
                .setArea(area)
                .setAttributes(attributes)
                .setCatalogMode(JpaCatalogMode.CHALLENGE)
                .setCqlFilterRead("a=b")
                .setCqlFilterWrite("foo=bar")
                .setDefaultStyle("defstyle")
                .setSpatialFilterType(JpaSpatialFilterType.CLIP)
                .setType(LayerType.LAYERGROUP));

        JpaLayerDetails expected = new JpaLayerDetails(details);

        entity.setLayerDetails(details);
        entity = repo.saveAndFlush(entity);
        em.detach(entity);

        JpaRule saved = repo.findById(entity.getId()).orElseThrow();

        assertThat(saved.getLayerDetails()).isEqualTo(expected);
    }

    @Test
    void testLayerDetails_unset_allowedStyles() {
        entity.setLayerDetails(
                new JpaLayerDetails().setAllowedStyles(Set.of("s1", "s2")).setCatalogMode(JpaCatalogMode.CHALLENGE));
        entity = repo.saveAndFlush(entity);
        final long ruleId = entity.getId();
        em.detach(entity);

        JpaRule rule = repo.findById(ruleId).orElseThrow();
        assertNotSame(entity, rule);

        rule.getLayerDetails().setAllowedStyles(null);
        repo.saveAndFlush(rule);
        em.detach(rule);

        rule = repo.findById(ruleId).orElseThrow();
        assertThat(rule.getLayerDetails().getAllowedStyles()).isEmpty();
    }

    @Test
    void testLayerDetails_update_allowedStyles() {
        entity.setLayerDetails(
                new JpaLayerDetails().setAllowedStyles(Set.of("s1", "s2")).setCatalogMode(JpaCatalogMode.CHALLENGE));
        entity = repo.saveAndFlush(entity);
        final long ruleId = entity.getId();
        em.detach(entity);

        JpaRule rule = repo.findById(ruleId).orElseThrow();

        assertNotSame(entity, rule);

        Set<String> newStyles = Set.of("newstyle1", "s1", "newstyle2");

        rule.getLayerDetails().getAllowedStyles().clear();
        rule.getLayerDetails().getAllowedStyles().addAll(newStyles);
        repo.saveAndFlush(rule);
        em.detach(rule);

        rule = repo.findById(ruleId).orElseThrow();

        assertThat(rule.getLayerDetails().getAllowedStyles()).isEqualTo(Set.of("newstyle1", "s1", "newstyle2"));
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder()} */
    @Test
    void findAll() {
        List<JpaRule> expected = addSamplesInReverseNaturalOrder();
        List<JpaRule> actual = repo.findAll();
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder(Predicate)} */
    @Test
    void findAllNaturalOrderFiltered() {
        final List<JpaRule> all = addSamplesInReverseNaturalOrder();

        QJpaRule qRule = QJpaRule.jpaRule;
        Predicate predicate = qRule.priority.gt(2L).and(qRule.identifier.layer.eq("*"));

        List<JpaRule> expected = all.stream()
                .filter(r ->
                        r.getPriority() > 2L && "*".equals(r.getIdentifier().getLayer()))
                .toList();

        Iterable<JpaRule> res = repo.findAll(predicate, Sort.by("priority"));
        List<JpaRule> actual = new ArrayList<>();
        res.forEach(actual::add);
        assertThat(actual).hasSameSizeAs(expected).isEqualTo(expected);
    }

    @Test
    void testShiftPriority() {
        List<JpaRule> initial = addSamplesInReverseNaturalOrder();
        assertThat(initial.get(0).getPriority()).isEqualTo(1);

        int affectedCount = repo.shiftPriority(1, 10);
        assertThat(affectedCount).isEqualTo(initial.size());
        initial.forEach(prev -> {
            JpaRule curr = repo.getReferenceById(prev.getId());
            long actual = curr.getPriority();
            long expected = 10 + prev.getPriority();
            assertThat(actual).isEqualTo(expected);
        });
    }

    @Test
    void testStreamIdsByShiftPriority() {
        List<JpaRule> initial = addSamplesInReverseNaturalOrder();
        // preflight
        assertThat(initial.get(0).getPriority()).isEqualTo(1);
        assertThat(initial).hasSize(11);

        Set<Long> expected = initial.stream().map(JpaRule::getId).collect(Collectors.toSet());
        Set<Long> actual = repo.streamIdsByShiftPriority(1).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected = initial.stream()
                .filter(r -> r.getPriority() >= 5)
                .map(JpaRule::getId)
                .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriority(5).collect(Collectors.toSet());

        assertThat(actual).isEqualTo(expected);

        expected = initial.stream()
                .filter(r -> r.getPriority() >= 10)
                .map(JpaRule::getId)
                .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriority(10).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected = Set.of();
        actual = repo.streamIdsByShiftPriority(12).collect(Collectors.toSet());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testShiftPriorityBetween() {
        List<JpaRule> initial = addSamplesInReverseNaturalOrder();
        assertThat(initial.get(0).getPriority()).isEqualTo(1);

        // shift priorities 5 to 11 by 10
        repo.shiftPrioritiesBetween(5, 11, 10);
        initial.forEach(prev -> {
            JpaRule curr = repo.getReferenceById(prev.getId());
            long actual = curr.getPriority();
            long expected = prev.getPriority() < 5 ? prev.getPriority() : 10 + prev.getPriority();
            assertThat(actual).isEqualTo(expected);
        });

        // shift priorities 1 to 4 by 3
        repo.shiftPrioritiesBetween(1, 4, 3);
        initial.forEach(prev -> {
            JpaRule curr = repo.getReferenceById(prev.getId());
            long actual = curr.getPriority();
            long expected;
            if (prev.getPriority() < 5) {
                expected = prev.getPriority() + 3;
            } else {
                expected = prev.getPriority() + 10;
            }
            assertThat(actual).isEqualTo(expected);
        });
    }

    @Test
    void testStreamIdsByShiftPriorityBetween() {
        List<JpaRule> initial = addSamplesInReverseNaturalOrder();
        // preflight
        assertThat(initial.get(0).getPriority()).isEqualTo(1);
        assertThat(initial).hasSize(11);

        Set<Long> expected = initial.stream()
                .filter(r -> r.getPriority() >= 5)
                .map(JpaRule::getId)
                .collect(Collectors.toSet());
        Set<Long> actual = repo.streamIdsByShiftPriorityBetween(5, 11).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected = initial.stream()
                .filter(r -> r.getPriority() >= 1 && r.getPriority() < 5)
                .map(JpaRule::getId)
                .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriorityBetween(1, 4).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);
    }

    /** Adds sample rules in reverse natural order and returns them in natural order */
    private List<JpaRule> addSamplesInReverseNaturalOrder() {
        JpaRule rule = this.entity;
        List<JpaRule> expected = new ArrayList<>();

        expected.add(new JpaRule(rule));

        rule.getIdentifier().setAccess(JpaGrantType.LIMIT);
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setAddressRange(new JpaIPAddressRange(1000L, 2000L, 32));
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setService("service");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setRequest("request");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setRolename("rolename");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setUsername("user");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setWorkspace("workspace");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setLayer("layer");
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setAccess(JpaGrantType.ALLOW);
        expected.add(new JpaRule(rule));

        rule.getIdentifier().setSubfield("subfield");
        expected.add(new JpaRule(rule));

        IntStream.range(0, expected.size()).forEach(p -> expected.get(p).setPriority(1 + p));

        List<JpaRule> reversed = new ArrayList<>(expected);
        Collections.reverse(reversed);
        repo.saveAllAndFlush(reversed);

        Collections.sort(expected, (r1, r2) -> Long.compare(r1.getPriority(), r2.getPriority()));
        return expected;
    }

    private JpaLayerAttribute latt(String name) {
        return new JpaLayerAttribute()
                .setAccess(AccessType.NONE)
                .setDataType("Integer")
                .setName(name);
    }

    private org.geolatte.geom.MultiPolygon<?> geom(String wkt) {
        return (org.geolatte.geom.MultiPolygon<?>) Wkt.fromWkt(wkt, CoordinateReferenceSystems.WGS84);
    }
}
