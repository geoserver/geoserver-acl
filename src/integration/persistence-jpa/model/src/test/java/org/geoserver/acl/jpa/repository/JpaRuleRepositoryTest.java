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
import org.geoserver.acl.jpa.config.AuthorizationDataSourceConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAConfiguration;
import org.geoserver.acl.jpa.config.AuthorizationJPAPropertiesTestConfiguration;
import org.geoserver.acl.jpa.model.CatalogMode;
import org.geoserver.acl.jpa.model.GeoServerInstance;
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
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Transactional
@SpringBootTest(
        classes = {
            AuthorizationJPAPropertiesTestConfiguration.class,
            AuthorizationDataSourceConfiguration.class,
            AuthorizationJPAConfiguration.class
        })
@ActiveProfiles("test")
public class JpaRuleRepositoryTest {

    private static final String WORLD =
            "MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))";

    private @Autowired JpaGeoServerInstanceRepository instanceRepo;
    private @Autowired JpaRuleRepository repo;

    private @Autowired EntityManager em;

    private GeoServerInstance anyInstance;

    private Rule entity;

    @BeforeEach
    void beforeEach() {
        anyInstance = instanceRepo.getInstanceAny();

        entity = new Rule();
        entity.getIdentifier().setInstance(anyInstance);
    }

    @Test
    void testIdentifierDefaultValues() {
        Rule rule = new Rule();
        assertNotNull(rule.getIdentifier());
        RuleIdentifier identifier = rule.getIdentifier();
        assertEquals("*", identifier.getLayer());
        assertEquals("*", identifier.getRequest());
        assertEquals("*", identifier.getRolename());
        assertEquals("*", identifier.getService());
        assertEquals("*", identifier.getSubfield());
        assertEquals("*", identifier.getUsername());
        assertEquals("*", identifier.getWorkspace());
        assertEquals(GrantType.DENY, identifier.getAccess());
        assertEquals(IPAddressRange.noData(), identifier.getAddressRange());
        assertNull(identifier.getInstance());
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

    @Disabled
    @Test
    void testSaveDuplicateIdentifier_default_values() {
        testSaveDuplicateIdentifier(entity);
    }

    @Disabled
    @Test
    void testSaveDuplicateIdentifier() {
        entity.getIdentifier()
                .setAccess(GrantType.LIMIT)
                .setAddressRange(new IPAddressRange(1000L, 2000L, 32))
                .setInstance(anyInstance)
                .setLayer("layer")
                .setRequest("GetCapabilities")
                .setRolename("ROLE_USER")
                .setService("WCS")
                .setSubfield("subfield")
                .setUsername("user")
                .setWorkspace("workspace");

        testSaveDuplicateIdentifier(entity);
    }

    private void testSaveDuplicateIdentifier(Rule rule) {
        rule = rule.clone().setId(null);
        Rule duplicateKey = rule.clone().setPriority(rule.getPriority() + 1000);
        assertEquals(rule.getIdentifier(), duplicateKey.getIdentifier());

        repo.saveAndFlush(rule);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicateKey));
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
        GeoServerInstance gsInstance2 =
                new GeoServerInstance()
                        .setName("secondInstance")
                        .setBaseURL("http://localhost:9090/geoserver")
                        .setDescription("Default geoserver instance")
                        .setUsername("admin")
                        .setPassword("geoserver");

        em.persist(gsInstance2);

        RuleIdentifier expected =
                entity.getIdentifier()
                        .setAccess(GrantType.DENY)
                        .setAddressRange(new IPAddressRange(1000L, 2000L, 32))
                        .setInstance(gsInstance2)
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
    void testSave_fails_on_dettached_GeoServerInstance() {

        GeoServerInstance unsavedGsInstance =
                new GeoServerInstance()
                        .setName("unsaved")
                        .setBaseURL("http://localhost:8080/geoserver");

        entity.getIdentifier().setInstance(unsavedGsInstance).clone();

        InvalidDataAccessApiUsageException expected =
                assertThrows(
                        InvalidDataAccessApiUsageException.class, () -> repo.saveAndFlush(entity));
        assertThat(expected.getCause())
                .isInstanceOf(IllegalStateException.class)
                .getCause()
                .isInstanceOf(TransientObjectException.class);
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
    void findAllNaturalOrder() {
        List<Rule> expected = addSamplesInReverseNaturalOrder();
        List<Rule> actual = repo.findAllNaturalOrder();
        assertEquals(expected, actual);
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

        List<Rule> actual = repo.findAllNaturalOrder(predicate);
        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).isEqualTo(expected);
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder(Pageable)} */
    @Test
    void findAllNaturalOrderPaged() {
        final List<Rule> expected = addSamplesInReverseNaturalOrder();

        assertNaturalOrderPaged(
                expected,
                (Predicate) null,
                (predicate, pageable) -> repo.findAllNaturalOrder(pageable));
    }

    /** {@link JpaRuleRepository#findAllNaturalOrder(Predicate, Pageable)} */
    @Test
    void findAllNaturalOrderFilteredAndPaged() {
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

        assertNaturalOrderPaged(expected, predicate, repo::findAllNaturalOrder);
    }

    private void assertNaturalOrderPaged(
            final List<Rule> all,
            Predicate predicate,
            BiFunction<Predicate, Pageable, Page<Rule>> function) {
        final int size = all.size();
        final int pageSize = 2;
        final int pages = 1 + size / pageSize;
        assertThat(pages).isGreaterThan(1);

        for (int pageN = 0; pageN < pages; pageN++) {
            PageRequest request = PageRequest.of(pageN, pageSize);
            Page<Rule> page = function.apply(predicate, request);
            int offset = pageN * pageSize;
            int toIndex = Math.min(offset + pageSize, all.size());
            List<Rule> expectedContents = all.subList(offset, toIndex);
            assertEquals(expectedContents, page.getContent());
        }

        PageRequest request = PageRequest.of(1 + pages, pageSize);
        assertThat(function.apply(predicate, request).getContent()).isEmpty();

        Pageable unpaged = Pageable.unpaged();
        assertThat(function.apply(predicate, unpaged).getContent()).isEqualTo(all);
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

        IntStream.range(0, expected.size()).forEach(p -> expected.get(p).setPriority(p));

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
