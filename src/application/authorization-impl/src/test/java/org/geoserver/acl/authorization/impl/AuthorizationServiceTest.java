/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization.impl;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.NONE;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READONLY;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READWRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.junit.jupiter.api.Test;

/**
 * {@link AuthorizationService} integration/conformance test
 *
 * <p>Concrete implementations must supply the required services in {@link
 * BaseAuthorizationServiceTest}
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@SuppressWarnings("unused")
public abstract class AuthorizationServiceTest extends BaseAuthorizationServiceTest {

    @Override
    protected abstract RuleAdminService getRuleAdminService();

    @Override
    protected abstract AdminRuleAdminService getAdminRuleAdminService();

    @Override
    protected abstract AuthorizationService getAuthorizationService();

    @Test
    public void testGetRulesForUsersAndGroup() {

        assertEquals(0, ruleAdminService.count(RuleFilter.any()));

        final AccessRequest u1 = createRequest("TestUser1", "p1")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1");

        final AccessRequest u2 = createRequest("TestUser2", "p2")
                .withService("s1")
                .withRequest("r2")
                .withWorkspace("w2")
                .withLayer("l2");

        final AccessRequest u3 = createRequest("TestUser3", "g3a", "g3b");

        Rule p10 = insert(10, u1.user(), "p1", null, "s1", "r1", null, "w1", "l1", ALLOW);
        Rule p20 = insert(20, u2.user(), "p2", null, "s1", "r2", null, "w2", "l2", ALLOW);
        Rule p30 = insert(30, u1.user(), "p1", null, "s3", null, null, "w3", null, ALLOW);
        Rule p40 = insert(40, null, "p1", null, null, null, null, null, null, ALLOW);
        Rule p50 = insert(50, null, "g3a", null, null, null, null, null, null, ALLOW);
        Rule p60 = insert(60, null, "g3b", null, null, null, null, null, null, ALLOW);

        assertThat(getMatchingRules(u1)).isEqualTo(of(p10, p40));
        assertThat(getMatchingRules(u2)).isEqualTo(of(p20));

        assertThat(getMatchingRules(u1.withRoles(Set.of("Z")))).isEmpty();
        assertThat(getMatchingRules(u1.withUser(null)))
                .as("only group rule should match")
                .isEqualTo(of(p40));
        assertThat(getMatchingRules(u1.withService("s3").withWorkspace("w3"))).isEqualTo(of(p30, p40));
        assertThat(getMatchingRules(u1.withService("s2")))
                .as("service mismatch")
                .isEqualTo(of(p40));

        assertThat(getMatchingRules(u1.withRoles(Set.of("p1", "g3a")))).isEqualTo(of(p10, p40, p50));
        assertThat(getMatchingRules(u2.withRoles(Set.of("p2", "g3b")))).isEqualTo(of(p20, p60));
    }

    @Test
    public void testGetRulesForGroupOnly() {

        assertEquals(0, ruleAdminService.count(RuleFilter.any()));

        Rule p10 = insert(10, null, "p1", null, "s1", "r1", null, "w1", "l1", ALLOW);
        Rule p20 = insert(20, null, "p2", null, "s1", "r1", null, "w1", "l1", ALLOW);
        Rule p30 = insert(30, null, "p1", null, "s3", null, null, null, null, ALLOW);
        Rule p40 = insert(40, null, "p1", null, null, null, null, null, null, ALLOW);
        Rule p50 = insert(50, null, "p2", null, null, null, null, null, null, ALLOW);

        AccessRequest u1 = createRequest("u1", "p1");
        AccessRequest u1s1 =
                u1.withService("s1").withRequest("r1").withWorkspace("w1").withLayer("l1");
        AccessRequest u2p1p2 = createRequest("u2", "p1", "p2");
        AccessRequest u2p1p2s1 =
                u2p1p2.withService("s1").withRequest("r1").withWorkspace("w1").withLayer("l1");

        assertThat(getMatchingRules(u1)).isEqualTo(of(p40));
        assertThat(getMatchingRules(u1s1)).isEqualTo(of(p10, p40));
        assertThat(getMatchingRules(u2p1p2)).isEqualTo(of(p40, p50));
        assertThat(getMatchingRules(u2p1p2.withService("s3").withWorkspace("wxx")))
                .isEqualTo(of(p30, p40, p50));
        assertThat(getMatchingRules(u2p1p2s1)).isEqualTo(of(p10, p20, p40, p50));

        assertThat(getMatchingRules(u1s1.withRoles(Set.of()))).isEmpty();
    }

    @Test
    public void testGetInfo() {
        assertEquals(0, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        List<Rule> rules = new ArrayList<>();

        rules.add(insert(Rule.allow().withPriority(100 + rules.size()).withService("WCS")));
        rules.add(insert(Rule.allow()
                .withPriority(100 + rules.size())
                .withService("s1")
                .withRequest("r2")
                .withWorkspace("w2")
                .withLayer("l2")));
        rules.add(insert(Rule.allow()
                .withPriority(100 + rules.size())
                .withService("s3")
                .withRequest("r3")
                .withWorkspace("w3")
                .withLayer("l3")));
        rules.add(insert(Rule.deny().withPriority(100 + rules.size())));

        assertEquals(4, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        AccessRequest req = createRequest("u0", "p0")
                .withService("WCS")
                .withRequest(null)
                .withWorkspace("W0")
                .withLayer("l0");

        {
            assertEquals(
                    2, authorizationService.getMatchingRules(req.withUser(null)).size());
            assertEquals(
                    ALLOW,
                    authorizationService.getAccessInfo(req.withUser(null)).grant());
        }
        {
            assertEquals(
                    2,
                    authorizationService
                            .getMatchingRules(req.withRoles(Set.of()))
                            .size());
            assertEquals(
                    ALLOW,
                    authorizationService.getAccessInfo(req.withRoles(Set.of())).grant());
        }
        {
            AccessRequest unmatch = req.withUser(null).withService("UNMATCH");
            assertEquals(1, authorizationService.getMatchingRules(unmatch).size());
            assertEquals(DENY, authorizationService.getAccessInfo(unmatch).grant());
        }
        {
            AccessRequest unmatch = req.withRoles(Set.of()).withService("UNMATCH");
            assertEquals(1, authorizationService.getMatchingRules(unmatch).size());
            assertEquals(DENY, authorizationService.getAccessInfo(unmatch).grant());
        }
    }

    @Test
    public void testResolveLazy() {
        assertEquals(0, ruleAdminService.count());

        Rule r1 = insert(Rule.allow().withPriority(100).withService("WCS"));
        Rule r2 = insert(Rule.allow().withPriority(101).withService("s1").withLayer("l2"));
        setLayerDetails(r2, Set.of(), Set.of());

        assertEquals(2, ruleAdminService.count());

        final AccessRequest req =
                AccessRequest.builder().service("s1").layer("l2").build();
        List<Rule> matchingRules = getMatchingRules(req);
        assertThat(matchingRules).isEqualTo(of(r2));

        AccessInfo accessInfo = getAccessInfo(req);
        assertEquals(ALLOW, accessInfo.grant());
        assertNull(accessInfo.intersectArea());
    }

    @Test
    public void testNoDefault() {

        assertEquals(0, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        insert(Rule.allow().withService("WCS"));

        assertEquals(
                1, getMatchingRules("u0", null, null, "WCS", null, "W0", "l0").size());
        assertEquals(
                ALLOW, getAccessInfo("u0", null, null, "WCS", null, "W0", "l0").grant());

        assertEquals(
                1, getMatchingRules(null, "p0", null, "WCS", null, "W0", "l0").size());
        assertEquals(
                ALLOW, getAccessInfo(null, "p0", null, "WCS", null, "W0", "l0").grant());

        assertEquals(
                0,
                getMatchingRules("u0", null, null, "UNMATCH", null, "W0", "l0").size());
        assertEquals(
                DENY,
                getAccessInfo("u0", null, null, "UNMATCH", null, "W0", "l0").grant());

        assertEquals(
                0,
                getMatchingRules(null, "p0", null, "UNMATCH", null, "W0", "l0").size());
        assertEquals(
                DENY,
                getAccessInfo(null, "p0", null, "UNMATCH", null, "W0", "l0").grant());
    }

    @Test
    public void testGroups() {
        assertEquals(0, ruleAdminService.count());

        Rule r1 = insert(Rule.allow()
                .withPriority(10)
                .withRolename("p1")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1"));
        Rule r2 = insert(Rule.deny().withPriority(11).withRolename("p1"));
        assertEquals(2, ruleAdminService.count());

        final AccessRequest req1 = createRequest("u1", "p1")
                .withService("s1")
                .withRequest("r1")
                .withWorkspace("w1")
                .withLayer("l1");
        final AccessRequest req2 = createRequest("u2", "p2");

        assertThat(getMatchingRules(req1)).isEqualTo(of(r1, r2));
        assertThat(getAccessInfo(req1).grant()).isEqualByComparingTo(ALLOW);

        assertThat(getMatchingRules(req1.withService("s2"))).isEqualTo(of(r2));
        assertThat(getAccessInfo(req1.withService("s2")).grant()).isEqualByComparingTo(DENY);

        assertThat(getMatchingRules(req2)).isEmpty();
        assertThat(getAccessInfo(req2).grant()).isEqualByComparingTo(DENY);
    }

    @Test
    public void testGroupOrder01() {
        assertEquals(0, ruleAdminService.count());

        final AccessRequest req1 = createRequest("u1", "p1");
        final AccessRequest req2 = createRequest("u2", "p2");

        List<Rule> rules = new ArrayList<Rule>();
        rules.add(insert(Rule.allow().withPriority(10 + rules.size()).withRolename("p1")));
        rules.add(insert(Rule.deny().withPriority(10 + rules.size()).withRolename("p2")));

        assertEquals(rules.size(), ruleAdminService.count());

        assertEquals(1, authorizationService.getMatchingRules(req1).size());
        assertEquals(1, authorizationService.getMatchingRules(req2).size());

        assertEquals(ALLOW, authorizationService.getAccessInfo(req1).grant());
        assertEquals(DENY, authorizationService.getAccessInfo(req2).grant());
    }

    @Test
    public void testGroupOrder02() {
        assertEquals(0, ruleAdminService.count());

        final AccessRequest req1 = createRequest("u1", "p1");
        final AccessRequest req2 = createRequest("u2", "p2");

        List<Rule> rules = new ArrayList<Rule>();
        rules.add(insert(Rule.deny().withPriority(10 + rules.size()).withRolename("p2")));
        rules.add(insert(Rule.allow().withPriority(10 + rules.size()).withRolename("p1")));

        assertEquals(rules.size(), ruleAdminService.count());

        assertEquals(1, authorizationService.getMatchingRules(req1).size());
        assertEquals(1, authorizationService.getMatchingRules(req2).size());

        assertEquals(ALLOW, authorizationService.getAccessInfo(req1).grant());
        assertEquals(DENY, authorizationService.getAccessInfo(req2).grant());
    }

    @Test
    public void testAttrib() {
        assertEquals(0, ruleAdminService.count());
        final AccessRequest g1 = createRequest("u1", "g1").withLayer("l1");
        final AccessRequest g2 = createRequest("u2", "g2").withLayer("l1");
        final AccessRequest g12 = createRequest("u12", "g1", "g2").withLayer("l1");
        final AccessRequest g13 = createRequest("u13", "g1", "g3").withLayer("l1");

        Rule r1 = insert(Rule.allow().withRolename("g1").withLayer("l1"));
        Set<String> r1Styles = Set.of("style01", "style02");
        Set<LayerAttribute> r1Atts = Set.of(attrib("att1", NONE), attrib("att2", READONLY), attrib("att3", READWRITE));
        setLayerDetails(r1, r1Styles, r1Atts);

        Rule r2 = insert(Rule.allow().withRolename("g2").withLayer("l1"));
        Set<String> r2Styles = Set.of("style01", "style03");
        Set<LayerAttribute> r2Atts = Set.of(attrib("att1", READONLY), attrib("att2", READWRITE), attrib("att3", NONE));
        setLayerDetails(r2, r2Styles, r2Atts);

        Rule r3 = insert(Rule.allow().withRolename("g3").withLayer("l1"));
        setLayerDetails(r3, Set.of(), Set.of());

        Rule r4 = insert(Rule.deny().withRolename("g4").withLayer("l1"));
        assertEquals(4, ruleAdminService.count());

        AccessInfo accessInfo;
        accessInfo = getAccessInfo(g1);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r1.id()));
        assertThat(accessInfo.attributes()).isEqualTo(r1Atts);
        assertThat(accessInfo.allowedStyles()).isEqualTo(r1Styles);

        accessInfo = getAccessInfo(g2);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r2.id()));
        assertThat(accessInfo.attributes()).isEqualTo(r2Atts);
        assertThat(accessInfo.allowedStyles()).isEqualTo(r2Styles);

        accessInfo = getAccessInfo(g12);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r1.id(), r2.id()));
        Set<LayerAttribute> expected =
                Set.of(attrib("att1", READONLY), attrib("att2", READWRITE), attrib("att3", READWRITE));
        assertThat(accessInfo.attributes()).isEqualTo(expected);
        assertThat(accessInfo.allowedStyles()).isEqualTo(Set.of("style01", "style02", "style03"));

        accessInfo = getAccessInfo(g13);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r1.id(), r3.id()));
        assertThat(accessInfo.attributes()).isEmpty();
        assertThat(accessInfo.allowedStyles()).isEmpty();
    }

    /**
     * Each of the two roles contributes attribute names that the other does not, plus one shared
     * name. Exercises the single-side branches in {@code unionAttributes} (name only on one side
     * is carried through unchanged) alongside the merge branch for the shared name.
     */
    @Test
    public void testAttrib_unionAcrossRolesWithUniqueAndSharedNames() {
        assertEquals(0, ruleAdminService.count());

        Rule r1 = insert(Rule.allow().withRolename("g1").withLayer("l1"));
        Set<LayerAttribute> r1Atts = Set.of(attrib("a_only", READONLY), attrib("shared", READONLY));
        setLayerDetails(r1, Set.of(), r1Atts);

        Rule r2 = insert(Rule.allow().withRolename("g2").withLayer("l1"));
        Set<LayerAttribute> r2Atts = Set.of(attrib("b_only", READWRITE), attrib("shared", NONE));
        setLayerDetails(r2, Set.of(), r2Atts);

        AccessRequest request = createRequest("u12", "g1", "g2").withLayer("l1");
        AccessInfo accessInfo = getAccessInfo(request);

        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r1.id(), r2.id()));
        // a_only carried from r1, b_only from r2, shared merged via lenient (READONLY + NONE = READONLY)
        Set<LayerAttribute> expected =
                Set.of(attrib("a_only", READONLY), attrib("b_only", READWRITE), attrib("shared", READONLY));
        assertThat(accessInfo.attributes()).isEqualTo(expected);
    }

    private AccessInfo getAccessInfo(AccessRequest request) {
        return authorizationService.getAccessInfo(request);
    }

    private LayerAttribute attrib(String name, LayerAttribute.AccessType access) {
        return LayerAttribute.builder()
                .access(access)
                .name(name)
                .dataType("String")
                .build();
    }

    private void setLayerDetails(Rule rule, Set<String> allowedStyles, Set<LayerAttribute> attributes) {
        LayerDetails d1 = LayerDetails.builder()
                .allowedStyles(allowedStyles)
                .attributes(attributes)
                .build();
        ruleAdminService.setLayerDetails(rule.id(), d1);
    }

    /** Added for issue #23 */
    @Test
    public void testGetAccessInfo_EmptyAllowableStyles() {
        assertEquals(0, ruleAdminService.count());
        Rule p30 = insert(Rule.allow().withPriority(30).withRolename("g2").withLayer("l1"));
        Rule p40 = insert(Rule.allow().withPriority(40).withRolename("g1").withLayer("l1"));
        {
            LayerDetails d1 = LayerDetails.builder()
                    .allowedStyles(Set.of("style01", "style02"))
                    .build();
            ruleAdminService.setLayerDetails(p40.id(), d1);
        }
        assertEquals(2, ruleAdminService.count());

        final AccessRequest request = createRequest("u1", "g1", "g2").withLayer("l1");
        assertThat(getMatchingRules(request)).isEqualTo(of(p30, p40));

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertEquals(ALLOW, accessInfo.grant());
        assertThat(accessInfo.matchingRules()).isEqualTo(of(p30.id(), p40.id()));

        assertThat(accessInfo.allowedStyles()).isEmpty();
    }

    @Test
    public void testIPAddress() {

        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY);
        assertEquals(0, ruleAdminService.count(filter));

        String ip10 = "10.10.100.0/24";
        String ip192 = "192.168.0.0/16";

        Rule g1Ip10 = Rule.allow().withPriority(10).withRolename("g1").withAddressRange(ip10);
        Rule g2ip10 = Rule.allow().withPriority(20).withRolename("g2").withAddressRange(ip10);
        Rule g1ip192 = Rule.allow().withPriority(30).withRolename("g1").withAddressRange(ip192);
        Rule g1allow = Rule.allow().withPriority(40).withRolename("g1");
        Rule g2deny = Rule.deny().withPriority(50).withRolename("g2");

        g1Ip10 = insert(g1Ip10);
        g2ip10 = insert(g2ip10);
        g1ip192 = insert(g1ip192);
        g1allow = insert(g1allow);
        g2deny = insert(g2deny);

        AccessRequest reqAnonymous = createRequest(null);
        AccessRequest reqG1 = createRequest(null, "g1", "ROLE_1");
        AccessRequest reqG2 = createRequest(null, "g2", "ROLE_2");

        assertEquals(List.of(), getMatchingRules(reqAnonymous));
        assertEquals(List.of(g1allow), getMatchingRules(reqG1));
        assertEquals(List.of(g2deny), getMatchingRules(reqG2));

        assertThat(getMatchingRules(reqAnonymous.withSourceAddress("10.10.100.4")))
                .isEmpty();
        assertThat(getMatchingRules(reqG1.withSourceAddress("10.10.100.4"))).isEqualTo(of(g1Ip10, g1allow));
        assertThat(getMatchingRules(reqG1.withSourceAddress("10.10.1.4")))
                .as("address out of range")
                .isEqualTo(of(g1allow));

        assertThat(getMatchingRules(reqAnonymous.withSourceAddress("192.168.1.1")))
                .isEmpty();

        assertThat(getMatchingRules(reqG1.withSourceAddress("192.168.1.1"))).isEqualTo(of(g1ip192, g1allow));
        assertThat(getMatchingRules(reqG1.withUser("anyuser").withSourceAddress("192.168.1.1")))
                .isEqualTo(of(g1ip192, g1allow));

        assertThat(getMatchingRules(reqG1.withSourceAddress("BADIP"))).isEmpty();
    }

    @Test
    public void testGetRulesForUserOnly() {
        assertEquals(0, ruleAdminService.count());

        insert(10, "u1", null, null, "s1", "r1", null, "w1", "l1", ALLOW);
        insert(20, "u2", null, null, "s2", "r2", null, "w2", "l2", ALLOW);
        insert(30, "u1", null, null, "s3", "r3", null, "w3", "l3", ALLOW);
        insert(40, "u1", null, null, null, null, null, null, null, ALLOW);
        insert(50, "u3a", null, null, null, null, null, null, null, ALLOW);
        insert(60, "u3b", null, null, null, null, null, null, null, ALLOW);

        final AccessRequest u1 = createRequest("u1", "g1");
        assertMatchingRules(u1, 40);
        assertMatchingRules(
                u1.withService("s1").withRequest("r1").withWorkspace("w1").withLayer("l1"), 10, 40);
        assertMatchingRules(
                u1.withService("s2").withRequest("r1").withWorkspace("w1").withLayer("l1"), 40);
        assertMatchingRules(
                u1.withService("s3").withRequest("r3").withWorkspace("w3").withLayer("l3"), 30, 40);

        final AccessRequest u2 = createRequest("u2", "g2");
        assertMatchingRules(u2, new Integer[0]);
        assertMatchingRules(
                u2.withService("s2").withRequest("r2").withWorkspace("w2").withLayer("l2"), 20);

        assertGetMatchingRules("u3a", "g1,g2", 50);
        assertGetMatchingRules("u3b", "g1,g2", 60);

        AccessRequest request =
                AccessRequest.builder().user("anonymous").roles("g2").build();
        assertThat(authorizationService.getMatchingRules(request)).isEmpty();

        request = AccessRequest.builder().user("anonymous").roles("g1").build();
        assertThat(authorizationService.getMatchingRules(request)).isEmpty();
    }

    @Test
    public void testGetAdminAuthorization() {

        final AccessRequest request = createRequest("auth00").withWorkspace("w1");
        final AccessRequest fullRequest =
                request.withService("s1").withRequest("r1").withLayer("l1");
        final AdminAccessRequest adminReq =
                AdminAccessRequest.builder().user("auth00").workspace("w1").build();

        Rule r10 = insert(10, "auth00", null, null, "s1", "r1", null, "w1", "l1", ALLOW);
        Rule r20 = insert(20, "auth00", null, null, null, null, null, "w1", null, ALLOW);

        AccessInfo accessInfo = authorizationService.getAccessInfo(request);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r20.id()));

        accessInfo = authorizationService.getAccessInfo(fullRequest);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r10.id(), r20.id()));

        AdminAccessInfo adminAuth = authorizationService.getAdminAuthorization(adminReq);
        assertThat(adminAuth.admin()).isFalse();

        AdminRule userAdminRule = insert(AdminRule.user().withPriority(20).withUsername(request.user()));

        accessInfo = authorizationService.getAccessInfo(fullRequest);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r10.id(), r20.id()));

        adminAuth = authorizationService.getAdminAuthorization(adminReq);
        assertThat(adminAuth.admin()).isFalse();
        assertThat(adminAuth.matchingAdminRule()).isEqualTo(userAdminRule.id());

        AdminRule adminRule = adminruleAdminService.insert(
                AdminRule.admin().withPriority(10).withUsername(request.user()).withWorkspace(request.workspace()));

        accessInfo = authorizationService.getAccessInfo(fullRequest);
        assertThat(accessInfo.grant()).isEqualTo(ALLOW);
        assertThat(accessInfo.matchingRules()).isEqualTo(of(r10.id(), r20.id()));

        adminAuth = authorizationService.getAdminAuthorization(adminReq);
        assertThat(adminAuth.admin()).isTrue();
        assertThat(adminAuth.matchingAdminRule()).isEqualTo(adminRule.id());
    }

    @Test
    public void testGetMatchingRules_MultiRoles() {
        assertEquals(0, ruleAdminService.count());

        final AccessRequest p1 = createRequest("u1", "p1");
        final AccessRequest p2 = createRequest("u2", "p2");
        final AccessRequest p1p2 = createRequest("u3", "p1", "p2");

        AccessRequest p1Full =
                p1.withService("s1").withRequest("r1").withWorkspace("w1").withLayer("l1");
        AccessRequest p2Full =
                p2.withService("s1").withRequest("r1").withWorkspace("w2").withLayer("l2");
        AccessRequest p1p2s1 = p1p2.withService("s1");

        insert(10, null, "p1", null, "s1", "r1", null, "w1", "l1", ALLOW);
        insert(20, null, "p2", null, "s1", "r1", null, "w2", "l2", ALLOW);
        insert(30, "u1", null, null, null, null, null, null, null, ALLOW);
        insert(40, "u2", null, null, null, null, null, null, null, ALLOW);
        insert(50, "u3", null, null, null, null, null, null, null, ALLOW);
        insert(51, "u3", "p1", null, null, null, null, null, null, ALLOW);
        insert(52, "u3", "p2", null, null, null, null, null, null, ALLOW);
        insert(60, null, "p1", null, null, null, null, null, null, ALLOW);
        insert(70, null, "p2", null, null, null, null, null, null, ALLOW);
        insert(80, null, "p3", null, null, null, null, null, null, ALLOW);
        insert(901, "u1", "p2", null, null, null, null, null, null, ALLOW);
        insert(902, "u2", "p1", null, null, null, null, null, null, ALLOW);
        insert(999, null, null, null, null, null, null, null, null, ALLOW);

        assertGetMatchingRules(null, null, 999);
        assertGetMatchingRules("u1", null, 30, 999);
        assertGetMatchingRules("u1", "p1", 30, 60, 999);
        assertGetMatchingRules(null, "NO", 999);
        assertGetMatchingRules(null, "p1", 60, 999);
        assertGetMatchingRules(null, "p1,NO", 60, 999);
        assertGetMatchingRules(null, "p1,p2", 60, 70, 999);
        assertGetMatchingRules(null, "p1,p2,NO", 60, 70, 999);

        assertMatchingRules(p1Full, 10, 30, 60, 999);
        assertMatchingRules(p2Full, 20, 40, 70, 999);
        assertMatchingRules(p1p2s1, 50, 51, 52, 60, 70, 999);
        AccessRequest p1p2w1l1 = p1p2s1.withRequest("r1").withWorkspace("w1").withLayer("l1");
        assertMatchingRules(p1p2w1l1, 10, 50, 51, 52, 60, 70, 999);
        AccessRequest p1p2w2l2 = p1p2s1.withRequest("r1").withWorkspace("w2").withLayer("l2");
        assertMatchingRules(p1p2w2l2, 20, 50, 51, 52, 60, 70, 999);

        assertGetMatchingRules("NO", null, 999);
        assertGetMatchingRules("NO", null, 999);
        assertGetMatchingRules("NO", "NO", 999);
        assertGetMatchingRules("NO", "p1", 60, 999);
        assertGetMatchingRules("NO", "p1NO", 999);
        assertGetMatchingRules("NO", "p1,p2", 60, 70, 999);
        assertGetMatchingRules("NO", "p1,p2,NO", 60, 70, 999);
    }

    private void assertGetMatchingRules(String userName, String groupNames, Integer... expectedPriorities) {

        String[] groups = groupNames == null ? new String[0] : groupNames.split(",");
        AccessRequest request = createRequest(userName, groups);
        assertMatchingRules(request, expectedPriorities);
    }

    private void assertMatchingRules(AccessRequest request, Integer... expectedPriorities) {
        List<Rule> rules = authorizationService.getMatchingRules(request);

        List<Long> pri = rules.stream().map(Rule::priority).sorted().toList();
        List<Long> exp = Arrays.asList(expectedPriorities).stream()
                .map(Integer::longValue)
                .toList();
        assertEquals(exp, pri, "Bad rule set selected for filter " + request);
    }

    private List<Rule> getMatchingRules(
            String userName,
            String roleName,
            String sourceAddress,
            String service,
            String request,
            String workspace,
            String layer) {

        return getMatchingRules(createRequest(userName, roleName), sourceAddress, service, request, workspace, layer);
    }

    private List<Rule> getMatchingRules(
            AccessRequest baseRequest,
            String sourceAddress,
            String service,
            String request,
            String workspace,
            String layer) {

        AccessRequest req = baseRequest
                .withSourceAddress(validateNotAny(sourceAddress))
                .withService(validateNotAny(service))
                .withRequest(validateNotAny(request))
                .withWorkspace(validateNotAny(workspace))
                .withLayer(validateNotAny(layer));

        return getMatchingRules(req);
    }

    private List<Rule> getMatchingRules(AccessRequest req) {
        return authorizationService.getMatchingRules(req);
    }

    private AccessInfo getAccessInfo(
            String userName,
            String roleName,
            String sourceAddress,
            String service,
            String request,
            String workspace,
            String layer) {

        AccessRequest req = createRequest(userName, roleName)
                .withSourceAddress(sourceAddress)
                .withService(service)
                .withRequest(request)
                .withWorkspace(workspace)
                .withLayer(layer);

        return authorizationService.getAccessInfo(req);
    }
}
