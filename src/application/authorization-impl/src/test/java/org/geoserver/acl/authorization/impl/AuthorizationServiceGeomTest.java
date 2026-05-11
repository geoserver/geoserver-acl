/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.CatalogMode.HIDE;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;
import static org.geoserver.acl.domain.rules.GrantType.LIMIT;
import static org.geoserver.acl.domain.rules.SpatialFilterType.CLIP;
import static org.geoserver.acl.domain.rules.SpatialFilterType.INTERSECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.jts.JTS;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.SpatialFilterType;
import org.junit.jupiter.api.Test;

/**
 * {@link AuthorizationService} integration/conformance test working with geometries
 *
 * <p>Concrete implementations must supply the required services in {@link
 * BaseAuthorizationServiceTest}
 */
@SuppressWarnings("unused")
public abstract class AuthorizationServiceGeomTest extends BaseAuthorizationServiceTest {
    /*
     * Simple test geometries with their spatial relationships:
     *
     *               (10,30)                                                     (40,30)
     * +-------------------+                             +-----------------------------+
     * |                   |                             |                             |
     * |        P2         |                             |             P4              |
     * |                   |                             |                             |
     * |                   |                             |               (36,25)       |
     * |           +-------+-----------------------------+---------------------+       |
     * |           |       |                             |                     |       |
     * |           |       |               P3            |                     |       |
     * |           |       |                             |                     |       |
     * |           |       |                   (23,20)   |                     |       |
     * |     +-----+-------+-------------------------+   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |     P1                  |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     |     |       |                         |   |                     |       |
     * |     +-----+-------+-------------------------+   |                     |       |
     * |     (3,10)|       |                             |                     |       |
     * |           |       |                             |                     |       |
     * |           |       |                             |                     |       |
     * |           |       |                             |                     |       |
     * |           +-------+-----------------------------+---------------------+       |
     * |           (6,5)   |                             |                             |
     * |                   |                             |                             |
     * |                   |                             |                             |
     * |                   |                             |                             |
     * +-------------------+                             +-----------------------------+
     * (0,0)                                             (25,0)
     *
     * WKT_WGS84_1 (P1): (3,10) to (23,20)  - overlaps with P2 and P3
     * WKT_WGS84_2 (P2): (0,0)  to (10,30)  - overlaps with P1 and P3
     * WKT_WGS84_3 (P3): (6,5)  to (36,25)  - overlaps with P1, P2, and P4
     * WKT_WGS84_4 (P4): (25,0) to (40,30)  - overlaps with P3
     */
    private static final String WKT_WGS84_1 = "SRID=4326;MULTIPOLYGON(((3 10, 3 20, 23 20, 23 10, 3 10)))";
    private static final String WKT_WGS84_2 = "SRID=4326;MULTIPOLYGON(((0 0, 0 30, 10 30, 10 0, 0 0)))";
    private static final String WKT_WGS84_3 = "SRID=4326;MULTIPOLYGON(((6 5, 6 25, 36 25, 36 5, 6 5)))";
    private static final String WKT_WGS84_4 = "SRID=4326;MULTIPOLYGON(((25 0, 25 30, 40 30, 40 0, 25 0)))";

    private static final String WKT_3003 =
            "SRID=3003;MultiPolygon (((1680529.71478682174347341 4849746.00902365241199732, 1682436.7076464940328151 4849731.7422441728413105, 1682446.21883281995542347 4849208.62699576932936907, 1680524.95919364970177412 4849279.96089325752109289, 1680529.71478682174347341 4849746.00902365241199732)))";
    private static final String WKT_23032 =
            "SRID=23032;MultiPolygon (((680588.67850254673976451 4850060.34823693986982107, 681482.71827003755606711 4850469.32878803834319115, 682633.56349697941914201 4849499.20374245755374432, 680588.67850254673976451 4850060.34823693986982107)))";
    private static final String WKT_3857 =
            "SRID=3857;MULTIPOLYGON(((0.0016139656066815888 -0.0006386457758059581,0.0019599705696027314 -0.0006386457758059581,0.0019599705696027314 -0.0008854090051601674,0.0016139656066815888 -0.0008854090051601674,0.0016139656066815888 -0.0006386457758059581)))";

    /**
     * Test that the original SRID is present in the allowedArea wkt representation, when retrieving
     * it from the AccessInfo object
     */
    @Test
    public void testRuleLimitsAllowedAreaSRIDIsPreserved() {
        Rule r1 = insert(10, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(r1, WKT_3857);

        Rule r2 = insert(11, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);

        AccessRequest request = AccessRequest.builder()
                .service("s1")
                .request("r1")
                .workspace("w1")
                .layer("l1")
                .build();
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        Geometry<?> area = accessInfo.intersectArea();
        assertEquals(3857, area.getCoordinateReferenceSystem().getCrsId().getCode());
    }

    private RuleLimits setRuleLimits(Rule rule, String allowedAreaWKT) {
        return setRuleLimits(
                rule, allowedAreaWKT, RuleLimits.DEFAULT_SPATIAL_FILTERTYPE, RuleLimits.DEFAULT_CATALOG_MODE);
    }

    private RuleLimits setRuleLimits(
            Rule rule, String allowedAreaWKT, SpatialFilterType spatialFilterType, CatalogMode catalogMode) {
        MultiPolygon<?> allowedArea = (MultiPolygon<?>) Wkt.fromWkt(allowedAreaWKT);
        RuleLimits limits = RuleLimits.builder()
                .allowedArea(allowedArea)
                .spatialFilterType(spatialFilterType)
                .catalogMode(catalogMode)
                .build();
        ruleAdminService.setLimits(rule.id(), limits);
        return limits;
    }

    /**
     * Test that the original SRID is present in the allowedArea wkt representation,when retrieving
     * it from the AccessInfo object
     */
    @Test
    public void testRuleLimitsAllowedAreaReprojectionWithDifferentSrid() {
        Rule r1 = insert(999, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);

        Rule r2 = insert(11, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(r2, WKT_3003);

        Rule r3 = insert(12, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(r3, WKT_23032);

        AccessRequest request = AccessRequest.builder()
                .service("s1")
                .request("r1")
                .workspace("w1")
                .layer("l1")
                .build();

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        Geometry<?> area = accessInfo.intersectArea();
        assertEquals(3003, area.getCoordinateReferenceSystem().getCrsId().getCode());
    }

    /**
     * test that when we have two rules referring to the same group one having a filter type {@link
     * SpatialFilterType#INTERSECT INTERSECT} and the other one having filter type {@link
     * SpatialFilterType#CLIP}, the result is a clip area obtained by the intersection of the two.
     */
    @Test
    public void testRuleSpatialFilterTypeClipSameGroup() {
        Rule p10 = insert(10, "auth11", "group1", null, "s1", "r1", null, "w1", "l1", LIMIT);
        RuleLimits limitsp10 = setRuleLimits(p10, WKT_WGS84_1, CLIP, HIDE);

        Rule p11 = insert(11, "auth11", "group1", null, "s1", "r1", null, "w1", "l1", LIMIT);
        RuleLimits llimitsp11 = setRuleLimits(p11, WKT_WGS84_3, INTERSECT, HIDE);

        Rule p9999 = insert(9999, null, "group1", null, "s1", "r1", null, "w1", "l1", ALLOW);
        Rule p10k = insert(10_000, null, "group2", null, null, null, null, null, null, DENY);

        final AccessRequest request = createRequest("auth11", "group1", "group2")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(List.of(p10.id(), p11.id(), p9999.id(), p10k.id()));

        // area in same group, the result should the itersection of the two allowed area
        // as a clip
        // geometry.
        org.locationtech.jts.geom.Geometry testArea =
                JTS.to(limitsp10.allowedArea()).intersection(JTS.to(llimitsp11.allowedArea()));
        testArea.normalize();
        assertNull(accessInfo.intersectArea());
        assertNotNull(accessInfo.clipArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.clipArea());
        resultArea.normalize();
        assertTrue(testArea.equalsExact(resultArea, 10.0E-15));
    }

    /**
     * test that when we have two rules referring to the same group both having a filter type
     * Intersects the result is an intersect area obtained by the intersection of the two.
     */
    @Test
    public void testRuleSpatialFilterTypeIntersectsSameGroup() {

        Rule p13 = insert(13, "u1", "g1", null, "s11", "r11", null, "w11", "l11", LIMIT);
        RuleLimits limitsp13 = setRuleLimits(p13, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p14 = insert(14, "u1", "g1", null, "s11", "r11", null, "w11", "l11", LIMIT);
        RuleLimits limitsp14 = setRuleLimits(p14, WKT_WGS84_3, INTERSECT, HIDE);

        Rule p9999 = insert(9999, null, "g1", null, "s11", "r11", null, "w11", "l11", ALLOW);

        AccessRequest request = createRequest("u1", "g1", "g2")
                .withService("s11")
                .withRequest("r11")
                .withWorkspace("w11")
                .withLayer("l11");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(List.of(p13.id(), p14.id(), p9999.id()));

        // area in same group, the result should be the
        // two allowed area as an intersects geometry.
        org.locationtech.jts.geom.Geometry testArea =
                JTS.to(limitsp13.allowedArea()).intersection(JTS.to(limitsp14.allowedArea()));
        testArea.normalize();
        assertNull(accessInfo.clipArea());
        assertNotNull(accessInfo.intersectArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.intersectArea());
        resultArea.normalize();
        assertTrue(testArea.equalsExact(resultArea, 10.0E-15));
    }

    /**
     * test the access enalargement behaviour with the SpatialFilterType. the user belongs to two
     * groups. One with an allowedArea of type intersects, the other one with an allowed area of
     * type clip. They should be returned separately in the final rule.
     */
    @Test
    public void testRuleSpatialFilterTypeEnlargeAccess() {

        Rule p15 = insert(15, null, "group22", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp15 = setRuleLimits(p15, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p16 = insert(16, null, "group23", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp16 = setRuleLimits(p16, WKT_WGS84_3, CLIP, HIDE);

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        AccessRequest request = createRequest("auth22", "group22", "group23")
                .withService("s22")
                .withRequest("r22")
                .withWorkspace("w22")
                .withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.grant());

        // we got a user in two groups one with an intersect spatialFilterType
        // and the other with a clip spatialFilterType. The two area should haven
        // been kept separated
        assertNotNull(accessInfo.intersectArea());
        assertNotNull(accessInfo.clipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry intersects = JTS.to(accessInfo.intersectArea());
        intersects.normalize();
        assertTrue(intersects.equalsExact(JTS.to(lp15.allowedArea()), 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.clipArea());
        clip.normalize();
        org.locationtech.jts.geom.MultiPolygon area2Jts = JTS.to(lp16.allowedArea());
        area2Jts.normalize();
        assertTrue(clip.equalsExact(area2Jts, 10.0E-15));
    }

    /**
     * the user belongs to two groups and there are two rules for each group: INTERSECTS and CLIP
     * for the first, and CLIP CLIP for the second. The expected result is only one allowedArea of
     * type clip obtained by the intersection of the firs two, united with the intersection of the
     * second two. the first INTERSECTS is resolve as CLIP because during constraint resolution the
     * more restrictive type is chosen.
     */
    @Test
    public void testRuleSpatialFilterTypeFourRules() {

        Rule p17 = insert(17, null, "group31", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp17 = setRuleLimits(p17, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p18 = insert(18, null, "group31", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp18 = setRuleLimits(p18, WKT_WGS84_2, CLIP, HIDE);

        Rule p19 = insert(19, null, "group32", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp19 = setRuleLimits(p19, WKT_WGS84_3, CLIP, HIDE);

        Rule p20 = insert(20, null, "group32", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp20 = setRuleLimits(p20, WKT_WGS84_4, CLIP, HIDE);

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        AccessRequest request = createRequest("auth33", "group31", "group32")
                .withService("s22")
                .withRequest("r22")
                .withWorkspace("w22")
                .withLayer("l22");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.grant());

        // we should have only the clip geometry
        assertNull(accessInfo.intersectArea());
        assertNotNull(accessInfo.clipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedResult = JTS.to(lp17.allowedArea())
                .intersection(JTS.to(lp18.allowedArea()))
                .union(JTS.to(lp19.allowedArea()).intersection(JTS.to(lp20.allowedArea())));
        expectedResult.normalize();
        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.clipArea());
        clip.normalize();
        assertTrue(clip.equalsExact(expectedResult, 10.0E-15));
    }

    /**
     * the user belongs to two groups and there are two rules for each group: CLIP and CLIP for the
     * first, and INTERSECTS INTERSECTS for the second. The expected result are two allowedArea the
     * first of type clip and second of type intersects.
     */
    @Test
    public void testRuleSpatialFilterTypeFourRules2() {

        Rule p21 = insert(21, null, "group41", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp21 = setRuleLimits(p21, WKT_WGS84_1, CLIP, HIDE);

        Rule p22 = insert(22, null, "group41", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp22 = setRuleLimits(p22, WKT_WGS84_2, CLIP, HIDE);

        Rule p23 = insert(23, null, "group42", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp23 = setRuleLimits(p23, WKT_WGS84_3, INTERSECT, HIDE);

        Rule p24 = insert(24, null, "group42", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp24 = setRuleLimits(p24, WKT_WGS84_4, INTERSECT, HIDE);

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        AccessRequest request = createRequest("auth44", "group41", "group42")
                .withService("s22")
                .withRequest("r22")
                .withWorkspace("w22")
                .withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        // we should have both
        assertThat(accessInfo.intersectArea()).isNotNull();
        assertThat(accessInfo.clipArea()).isNotNull();

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedIntersects =
                JTS.to(lp23.allowedArea()).intersection(JTS.to(lp24.allowedArea()));
        expectedIntersects.normalize();
        org.locationtech.jts.geom.Geometry intersects = JTS.to(accessInfo.intersectArea());
        intersects.normalize();

        assertTrue(expectedIntersects.equalsExact(intersects, 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.clipArea());
        clip.normalize();
        org.locationtech.jts.geom.Geometry expectedClip =
                JTS.to(lp21.allowedArea()).intersection(JTS.to(lp22.allowedArea()));
        expectedClip.normalize();
        assertTrue(expectedClip.equalsExact(clip, 10.0E-15));
    }

    /**
     * Mirror of {@link #testRuleSpatialFilterTypeEnlargeAccess()}: two roles, one CLIP-only and
     * one INTERSECT-only, with role names chosen so that the per-role iteration order makes the
     * CLIP role become {@code baseAccess} and the INTERSECT role become {@code moreAccess} in
     * {@code setAllowedAreas}. Hits the {@code moreIntersects != null && baseClip != null} and
     * {@code baseClip != null && moreIntersects != null} fall-through branches.
     *
     * <p>Result is symmetric with the original test and not interesting on its own; the test
     * exists for branch coverage of the symmetric path.
     */
    @Test
    public void testRuleSpatialFilterTypeEnlargeAccess_clipBaseIntersectMore() {
        Rule pClip = insert(15, null, "z_grpClip", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lpClip = setRuleLimits(pClip, WKT_WGS84_3, CLIP, HIDE);

        Rule pIntersect = insert(16, null, "a_grpInt", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lpIntersect = setRuleLimits(pIntersect, WKT_WGS84_1, INTERSECT, HIDE);

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        AccessRequest request = createRequest("auth_mirror", "z_grpClip", "a_grpInt")
                .withService("s22")
                .withRequest("r22")
                .withWorkspace("w22")
                .withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.intersectArea()).isNotNull();
        assertThat(accessInfo.clipArea()).isNotNull();

        org.locationtech.jts.geom.Geometry intersect = JTS.to(accessInfo.intersectArea());
        intersect.normalize();
        assertTrue(intersect.equalsExact(JTS.to(lpIntersect.allowedArea()), 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.clipArea());
        clip.normalize();
        org.locationtech.jts.geom.MultiPolygon clipExpected = JTS.to(lpClip.allowedArea());
        clipExpected.normalize();
        assertTrue(clip.equalsExact(clipExpected, 10.0E-15));
    }

    /**
     * Two roles, each contributing an INTERSECT-only allowed area. {@code unionIntersects} is the
     * real JTS union; exercises the {@code else} arm that calls {@code ret.intersectArea(...)}
     * with the unioned geometry.
     */
    @Test
    public void testEnlargeAccess_intersectUnionedAcrossRoles() {
        Rule pA = insert(20, null, "groupA", null, "s33", "r33", null, "w33", "l33", LIMIT);
        RuleLimits lpA = setRuleLimits(pA, WKT_WGS84_1, INTERSECT, HIDE);

        Rule pB = insert(21, null, "groupB", null, "s33", "r33", null, "w33", "l33", LIMIT);
        RuleLimits lpB = setRuleLimits(pB, WKT_WGS84_3, INTERSECT, HIDE);

        insert(999, null, null, null, "s33", "r33", null, "w33", "l33", ALLOW);

        AccessRequest request = createRequest("auth_inter_union", "groupA", "groupB")
                .withService("s33")
                .withRequest("r33")
                .withWorkspace("w33")
                .withLayer("l33");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.intersectArea()).isNotNull();
        assertThat(accessInfo.clipArea()).isNull();

        org.locationtech.jts.geom.Geometry expected = JTS.to(lpA.allowedArea()).union(JTS.to(lpB.allowedArea()));
        expected.normalize();
        org.locationtech.jts.geom.Geometry actual = JTS.to(accessInfo.intersectArea());
        actual.normalize();
        assertTrue(expected.equalsExact(actual, 10.0E-15));
    }

    /**
     * One role has an INTERSECT-only spatial restriction, another role has no spatial restriction
     * at all. Since the second role on its own grants unrestricted access, the union must not
     * restrict spatially: the more permissive rule wins.
     *
     * <p>Exercises the {@code unionIntersects == null} fall-through path in
     * {@code AuthorizationServiceImpl#setAllowedAreas}: after geometry-union honors the GeoFence
     * "null when either side is null" semantics, this case enters the {@code if} block but neither
     * inner branch matches, leaving the result without a spatial restriction.
     */
    @Test
    public void testEnlargeAccess_intersectVsNoSpatialRestriction() {
        // group_alpha: LIMIT INTERSECT + matches the general ALLOW
        Rule limit = insert(1, null, "group_alpha", null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(limit, WKT_WGS84_1, INTERSECT, HIDE);
        // shared ALLOW for any role; group_beta has no LIMIT, so it produces no spatial restriction
        insert(999, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);

        AccessRequest request = createRequest("user", "group_alpha", "group_beta")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.intersectArea())
                .as("group_beta has no spatial restriction; the union must not restrict spatially")
                .isNull();
        assertThat(accessInfo.clipArea()).isNull();
    }

    /**
     * Mirror of {@link #testEnlargeAccess_intersectVsNoSpatialRestriction()} for the CLIP path:
     * one role restricts via CLIP, another has no spatial restriction. The union must not restrict
     * spatially.
     */
    @Test
    public void testEnlargeAccess_clipVsNoSpatialRestriction() {
        Rule limit = insert(1, null, "group_alpha", null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(limit, WKT_WGS84_1, CLIP, HIDE);
        insert(999, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);

        AccessRequest request = createRequest("user", "group_alpha", "group_beta")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.intersectArea()).isNull();
        assertThat(accessInfo.clipArea())
                .as("group_beta has no spatial restriction; the union must not restrict spatially")
                .isNull();
    }

    @Test
    public void testLimitAndAllowRuleEnlargementLayerGroup() {
        Rule limit = insert(1, null, "ROLE_ONE", null, "wms", null, null, null, "lakes_and_places", LIMIT);
        RuleLimits geomLimits = setRuleLimits(limit, WKT_WGS84_3, INTERSECT, HIDE);

        Rule allow = insert(2, null, null, null, null, null, null, null, null, ALLOW);

        // ROLE_ONE matches both rules; ROLE_TWO only the allow rule. ROLE_TWO has no spatial
        // restriction at all, so the union of the two roles must not restrict spatially: the
        // more permissive rule wins.
        AccessRequest request = createRequest("gabe", "ROLE_ONE", "ROLE_TWO")
                .withService("WMS")
                .withRequest("GetMap")
                .withWorkspace(null)
                .withLayer("lakes_and_places");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.intersectArea())
                .as("ROLE_TWO grants unrestricted access; the union must not restrict spatially")
                .isNull();
        assertThat(accessInfo.clipArea()).isNull();
    }
}
