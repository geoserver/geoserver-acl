/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

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

import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.jts.JTS;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminServiceImpl;
import org.geoserver.acl.domain.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.MemoryRuleRepository;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleAdminServiceImpl;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.SpatialFilterType;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * {@link AuthorizationService} integration/conformance test working with geometries
 *
 * <p>Concrete implementations must supply the required services in {@link ServiceTestBase}
 */
public class AuthorizationServiceImpl_GeomTest extends ServiceTestBase {
    private static final String WKT_WGS84_1 =
            "SRID=4326;MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
    private static final String WKT_WGS84_2 =
            "SRID=4326;MultiPolygon (((-1.46109090909091011 5.68500000000000139, -0.68600000000000083 5.7651818181818193, -0.73945454545454625 2.00554545454545519, -1.54127272727272846 1.9610000000000003, -1.46109090909091011 5.68500000000000139)))";
    private static final String WKT_WGS84_3 =
            "SRID=4326;MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
    private static final String WKT_WGS84_4 =
            "SRID=4326;MultiPolygon (((-1.30963636363636482 5.96118181818181991, 1.78181818181818175 4.84754545454545571, -0.90872727272727349 2.26390909090909132, -1.30963636363636482 5.96118181818181991)))";

    private static final String WKT_3003 =
            "SRID=3003;MultiPolygon (((1680529.71478682174347341 4849746.00902365241199732, 1682436.7076464940328151 4849731.7422441728413105, 1682446.21883281995542347 4849208.62699576932936907, 1680524.95919364970177412 4849279.96089325752109289, 1680529.71478682174347341 4849746.00902365241199732)))";
    private static final String WKT_23032 =
            "SRID=23032;MultiPolygon (((680588.67850254673976451 4850060.34823693986982107, 681482.71827003755606711 4850469.32878803834319115, 682633.56349697941914201 4849499.20374245755374432, 680588.67850254673976451 4850060.34823693986982107)))";
    private static final String WKT_3857 =
            "SRID=3857;MULTIPOLYGON(((0.0016139656066815888 -0.0006386457758059581,0.0019599705696027314 -0.0006386457758059581,0.0019599705696027314 -0.0008854090051601674,0.0016139656066815888 -0.0008854090051601674,0.0016139656066815888 -0.0006386457758059581)))";

    @Override
    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminServiceImpl(new MemoryRuleRepository());
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return new AdminRuleAdminServiceImpl(new MemoryAdminRuleRepository());
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return new AuthorizationServiceImpl(super.adminruleAdminService, super.ruleAdminService);
    }

    /**
     * Test that the original SRID is present in the allowedArea wkt representation, when retrieving
     * it from the AccessInfo object
     */
    @Test
    public void testRuleLimitsAllowedAreaSRIDIsPreserved() {
        Rule r1 = insert(10, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
        setRuleLimits(r1, WKT_3857);

        Rule r2 = insert(11, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);

        AccessRequest request =
                AccessRequest.builder()
                        .service("s1")
                        .request("r1")
                        .workspace("w1")
                        .layer("l1")
                        .build();
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        Geometry<?> area = accessInfo.getArea();
        assertEquals(3857, area.getCoordinateReferenceSystem().getCrsId().getCode());
    }

    private RuleLimits setRuleLimits(Rule rule, String allowedAreaWKT) {
        return setRuleLimits(
                rule,
                allowedAreaWKT,
                RuleLimits.DEFAULT_SPATIAL_FILTERTYPE,
                RuleLimits.DEFAULT_CATALOG_MODE);
    }

    private RuleLimits setRuleLimits(
            Rule rule,
            String allowedAreaWKT,
            SpatialFilterType spatialFilterType,
            CatalogMode catalogMode) {
        MultiPolygon<?> allowedArea = (MultiPolygon<?>) Wkt.fromWkt(allowedAreaWKT);
        RuleLimits limits =
                RuleLimits.builder()
                        .allowedArea(allowedArea)
                        .spatialFilterType(spatialFilterType)
                        .catalogMode(catalogMode)
                        .build();
        ruleAdminService.setLimits(rule.getId(), limits);
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

        AccessRequest request =
                AccessRequest.builder()
                        .service("s1")
                        .request("r1")
                        .workspace("w1")
                        .layer("l1")
                        .build();

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        Geometry<?> area = accessInfo.getArea();
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

        final AccessRequest request =
                createRequest("auth11", "group1", "group2")
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.getGrant()).isEqualTo(ALLOW);
        assertThat(accessInfo.getMatchingRules())
                .isEqualTo(List.of(p10.getId(), p11.getId(), p9999.getId(), p10k.getId()));

        // area in same group, the result should the itersection of the two allowed area
        // as a clip
        // geometry.
        org.locationtech.jts.geom.Geometry testArea =
                JTS.to(limitsp10.getAllowedArea())
                        .intersection(JTS.to(llimitsp11.getAllowedArea()));
        testArea.normalize();
        assertNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.getClipArea());
        resultArea.normalize();
        assertTrue(testArea.equalsExact(resultArea, 10.0E-15));
    }

    /**
     * test that when we have two rules referring to the same group both having a filter type
     * Intersects the result is an intersect area obtained by the intersection of the two.
     */
    @Test
    public void testRuleSpatialFilterTypeIntersectsSameGroup() {

        Rule p9999 = insert(9999, null, "g1", null, "s11", "r11", null, "w11", "l11", ALLOW);
        Rule p13 = insert(13, "u1", "g1", null, "s11", "r11", null, "w11", "l11", LIMIT);
        RuleLimits limitsp13 = setRuleLimits(p13, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p14 = insert(14, "u1", "g1", null, "s11", "r11", null, "w11", "l11", LIMIT);
        RuleLimits limitsp14 = setRuleLimits(p14, WKT_WGS84_3, INTERSECT, HIDE);

        AccessRequest request =
                createRequest("u1", "g1", "g2")
                        .withService("s11")
                        .withRequest("r11")
                        .withWorkspace("w11")
                        .withLayer("l11");
        // request = user.withWorkspace("w11").withLayer("l11");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.getGrant()).isEqualTo(ALLOW);
        assertThat(accessInfo.getMatchingRules())
                .isEqualTo(List.of(p13.getId(), p14.getId(), p9999.getId()));

        // area in same group, the result should be the
        // two allowed area as an intersects geometry.
        org.locationtech.jts.geom.Geometry testArea =
                JTS.to(limitsp13.getAllowedArea()).intersection(JTS.to(limitsp14.getAllowedArea()));
        testArea.normalize();
        assertNull(accessInfo.getClipArea());
        assertNotNull(accessInfo.getArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.getArea());
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

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        Rule p15 = insert(15, null, "group22", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp15 = setRuleLimits(p15, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p16 = insert(16, null, "group23", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp16 = setRuleLimits(p16, WKT_WGS84_3, CLIP, HIDE);

        AccessRequest request =
                createRequest("auth22", "group22", "group23")
                        .withService("s22")
                        .withRequest("r22")
                        .withWorkspace("w22")
                        .withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // we got a user in two groups one with an intersect spatialFilterType
        // and the other with a clip spatialFilterType. The two area should haven
        // been kept separated
        assertNotNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry intersects = JTS.to(accessInfo.getArea());
        intersects.normalize();
        assertTrue(intersects.equalsExact(JTS.to(lp15.getAllowedArea()), 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
        clip.normalize();
        org.locationtech.jts.geom.MultiPolygon area2Jts = JTS.to(lp16.getAllowedArea());
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

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        Rule p17 = insert(17, null, "group31", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp17 = setRuleLimits(p17, WKT_WGS84_1, INTERSECT, HIDE);

        Rule p18 = insert(18, null, "group31", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp18 = setRuleLimits(p18, WKT_WGS84_2, CLIP, HIDE);

        Rule p19 = insert(19, null, "group32", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp19 = setRuleLimits(p19, WKT_WGS84_3, CLIP, HIDE);

        Rule p20 = insert(20, null, "group32", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp20 = setRuleLimits(p20, WKT_WGS84_4, CLIP, HIDE);

        AccessRequest request =
                createRequest("auth33", "group31", "group32")
                        .withService("s22")
                        .withRequest("r22")
                        .withWorkspace("w22")
                        .withLayer("l22");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // we should have only the clip geometry
        assertNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedResult =
                JTS.to(lp17.getAllowedArea())
                        .intersection(JTS.to(lp18.getAllowedArea()))
                        .union(
                                JTS.to(lp19.getAllowedArea())
                                        .intersection(JTS.to(lp20.getAllowedArea())));
        expectedResult.normalize();
        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
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

        insert(999, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        Rule p21 = insert(21, null, "group41", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp21 = setRuleLimits(p21, WKT_WGS84_1, CLIP, HIDE);

        Rule p22 = insert(22, null, "group41", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp22 = setRuleLimits(p22, WKT_WGS84_2, CLIP, HIDE);

        Rule p23 = insert(23, null, "group42", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp23 = setRuleLimits(p23, WKT_WGS84_3, INTERSECT, HIDE);

        Rule p24 = insert(24, null, "group42", null, "s22", "r22", null, "w22", "l22", LIMIT);
        RuleLimits lp24 = setRuleLimits(p24, WKT_WGS84_4, INTERSECT, HIDE);

        AccessRequest request =
                createRequest("auth44", "group41", "group42")
                        .withService("s22")
                        .withRequest("r22")
                        .withWorkspace("w22")
                        .withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.getGrant()).isEqualTo(ALLOW);
        // we should have both
        assertThat(accessInfo.getArea()).isNotNull();
        assertThat(accessInfo.getClipArea()).isNotNull();

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedIntersects =
                JTS.to(lp23.getAllowedArea()).intersection(JTS.to(lp24.getAllowedArea()));
        expectedIntersects.normalize();
        org.locationtech.jts.geom.Geometry intersects = JTS.to(accessInfo.getArea());
        intersects.normalize();

        assertTrue(expectedIntersects.equalsExact(intersects, 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
        clip.normalize();
        org.locationtech.jts.geom.Geometry expectedClip =
                JTS.to(lp21.getAllowedArea()).intersection(JTS.to(lp22.getAllowedArea()));
        expectedClip.normalize();
        assertTrue(expectedClip.equalsExact(clip, 10.0E-15));
    }
}
