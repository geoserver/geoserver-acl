/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.model.rules.GrantType.ALLOW;
import static org.geoserver.acl.model.rules.GrantType.LIMIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.jts.JTS;
import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.model.authorization.AccessInfo;
import org.geoserver.acl.model.authorization.AccessRequest;
import org.geoserver.acl.model.authorization.AuthorizationService;
import org.geoserver.acl.model.rules.CatalogMode;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleLimits;
import org.geoserver.acl.model.rules.SpatialFilterType;
import org.geoserver.acl.rules.MemoryRuleRepository;
import org.geoserver.acl.rules.RuleAdminService;
import org.junit.jupiter.api.Test;

import java.util.Set;

/**
 * {@link AuthorizationService} integration/conformance test working with geometries
 *
 * <p>Concrete implementations must supply the required services in {@link ServiceTestBase}
 */
public class AuthorizationServiceImpl_GeomTest extends ServiceTestBase {

    @Override
    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminService(new MemoryRuleRepository());
    }

    @Override
    protected AdminRuleAdminService getAdminRuleAdminService() {
        return new AdminRuleAdminService(new MemoryAdminRuleRepository());
    }

    @Override
    protected AuthorizationService getAuthorizationService() {
        return new AuthorizationServiceImpl(super.adminruleAdminService, super.ruleAdminService);
    }

    @Test
    public void testRuleLimitsAllowedAreaSRIDIsPreserved() {
        // test that the original SRID is present in the allowedArea wkt representation,
        // when retrieving it from the AccessInfo object
        String id;
        {
            Rule r1 = insert(10, null, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
            id = r1.getId();
        }

        {
            insert(11, null, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);
        }

        // save limits and check it has been saved
        {
            String wkt =
                    "SRID=3857;MULTIPOLYGON(((0.0016139656066815888 -0.0006386457758059581,0.0019599705696027314 -0.0006386457758059581,0.0019599705696027314 -0.0008854090051601674,0.0016139656066815888 -0.0008854090051601674,0.0016139656066815888 -0.0006386457758059581)))";

            MultiPolygon<?> allowedArea = (MultiPolygon<?>) Wkt.fromWkt(wkt);
            RuleLimits limits = RuleLimits.builder().allowedArea(allowedArea).build();
            ruleAdminService.setLimits(id, limits);
        }

        {
            //            RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
            //            filter.setWorkspace("w1");
            //            filter.setService("s1");
            //            filter.setRequest("r1");
            //            filter.setLayer("l1");

            AccessRequest request =
                    AccessRequest.builder()
                            .user(null)
                            .roles(Set.of())
                            .service("s1")
                            .request("r1")
                            .workspace("w1")
                            .layer("l1")
                            .build();
            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            Geometry<?> area = accessInfo.getArea();
            assertEquals(3857, area.getCoordinateReferenceSystem().getCrsId().getCode());
        }
    }

    @Test
    public void testRuleLimitsAllowedAreaReprojectionWithDifferentSrid() {
        // test that the original SRID is present in the allowedArea wkt representation,
        // when retrieving it from the AccessInfo object
        String id2;
        String id3;
        {
            insert(999, null, null, null, null, "s1", "r1", null, "w1", "l1", ALLOW);
        }

        {
            Rule r2 = insert(11, null, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
            id2 = r2.getId();
        }

        // save limits and check it has been saved
        {
            String wkt =
                    "SRID=3003;MultiPolygon (((1680529.71478682174347341 4849746.00902365241199732, 1682436.7076464940328151 4849731.7422441728413105, 1682446.21883281995542347 4849208.62699576932936907, 1680524.95919364970177412 4849279.96089325752109289, 1680529.71478682174347341 4849746.00902365241199732)))";

            MultiPolygon<?> allowedArea = (MultiPolygon<?>) Wkt.fromWkt(wkt);
            RuleLimits limits = RuleLimits.builder().allowedArea(allowedArea).build();
            ruleAdminService.setLimits(id2, limits);
        }

        {
            Rule r3 = insert(12, null, null, null, null, "s1", "r1", null, "w1", "l1", LIMIT);
            id3 = r3.getId();
        }

        // save limits and check it has been saved
        {
            String wkt =
                    "SRID=23032;MultiPolygon (((680588.67850254673976451 4850060.34823693986982107, 681482.71827003755606711 4850469.32878803834319115, 682633.56349697941914201 4849499.20374245755374432, 680588.67850254673976451 4850060.34823693986982107)))";

            MultiPolygon<?> allowedArea = (MultiPolygon<?>) Wkt.fromWkt(wkt);
            RuleLimits limits = RuleLimits.builder().allowedArea(allowedArea).build();
            ruleAdminService.setLimits(id3, limits);
        }

        {
            //            RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
            //            filter.setWorkspace("w1");
            //            filter.setService("s1");
            //            filter.setRequest("r1");
            //            filter.setLayer("l1");
            //            AccessRequest request = AccessRequest.builder().filter(filter).build();
            AccessRequest request =
                    AccessRequest.builder()
                            .user(null)
                            .roles(Set.of())
                            .service("s1")
                            .request("r1")
                            .workspace("w1")
                            .layer("l1")
                            .build();

            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            Geometry<?> area = accessInfo.getArea();
            assertEquals(3003, area.getCoordinateReferenceSystem().getCrsId().getCode());
        }
    }

    @Test
    public void testRuleSpatialFilterTypeClipSameGroup() {

        // test that when we have two rules referring to the same group
        // one having a filter type Intersects and the other one having filter type Clip
        // the result is a clip area obtained by the intersection of the two.
        final AccessRequest user = createRequest("auth11", "group11", "group12");

        insert(9999, null, null, null, null, "s11", "r11", null, "w11", "l11", ALLOW);
        String id1 =
                insert(
                                10,
                                user.getUser(),
                                "group11",
                                null,
                                null,
                                "s11",
                                "r11",
                                null,
                                "w11",
                                "l11",
                                LIMIT)
                        .getId();

        String areaWKT =
                "MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
        MultiPolygon<?> area = (MultiPolygon<?>) Wkt.fromWkt(areaWKT);
        RuleLimits limits =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area)
                        .build();

        ruleAdminService.setLimits(id1, limits);

        String id2 =
                insert(
                                11,
                                user.getUser(),
                                "group12",
                                null,
                                null,
                                "s11",
                                "r11",
                                null,
                                "w11",
                                "l11",
                                LIMIT)
                        .getId();
        String areaWKT2 =
                "MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
        MultiPolygon<?> area2 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT2);

        RuleLimits limits2 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.INTERSECT)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area2)
                        .build();
        ruleAdminService.setLimits(id2, limits2);
        //        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
        //        filter.setWorkspace("w11");
        //        filter.setLayer("l11");
        //
        // AccessRequest request = AccessRequest.builder().user(user).filter(filter).build();
        AccessRequest request = AccessRequest.builder().workspace("w11").layer("l11").build();
        //        request = user.withWorkspace("w11").withLayer("l11");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // area in same group, the result should an itersection of the
        // two allowed area as a clip geometry.
        org.locationtech.jts.geom.Geometry testArea = JTS.to(area).intersection(JTS.to(area2));
        testArea.normalize();
        assertNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.getClipArea());
        resultArea.normalize();
        assertTrue(testArea.equalsExact(resultArea, 10.0E-15));
    }

    @Test
    public void testRuleSpatialFilterTypeIntersectsSameGroup() {

        // test that when we have two rules referring to the same group
        // both having a filter type Intersects
        // the result is an intersect area obtained by the intersection of the two.
        final AccessRequest user = createRequest("auth12", "group13", "group14");

        insert(9999, null, null, null, null, "s11", "r11", null, "w11", "l11", ALLOW);
        String id =
                insert(
                                13,
                                user.getUser(),
                                "group13",
                                null,
                                null,
                                "s11",
                                "r11",
                                null,
                                "w11",
                                "l11",
                                LIMIT)
                        .getId();
        String areaWKT =
                "MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
        MultiPolygon<?> area = (MultiPolygon<?>) Wkt.fromWkt(areaWKT);

        RuleLimits limits =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.INTERSECT)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area)
                        .build();

        ruleAdminService.setLimits(id, limits);

        String id2 =
                insert(
                                14,
                                user.getUser(),
                                "group14",
                                null,
                                null,
                                "s11",
                                "r11",
                                null,
                                "w11",
                                "l11",
                                LIMIT)
                        .getId();
        String areaWKT2 =
                "MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
        MultiPolygon<?> area2 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT2);

        RuleLimits limits2 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.INTERSECT)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area2)
                        .build();

        ruleAdminService.setLimits(id2, limits2);

        AccessRequest request = AccessRequest.builder().workspace("w11").layer("l11").build();
        //        request = user.withWorkspace("w11").withLayer("l11");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // area in same group, the result should be the
        // two allowed area as an intersects geometry.
        org.locationtech.jts.geom.Geometry testArea = JTS.to(area).intersection(JTS.to(area2));
        testArea.normalize();
        assertNull(accessInfo.getClipArea());
        assertNotNull(accessInfo.getArea());

        org.locationtech.jts.geom.Geometry resultArea = JTS.to(accessInfo.getArea());
        resultArea.normalize();
        assertTrue(testArea.equalsExact(resultArea, 10.0E-15));
    }

    @Test
    public void testRuleSpatialFilterTypeEnlargeAccess() {
        // test the access enalargement behaviour with the SpatialFilterType.
        // the user belongs to two groups. One with an allowedArea of type intersects,
        // the other one with an allowed area of type clip. They should be returned
        // separately in the final rule.
        final AccessRequest user = createRequest("auth22", "group22", "group23");

        insert(999, null, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        String id =
                insert(15, null, "group22", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT =
                "MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
        MultiPolygon<?> area = (MultiPolygon<?>) Wkt.fromWkt(areaWKT);
        RuleLimits limits =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.INTERSECT)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area)
                        .build();
        ruleAdminService.setLimits(id, limits);

        String id2 =
                insert(16, null, "group23", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT2 =
                "MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
        MultiPolygon<?> area2 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT2);
        RuleLimits limits2 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area2)
                        .build();
        ruleAdminService.setLimits(id2, limits2);

        //        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
        //        filter.setWorkspace("w22");
        //        filter.setLayer("l22");
        //        filter.setUser(user.getName());
        //        AccessRequest request = AccessRequest.builder().user(user).filter(filter).build();
        AccessRequest request = user.withWorkspace("w22").withLayer("l22");

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
        assertTrue(intersects.equalsExact(JTS.to(area), 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
        clip.normalize();
        org.locationtech.jts.geom.MultiPolygon area2Jts = JTS.to(area2);
        area2Jts.normalize();
        assertTrue(clip.equalsExact(area2Jts, 10.0E-15));
    }

    @Test
    public void testRuleSpatialFilterTypeFourRules() {
        // the user belongs to two groups and there are two rules for each group:
        // INTERSECTS and CLIP for the first, and CLIP CLIP for the second.
        // The expected result is only one allowedArea of type clip
        // obtained by the intersection of the firs two, united with the intersection of
        // the second
        // two.
        // the first INTERSECTS is resolve as CLIP because during constraint resolution
        // the more
        // restrictive
        // type is chosen.

        final AccessRequest user = createRequest("auth33", "group31", "group32");

        insert(999, null, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        String id =
                insert(17, null, "group31", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT =
                "SRID=4326;MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
        MultiPolygon<?> area = (MultiPolygon<?>) Wkt.fromWkt(areaWKT);
        RuleLimits limits =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.INTERSECT)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area)
                        .build();

        ruleAdminService.setLimits(id, limits);
        assertThat(ruleAdminService.get(id).get().getRuleLimits()).isEqualTo(limits);

        String id2 =
                insert(18, null, "group31", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT2 =
                "SRID=4326;MultiPolygon (((-1.46109090909091011 5.68500000000000139, -0.68600000000000083 5.7651818181818193, -0.73945454545454625 2.00554545454545519, -1.54127272727272846 1.9610000000000003, -1.46109090909091011 5.68500000000000139)))";
        MultiPolygon<?> area2 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT2);
        RuleLimits limits2 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area2)
                        .build();
        ruleAdminService.setLimits(id2, limits2);
        assertThat(ruleAdminService.get(id2).get().getRuleLimits()).isEqualTo(limits2);

        String id3 =
                insert(19, null, "group32", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT3 =
                "SRID=4326;MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
        MultiPolygon<?> area3 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT3);
        RuleLimits limits3 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area3)
                        .build();

        ruleAdminService.setLimits(id3, limits3);
        assertThat(ruleAdminService.get(id3).get().getRuleLimits()).isEqualTo(limits3);

        String id4 =
                insert(20, null, "group32", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT4 =
                "SRID=4326;MultiPolygon (((-1.30963636363636482 5.96118181818181991, 1.78181818181818175 4.84754545454545571, -0.90872727272727349 2.26390909090909132, -1.30963636363636482 5.96118181818181991)))";
        MultiPolygon<?> area4 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT4);
        RuleLimits limits4 =
                RuleLimits.builder()
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .catalogMode(CatalogMode.HIDE)
                        .allowedArea(area4)
                        .build();
        ruleAdminService.setLimits(id4, limits4);

        //        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
        //        filter.setWorkspace("w22");
        //        filter.setLayer("l22");
        //        filter.setUser(user.getName());
        //        AccessRequest request = AccessRequest.builder().user(user).filter(filter).build();
        AccessRequest request = user.withWorkspace("w22").withLayer("l22");
        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // we should have only the clip geometry
        assertNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedResult =
                JTS.to(area)
                        .intersection(JTS.to(area2))
                        .union(JTS.to(area3).intersection(JTS.to(area4)));
        expectedResult.normalize();
        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
        clip.normalize();
        assertTrue(clip.equalsExact(expectedResult, 10.0E-15));
    }

    @Test
    public void testRuleSpatialFilterTypeFourRules2() {
        // the user belongs to two groups and there are two rules for each group:
        // CLIP and CLIP for the first, and INTERSECTS INTERSECTS for the second.
        // The expected result are two allowedArea the first of type clip and second of
        // type
        // intersects.

        final AccessRequest user = createRequest("auth44", "group41", "group42");

        insert(999, null, null, null, null, "s22", "r22", null, "w22", "l22", ALLOW);

        String id =
                insert(21, null, "group41", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT =
                "MultiPolygon (((-1.93327272727272859 5.5959090909090925, 2.22727272727272707 5.67609090909091041, 2.00454545454545441 4.07245454545454599, -1.92436363636363761 4.54463636363636425, -1.92436363636363761 4.54463636363636425, -1.93327272727272859 5.5959090909090925)))";
        MultiPolygon<?> area = (MultiPolygon<?>) Wkt.fromWkt(areaWKT);
        RuleLimits limits =
                RuleLimits.clip().withCatalogMode(CatalogMode.HIDE).withAllowedArea(area);
        ruleAdminService.setLimits(id, limits);

        String id2 =
                insert(22, null, "group41", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT2 =
                "MultiPolygon (((-1.46109090909091011 5.68500000000000139, -0.68600000000000083 5.7651818181818193, -0.73945454545454625 2.00554545454545519, -1.54127272727272846 1.9610000000000003, -1.46109090909091011 5.68500000000000139)))";
        MultiPolygon<?> area2 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT2);
        RuleLimits limits2 =
                RuleLimits.clip().withCatalogMode(CatalogMode.HIDE).withAllowedArea(area2);
        ruleAdminService.setLimits(id2, limits2);

        String id3 =
                insert(23, null, "group42", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT3 =
                "MultiPolygon (((-1.78181818181818308 5.95227272727272894, -0.16927272727272813 5.4711818181818197, 1.97781818181818148 3.81409090909090986, 1.93327272727272748 2.05009090909090919, -2.6638181818181832 2.64700000000000069, -1.78181818181818308 5.95227272727272894)))";
        MultiPolygon<?> area3 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT3);
        RuleLimits limits3 =
                RuleLimits.intersect().withCatalogMode(CatalogMode.HIDE).withAllowedArea(area3);
        ruleAdminService.setLimits(id3, limits3);

        String id4 =
                insert(24, null, "group42", null, null, "s22", "r22", null, "w22", "l22", LIMIT)
                        .getId();
        String areaWKT4 =
                "MultiPolygon (((-1.30963636363636482 5.96118181818181991, 1.78181818181818175 4.84754545454545571, -0.90872727272727349 2.26390909090909132, -1.30963636363636482 5.96118181818181991)))";
        MultiPolygon<?> area4 = (MultiPolygon<?>) Wkt.fromWkt(areaWKT4);
        RuleLimits limits4 =
                RuleLimits.intersect().withCatalogMode(CatalogMode.HIDE).withAllowedArea(area4);
        ruleAdminService.setLimits(id4, limits4);

        //        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
        //        filter.setWorkspace("w22");
        //        filter.setLayer("l22");
        //        filter.setUser(user.getName());
        //        AccessRequest request = AccessRequest.builder().user(user).filter(filter).build();
        AccessRequest request = user.withWorkspace("w22").withLayer("l22");

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.getGrant());

        // we should have both
        assertNotNull(accessInfo.getArea());
        assertNotNull(accessInfo.getClipArea());

        // the intersects should be equal to the originally defined
        // allowed area
        org.locationtech.jts.geom.Geometry expectedIntersects =
                JTS.to(area3).intersection(JTS.to(area4));
        expectedIntersects.normalize();
        org.locationtech.jts.geom.Geometry intersects = JTS.to(accessInfo.getArea());
        intersects.normalize();
        System.out.println(intersects.toString());
        System.out.println(expectedIntersects.toString());
        assertTrue(expectedIntersects.equalsExact(intersects, 10.0E-15));

        org.locationtech.jts.geom.Geometry clip = JTS.to(accessInfo.getClipArea());
        clip.normalize();
        org.locationtech.jts.geom.Geometry expectedClip = JTS.to(area2).intersection(JTS.to(area));
        expectedClip.normalize();
        assertTrue(expectedClip.equalsExact(clip, 10.0E-15));
    }
}
