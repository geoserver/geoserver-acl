package org.geoserver.acl.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.querydsl.core.types.Predicate;

import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geoserver.acl.jpa.config.AclDataSourceConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.model.CatalogMode;
import org.geoserver.acl.jpa.model.GrantType;
import org.geoserver.acl.jpa.model.IPAddressRange;
import org.geoserver.acl.jpa.model.LayerAttribute;
import org.geoserver.acl.jpa.model.LayerAttribute.AccessType;
import org.geoserver.acl.jpa.model.LayerDetails;
import org.geoserver.acl.jpa.model.LayerDetails.LayerType;
import org.geoserver.acl.jpa.model.QRule;
import org.geoserver.acl.jpa.model.Rule;
import org.geoserver.acl.jpa.model.RuleIdentifier;
import org.geoserver.acl.jpa.model.RuleLimits;
import org.geoserver.acl.jpa.model.SpatialFilterType;
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
import java.util.stream.Collectors;
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
public class JpaRuleRepositoryTest {

    private static final String WORLD =
            "MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired JpaRuleRepository repo;

    private @Autowired EntityManager em;

    private Rule entity;

    @BeforeEach
    void beforeEach() {
        entity = new Rule();
    }

    @Test
    void testIdentifierDefaultValues() {
        Rule rule = new Rule();
        assertNotNull(rule.getIdentifier());
        RuleIdentifier identifier = rule.getIdentifier();
        assertEquals("*", identifier.getInstance());
        assertEquals("*", identifier.getLayer());
        assertEquals("*", identifier.getRequest());
        assertEquals("*", identifier.getRolename());
        assertEquals("*", identifier.getService());
        assertEquals("*", identifier.getSubfield());
        assertEquals("*", identifier.getUsername());
        assertEquals("*", identifier.getWorkspace());
        assertEquals(GrantType.DENY, identifier.getAccess());
        assertEquals(IPAddressRange.noData(), identifier.getAddressRange());
    }

    @Test
    void testIdentifierDoesNotAllowNull() {
        // non nullable attributes in RuleIdentified can't even be set to null
        RuleIdentifier identifier = entity.getIdentifier();
        assertThrows(NullPointerException.class, () -> identifier.setAccess(null));
        assertThrows(NullPointerException.class, () -> identifier.setAddressRange(null));
        assertThrows(NullPointerException.class, () -> identifier.setLayer(null));
        assertThrows(NullPointerException.class, () -> identifier.setRequest(null));
        assertThrows(NullPointerException.class, () -> identifier.setRolename(null));
        assertThrows(NullPointerException.class, () -> identifier.setService(null));
        assertThrows(NullPointerException.class, () -> identifier.setSubfield(null));
        assertThrows(NullPointerException.class, () -> identifier.setUsername(null));
        assertThrows(NullPointerException.class, () -> identifier.setWorkspace(null));

        entity.getIdentifier().setInstance(null);
        DataIntegrityViolationException expected =
                assertThrows(DataIntegrityViolationException.class, () -> repo.save(entity));
        assertThat(expected)
                .hasMessageContaining("not-null property references a null or transient value")
                .hasMessageContaining("identifier.instance");
    }

    @Test
    void testSave_Identifier_defaultValues() {

        RuleIdentifier expected = entity.getIdentifier().clone();

        Rule saved = repo.saveAndFlush(entity);
        assertSame(entity, saved);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void testSave_Identifier() {
        RuleIdentifier expected =
                entity.getIdentifier()
                        .setAccess(GrantType.DENY)
                        .setAddressRange(new IPAddressRange(1000L, 2000L, 32))
                        .setInstance("gsInstance")
                        .setLayer("layer")
                        .setRequest("GetCapabilities")
                        .setRolename("ROLE_USER")
                        .setService("WCS")
                        .setSubfield("subfield")
                        .setUsername("user")
                        .setWorkspace("workspace")
                        .clone();

        Rule saved = repo.saveAndFlush(entity);
        em.detach(saved);

        Rule found = repo.getReferenceById(saved.getId());
        assertThat(found.getIdentifier()).isNotSameAs(saved.getIdentifier()).isEqualTo(expected);
    }

    @Test
    void testRuleLimits() {
        Rule rule = repo.saveAndFlush(entity);
        assertNull(rule.getRuleLimits());

        rule.setRuleLimits(
                new RuleLimits()
                        .setAllowedArea(geom(WORLD))
                        .setCatalogMode(CatalogMode.MIXED)
                        .setSpatialFilterType(SpatialFilterType.CLIP));

        RuleLimits expected = rule.getRuleLimits().clone();

        repo.saveAndFlush(rule);
        rule = repo.findById(rule.getId()).orElseThrow();

        assertNotNull(rule.getRuleLimits());
        assertThat(rule.getRuleLimits()).isEqualTo(expected);
    }

    @Test
    void testLayerDetails() {

        final MultiPolygon<?> area = geom(WORLD);

        Set<LayerAttribute> attributes = Set.of(latt("att1"), latt("att2"));
        final LayerDetails details =
                new LayerDetails()
                        .setAllowedStyles(Set.of("s1", "s2"))
                        .setArea(area)
                        .setAttributes(attributes)
                        .setCatalogMode(CatalogMode.CHALLENGE)
                        .setCqlFilterRead("a=b")
                        .setCqlFilterWrite("foo=bar")
                        .setDefaultStyle("defstyle")
                        .setSpatialFilterType(SpatialFilterType.CLIP)
                        .setType(LayerType.LAYERGROUP)
                        .clone();

        LayerDetails expected = details.clone();

        entity.setLayerDetails(details);
        entity = repo.saveAndFlush(entity);
        em.detach(entity);

        Rule saved = repo.findById(entity.getId()).orElseThrow();

        assertThat(saved.getLayerDetails()).isEqualTo(expected);
    }

    @Test
    void testLayerDetails_unset_allowedStyles() {
        entity.setLayerDetails(
                new LayerDetails()
                        .setAllowedStyles(Set.of("s1", "s2"))
                        .setCatalogMode(CatalogMode.CHALLENGE));
        entity = repo.saveAndFlush(entity);
        final long ruleId = entity.getId();
        em.detach(entity);

        Rule rule = repo.findById(ruleId).orElseThrow();
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
                new LayerDetails()
                        .setAllowedStyles(Set.of("s1", "s2"))
                        .setCatalogMode(CatalogMode.CHALLENGE));
        entity = repo.saveAndFlush(entity);
        final long ruleId = entity.getId();
        em.detach(entity);

        Rule rule = repo.findById(ruleId).orElseThrow();

        assertNotSame(entity, rule);

        Set<String> newStyles = Set.of("newstyle1", "s1", "newstyle2");

        rule.getLayerDetails().getAllowedStyles().clear();
        rule.getLayerDetails().getAllowedStyles().addAll(newStyles);
        repo.saveAndFlush(rule);
        em.detach(rule);

        rule = repo.findById(ruleId).orElseThrow();

        assertThat(rule.getLayerDetails().getAllowedStyles())
                .isEqualTo(Set.of("newstyle1", "s1", "newstyle2"));
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder()} */
    @Test
    void findAll() {
        List<Rule> expected = addSamplesInReverseNaturalOrder();
        List<Rule> actual = repo.findAll();
        assertEquals(Set.copyOf(expected), Set.copyOf(actual));
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder(Predicate)} */
    @Test
    void findAllNaturalOrderFiltered() {
        final List<Rule> all = addSamplesInReverseNaturalOrder();

        QRule qRule = QRule.rule;
        Predicate predicate = qRule.priority.gt(2L).and(qRule.identifier.layer.eq("*"));

        List<Rule> expected =
                all.stream()
                        .filter(
                                r ->
                                        r.getPriority() > 2L
                                                && "*".equals(r.getIdentifier().getLayer()))
                        .collect(Collectors.toList());

        Iterable<Rule> res = repo.findAll(predicate, Sort.by("priority"));
        List<Rule> actual = new ArrayList<>();
        res.forEach(actual::add);
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testShiftPriority() {
        List<Rule> initial = addSamplesInReverseNaturalOrder();
        assertThat(initial.get(0).getPriority()).isEqualTo(1);

        int affectedCount = repo.shiftPriority(1, 10);
        assertThat(affectedCount).isEqualTo(initial.size());
        initial.forEach(
                prev -> {
                    Rule curr = repo.getReferenceById(prev.getId());
                    long actual = curr.getPriority();
                    long expected = 10 + prev.getPriority();
                    assertThat(actual).isEqualTo(expected);
                });
    }

    @Test
    void testStreamIdsByShiftPriority() {
        List<Rule> initial = addSamplesInReverseNaturalOrder();
        // preflight
        assertThat(initial.get(0).getPriority()).isEqualTo(1);
        assertThat(initial.size()).isEqualTo(11);

        Set<Long> expected = initial.stream().map(Rule::getId).collect(Collectors.toSet());
        Set<Long> actual = repo.streamIdsByShiftPriority(1).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected =
                initial.stream()
                        .filter(r -> r.getPriority() >= 5)
                        .map(Rule::getId)
                        .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriority(5).collect(Collectors.toSet());

        assertThat(actual).isEqualTo(expected);

        expected =
                initial.stream()
                        .filter(r -> r.getPriority() >= 10)
                        .map(Rule::getId)
                        .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriority(10).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected = Set.of();
        actual = repo.streamIdsByShiftPriority(12).collect(Collectors.toSet());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testShiftPriorityBetween() {
        List<Rule> initial = addSamplesInReverseNaturalOrder();
        assertThat(initial.get(0).getPriority()).isEqualTo(1);

        // shift priorities 5 to 11 by 10
        repo.shiftPrioritiesBetween(5, 11, 10);
        initial.forEach(
                prev -> {
                    Rule curr = repo.getReferenceById(prev.getId());
                    long actual = curr.getPriority();
                    long expected =
                            prev.getPriority() < 5 ? prev.getPriority() : 10 + prev.getPriority();
                    assertThat(actual).isEqualTo(expected);
                });

        // shift priorities 1 to 4 by 3
        repo.shiftPrioritiesBetween(1, 4, 3);
        initial.forEach(
                prev -> {
                    Rule curr = repo.getReferenceById(prev.getId());
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
        List<Rule> initial = addSamplesInReverseNaturalOrder();
        // preflight
        assertThat(initial.get(0).getPriority()).isEqualTo(1);
        assertThat(initial.size()).isEqualTo(11);

        Set<Long> expected =
                initial.stream()
                        .filter(r -> r.getPriority() >= 5)
                        .map(Rule::getId)
                        .collect(Collectors.toSet());
        Set<Long> actual = repo.streamIdsByShiftPriorityBetween(5, 11).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);

        expected =
                initial.stream()
                        .filter(r -> r.getPriority() >= 1 && r.getPriority() < 5)
                        .map(Rule::getId)
                        .collect(Collectors.toSet());
        actual = repo.streamIdsByShiftPriorityBetween(1, 4).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);
    }

    /** Adds sample rules in reverse natural order and returns them in natural order */
    private List<Rule> addSamplesInReverseNaturalOrder() {
        Rule rule = this.entity;
        List<Rule> expected = new ArrayList<>();

        expected.add(rule.clone());

        rule.getIdentifier().setAccess(GrantType.LIMIT);
        expected.add(rule.clone());

        rule.getIdentifier().setAddressRange(new IPAddressRange(1000L, 2000L, 32));
        expected.add(rule.clone());

        rule.getIdentifier().setService("service");
        expected.add(rule.clone());

        rule.getIdentifier().setRequest("request");
        expected.add(rule.clone());

        rule.getIdentifier().setRolename("rolename");
        expected.add(rule.clone());

        rule.getIdentifier().setUsername("user");
        expected.add(rule.clone());

        rule.getIdentifier().setWorkspace("workspace");
        expected.add(rule.clone());

        rule.getIdentifier().setLayer("layer");
        expected.add(rule.clone());

        rule.getIdentifier().setAccess(GrantType.ALLOW);
        expected.add(rule.clone());

        rule.getIdentifier().setSubfield("subfield");
        expected.add(rule.clone());

        IntStream.range(0, expected.size()).forEach(p -> expected.get(p).setPriority(1 + p));

        List<Rule> reversed = new ArrayList<>(expected);
        Collections.reverse(reversed);
        repo.saveAllAndFlush(reversed);

        Collections.sort(expected, (r1, r2) -> Long.compare(r1.getPriority(), r2.getPriority()));
        return expected;
    }

    private LayerAttribute latt(String name) {
        return new LayerAttribute().setAccess(AccessType.NONE).setDataType("Integer").setName(name);
    }

    private org.geolatte.geom.MultiPolygon<?> geom(String wkt) {
        return (org.geolatte.geom.MultiPolygon<?>)
                Wkt.fromWkt(wkt, CoordinateReferenceSystems.WGS84);
    }
}
