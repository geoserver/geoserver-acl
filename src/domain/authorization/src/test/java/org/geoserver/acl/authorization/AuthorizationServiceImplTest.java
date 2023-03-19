/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.NonNull;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.MemoryAdminRuleRepository;
import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.authorization.AccessInfo;
import org.geoserver.acl.model.authorization.AccessRequest;
import org.geoserver.acl.model.authorization.AuthorizationService;
import org.geoserver.acl.model.authorization.User;
import org.geoserver.acl.model.filter.RuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.filter.predicate.SpecialFilterType;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.IPAddressRange;
import org.geoserver.acl.model.rules.LayerAttribute;
import org.geoserver.acl.model.rules.LayerDetails;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.rules.MemoryRuleRepository;
import org.geoserver.acl.rules.RuleAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link AuthorizationService} integration/conformance test
 *
 * <p>Concrete implementations must supply the required services in {@link ServiceTestBase}
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
public class AuthorizationServiceImplTest extends ServiceTestBase {

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
    public void testGetRulesForUsersAndGroup() {

        assertEquals(0, ruleAdminService.count(RuleFilter.any()));

        final User u1 = createUser("TestUser1", "p1");
        final User u2 = createUser("TestUser2", "p2");
        final User u3 = createUser("TestUser3", "g3a", "g3b");

        insert(
                Rule.allow()
                        .withPriority(10)
                        .withUsername(u1.getName())
                        .withRolename("p1")
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1"));
        insert(
                Rule.allow()
                        .withPriority(20)
                        .withUsername(u2.getName())
                        .withRolename("p2")
                        .withService("s1")
                        .withRequest("r2")
                        .withWorkspace("w2")
                        .withLayer("l2"));
        insert(
                Rule.allow()
                        .withPriority(30)
                        .withUsername(u1.getName())
                        .withRolename("p1")
                        .withService("s3")
                        .withRequest("r3")
                        .withWorkspace("w3")
                        .withLayer("l3"));
        insert(Rule.allow().withPriority(40).withUsername(u1.getName()).withRolename("p1"));
        insert(Rule.allow().withPriority(50).withRolename("g3a"));
        insert(Rule.allow().withPriority(60).withRolename("g3b"));

        assertEquals(
                3, getMatchingRules(u1, u1.getName(), "*", "Z", "*", "*", "*", "*", "*").size());
        assertEquals(3, getMatchingRules(null, "*", "p1", "Z", "*", "*", "*", "*", "*").size());
        assertEquals(
                1,
                getMatchingRules(u1, u1.getName(), "*", "Z", "*", null, null, null, null).size());
        assertEquals(0, getMatchingRules(null, "*", "Z", "Z", "*", null, null, null, null).size());
        assertEquals(
                1,
                getMatchingRules(u1, u1.getName(), "*", "Z", "*", null, null, null, null).size());
        assertEquals(
                1,
                getMatchingRules(u1, u1.getName(), "*", "Z", "*", null, null, null, null).size());
        assertEquals(
                1, getMatchingRules(u2, u2.getName(), "*", "Z", "*", "*", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "p2", "Z", "*", "*", "*", "*", "*").size());
        assertEquals(
                2, getMatchingRules(u1, u1.getName(), "*", "Z", "*", "s1", "*", "*", "*").size());
        assertEquals(2, getMatchingRules(null, "*", "p1", "Z", "*", "s1", "*", "*", "*").size());
        assertEquals(
                2, getMatchingRules(u3, u3.getName(), "*", "Z", "*", "s1", "*", "*", "*").size());
    }

    private static RuleFilter createFilter(String userName, String groupName, String service) {
        RuleFilter filter;
        filter = new RuleFilter(SpecialFilterType.ANY);
        if (userName != null) filter.setUser(userName);
        if (groupName != null) filter.setRole(groupName);
        if (service != null) filter.setService(service);
        return filter;
    }

    @Test
    public void testGetRulesForGroupOnly() {

        assertEquals(0, ruleAdminService.count(RuleFilter.any()));

        Rule r1 =
                Rule.allow()
                        .withPriority(10)
                        .withRolename("p1")
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1");
        Rule r2 =
                Rule.allow()
                        .withPriority(20)
                        .withRolename("p2")
                        .withService("s1")
                        .withRequest("r2")
                        .withWorkspace("w2")
                        .withLayer("l2");
        Rule r3 =
                Rule.allow()
                        .withPriority(30)
                        .withRolename("p1")
                        .withService("s3")
                        .withRequest("r3")
                        .withWorkspace("w3")
                        .withLayer("l3");
        Rule r4 = Rule.allow().withPriority(40).withRolename("p1");

        r1 = insert(r1);
        r2 = insert(r2);
        r3 = insert(r3);
        r4 = insert(r4);

        assertEquals(4, getMatchingRules(null, "*", "*", "*", "*", "*", "*", "*", "*").size());
        assertEquals(3, getMatchingRules(null, "*", "*", "*", "*", "s1", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "*", "*", "*", "ZZ", "*", "*", "*").size());

        assertEquals(3, getMatchingRules(null, "*", "p1", "*", "*", "*", "*", "*", "*").size());
        assertEquals(2, getMatchingRules(null, "*", "p1", "*", "*", "s1", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "p1", "*", "*", "ZZ", "*", "*", "*").size());

        assertEquals(1, getMatchingRules(null, "*", "p2", "*", "*", "*", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "p2", "*", "*", "s1", "*", "*", "*").size());
        assertEquals(0, getMatchingRules(null, "*", "p2", "*", "*", "ZZ", "*", "*", "*").size());

        RuleFilter filter;
        AccessRequest req;

        filter = createFilter(null, "p1", null);
        req = AccessRequest.builder().filter(filter).build();
        assertEquals(3, authorizationService.getMatchingRules(req).size());

        filter = createFilter((String) null, null, "s3");
        req = AccessRequest.builder().filter(filter).build();
        assertEquals(2, authorizationService.getMatchingRules(req).size());
    }

    @Test
    public void testGetInfo() {
        final User user = createUser("u0", "p0");
        assertEquals(0, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        List<Rule> rules = new ArrayList<>();

        rules.add(insert(Rule.allow().withPriority(100 + rules.size()).withService("WCS")));
        rules.add(
                insert(
                        Rule.allow()
                                .withPriority(100 + rules.size())
                                .withService("s1")
                                .withRequest("r2")
                                .withWorkspace("w2")
                                .withLayer("l2")));
        rules.add(
                insert(
                        Rule.allow()
                                .withPriority(100 + rules.size())
                                .withService("s3")
                                .withRequest("r3")
                                .withWorkspace("w3")
                                .withLayer("l3")));
        rules.add(insert(Rule.deny().withPriority(100 + rules.size())));

        assertEquals(4, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        RuleFilter baseFilter = new RuleFilter(SpecialFilterType.ANY);
        baseFilter.setUser("u0");
        baseFilter.setRole("p0");
        baseFilter.setInstance("i0");
        baseFilter.setService("WCS");
        baseFilter.setRequest(SpecialFilterType.ANY);
        baseFilter.setWorkspace("W0");
        baseFilter.setLayer("l0");

        AccessRequest req = AccessRequest.builder().user(user).filter(baseFilter).build();

        {
            RuleFilter ruleFilter = new RuleFilter(baseFilter);
            ruleFilter.setUser(SpecialFilterType.ANY);
            assertEquals(
                    2, authorizationService.getMatchingRules(req.withFilter(ruleFilter)).size());
            assertEquals(
                    GrantType.ALLOW,
                    authorizationService.getAccessInfo(req.withFilter(ruleFilter)).getGrant());
        }
        {
            RuleFilter ruleFilter = new RuleFilter(baseFilter);
            ruleFilter.setRole(SpecialFilterType.ANY);

            assertEquals(
                    2, authorizationService.getMatchingRules(req.withFilter(ruleFilter)).size());
            assertEquals(
                    GrantType.ALLOW,
                    authorizationService.getAccessInfo(req.withFilter(ruleFilter)).getGrant());
        }
        {
            RuleFilter ruleFilter = new RuleFilter(baseFilter);
            ruleFilter.setUser(SpecialFilterType.ANY);
            ruleFilter.setService("UNMATCH");

            assertEquals(
                    1, authorizationService.getMatchingRules(req.withFilter(ruleFilter)).size());
            assertEquals(
                    GrantType.DENY,
                    authorizationService.getAccessInfo(req.withFilter(ruleFilter)).getGrant());
        }
        {
            RuleFilter ruleFilter = new RuleFilter(baseFilter);
            ruleFilter.setRole(SpecialFilterType.ANY);
            ruleFilter.setService("UNMATCH");

            assertEquals(
                    1, authorizationService.getMatchingRules(req.withFilter(ruleFilter)).size());
            assertEquals(
                    GrantType.DENY,
                    authorizationService.getAccessInfo(req.withFilter(ruleFilter)).getGrant());
        }
    }

    @Test
    public void testResolveLazy() {
        assertEquals(0, ruleAdminService.count());

        List<Rule> rules = new ArrayList<>();

        rules.add(insert(Rule.allow().withPriority(100 + rules.size()).withService("WCS")));
        rules.add(
                insert(
                        Rule.allow()
                                .withPriority(100 + rules.size())
                                .withService("s1")
                                .withRequest("r2")
                                .withWorkspace("w2")
                                .withLayer("l2")));

        LayerDetails details = LayerDetails.builder().build();
        ruleAdminService.setLayerDetails(rules.get(1).getId(), details);

        assertEquals(2, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        AccessInfo accessInfo;

        {
            final RuleFilter ruleFilter = new RuleFilter(SpecialFilterType.ANY);
            ruleFilter.setService("s1");
            ruleFilter.setLayer("l2");
            final AccessRequest req = AccessRequest.builder().filter(ruleFilter).build();
            RuleQuery<RuleFilter> query = RuleQuery.of(new RuleFilter(SpecialFilterType.ANY));
            assertEquals(2, ruleAdminService.getAll(query).count());
            List<Rule> matchingRules = authorizationService.getMatchingRules(req);
            // LOGGER.info("Matching rules: " + matchingRules);
            assertEquals(1, matchingRules.size());
            accessInfo = authorizationService.getAccessInfo(req);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());
            assertNull(accessInfo.getArea());
        }
    }

    @Test
    public void testNoDefault() {
        final User user = createUser("u0", "p0");
        assertEquals(0, ruleAdminService.count(new RuleFilter(SpecialFilterType.ANY)));

        insert(Rule.allow().withService("WCS"));

        assertEquals(
                1, getMatchingRules(user, "u0", "*", "i0", null, "WCS", null, "W0", "l0").size());
        assertEquals(
                GrantType.ALLOW,
                getAccessInfo(user, "u0", "*", "i0", null, "WCS", null, "W0", "l0").getGrant());

        assertEquals(
                1, getMatchingRules(user, "*", "p0", "i0", null, "WCS", null, "W0", "l0").size());
        assertEquals(
                GrantType.ALLOW,
                getAccessInfo(user, "*", "p0", "i0", null, "WCS", null, "W0", "l0").getGrant());

        assertEquals(
                0,
                getMatchingRules(user, "u0", "*", "i0", null, "UNMATCH", null, "W0", "l0").size());
        assertEquals(
                GrantType.DENY,
                getAccessInfo(user, "u0", "*", "i0", null, "UNMATCH", null, "W0", "l0").getGrant());

        assertEquals(
                0,
                getMatchingRules(user, "*", "p0", "i0", null, "UNMATCH", null, "W0", "l0").size());
        assertEquals(
                GrantType.DENY,
                getAccessInfo(user, "*", "p0", "i0", null, "UNMATCH", null, "W0", "l0").getGrant());
    }

    @Test
    public void testGroups() {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("u1", "p1");
        final User u2 = createUser("u2", "p2");

        List<Rule> rules = new ArrayList<>();

        rules.add(
                insert(
                        Rule.allow()
                                .withPriority(10 + rules.size())
                                .withRolename("p1")
                                .withService("s1")
                                .withRequest("r1")
                                .withWorkspace("w1")
                                .withLayer("l1")));
        rules.add(insert(Rule.deny().withPriority(10 + rules.size()).withRolename("p1")));

        // LOGGER.info("SETUP ENDED, STARTING TESTS");
        // ===

        assertEquals(rules.size(), ruleAdminService.count());

        {
            RuleFilter filter = new RuleFilter(SpecialFilterType.ANY).setUser(u1.getName());
            AccessRequest request = AccessRequest.builder().user(u1).filter(filter).build();
            assertEquals(2, authorizationService.getMatchingRules(request).size());
            filter.setService("s1");
            request = request.withFilter(filter);
            assertEquals(2, authorizationService.getMatchingRules(request).size());
            assertEquals(GrantType.ALLOW, authorizationService.getAccessInfo(request).getGrant());

            filter.setService("s2");
            request = request.withFilter(filter);
            assertEquals(1, authorizationService.getMatchingRules(request).size());
            assertEquals(GrantType.DENY, authorizationService.getAccessInfo(request).getGrant());
        }

        {
            RuleFilter filter = new RuleFilter(SpecialFilterType.ANY).setUser(u2.getName());
            AccessRequest request = AccessRequest.builder().user(u2).filter(filter).build();
            assertEquals(0, authorizationService.getMatchingRules(request).size());
            assertEquals(GrantType.DENY, authorizationService.getAccessInfo(request).getGrant());
        }
    }

    @Test
    public void testGroupOrder01() throws UnknownHostException {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("u1", "p1");
        final User u2 = createUser("u2", "p2");

        List<Rule> rules = new ArrayList<Rule>();
        rules.add(insert(Rule.allow().withPriority(10 + rules.size()).withRolename("p1")));
        rules.add(insert(Rule.deny().withPriority(10 + rules.size()).withRolename("p2")));

        // LOGGER.info("SETUP ENDED, STARTING TESTS");
        // ===

        assertEquals(rules.size(), ruleAdminService.count());

        RuleFilter filterU1 = new RuleFilter(SpecialFilterType.ANY).setUser(u1.getName());
        AccessRequest req1 = AccessRequest.builder().user(u1).filter(filterU1).build();

        RuleFilter filterU2 = new RuleFilter(SpecialFilterType.ANY).setUser(u2.getName());
        AccessRequest req2 = AccessRequest.builder().user(u2).filter(filterU2).build();

        assertEquals(1, authorizationService.getMatchingRules(req1).size());
        assertEquals(1, authorizationService.getMatchingRules(req2).size());

        assertEquals(GrantType.ALLOW, authorizationService.getAccessInfo(req1).getGrant());
        assertEquals(GrantType.DENY, authorizationService.getAccessInfo(req2).getGrant());
    }

    @Test
    public void testGroupOrder02() {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("u1", "p1");
        final User u2 = createUser("u2", "p2");

        List<Rule> rules = new ArrayList<Rule>();
        rules.add(insert(Rule.deny().withPriority(10 + rules.size()).withRolename("p2")));
        rules.add(insert(Rule.allow().withPriority(10 + rules.size()).withRolename("p1")));

        // LOGGER.info("SETUP ENDED, STARTING TESTS");
        // ===

        assertEquals(rules.size(), ruleAdminService.count());

        RuleFilter filterU1 = new RuleFilter(SpecialFilterType.ANY).setUser(u1.getName());
        AccessRequest req1 = AccessRequest.builder().user(u1).filter(filterU1).build();

        RuleFilter filterU2 = new RuleFilter(SpecialFilterType.ANY).setUser(u2.getName());
        AccessRequest req2 = AccessRequest.builder().user(u2).filter(filterU2).build();

        assertEquals(1, authorizationService.getMatchingRules(req1).size());
        assertEquals(1, authorizationService.getMatchingRules(req2).size());

        assertEquals(GrantType.ALLOW, authorizationService.getAccessInfo(req1).getGrant());
        assertEquals(GrantType.DENY, authorizationService.getAccessInfo(req2).getGrant());
    }

    @Test
    public void testAttrib() {
        assertEquals(0, ruleAdminService.count());
        final User u1 = createUser("u1", "g1");
        final User u2 = createUser("u2", "g2");
        final User u12 = createUser("u12", "g1", "g2");
        final User u13 = createUser("u13", "g1", "g3");

        {
            Rule r1 = insert(Rule.allow().withRolename("g1").withLayer("l1"));

            Set<LayerAttribute> atts1 =
                    Set.of(
                            LayerAttribute.none().withName("att1").withDataType("String"),
                            LayerAttribute.read().withName("att2").withDataType("String"),
                            LayerAttribute.write().withName("att3").withDataType("String"));

            LayerDetails d1 =
                    LayerDetails.builder()
                            .allowedStyles(Set.of("style01", "style02"))
                            .attributes(atts1)
                            .build();
            ruleAdminService.setLayerDetails(r1.getId(), d1);

            Rule r2 = insert(Rule.allow().withRolename("g2").withLayer("l1"));

            Set<LayerAttribute> atts2 =
                    Set.of(
                            LayerAttribute.read().withName("att1").withDataType("String"),
                            LayerAttribute.write().withName("att2").withDataType("String"),
                            LayerAttribute.none().withName("att3").withDataType("String"));

            LayerDetails d2 =
                    LayerDetails.builder()
                            .allowedStyles(Set.of("style02", "style03"))
                            .attributes(atts2)
                            .build();

            ruleAdminService.setLayerDetails(r2.getId(), d2);

            Rule r3 = insert(Rule.allow().withRolename("g3").withLayer("l1"));
            LayerDetails d3 = LayerDetails.builder().build();
            ruleAdminService.setLayerDetails(r3.getId(), d3);

            insert(Rule.deny().withRolename("g4").withLayer("l1"));
        }

        // LOGGER.info("SETUP ENDED, STARTING
        // TESTS========================================");

        assertEquals(4, ruleAdminService.count());

        // ===

        // TEST u1
        {
            RuleFilter filterU1 = new RuleFilter(SpecialFilterType.ANY).setUser("u1");
            AccessRequest request = AccessRequest.builder().user(u1).filter(filterU1).build();
            // LOGGER.info("getMatchingRules ========================================");
            assertEquals(1, authorizationService.getMatchingRules(request).size());

            // LOGGER.info("getAccessInfo ========================================");
            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());
        }

        // TEST u2
        {
            RuleFilter filter = new RuleFilter(SpecialFilterType.ANY).setUser("u2").setLayer("l1");
            AccessRequest request = AccessRequest.builder().user(u2).filter(filter).build();

            assertEquals(1, authorizationService.getMatchingRules(request).size());

            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());
            assertNotNull(accessInfo.getAttributes());
            assertEquals(3, accessInfo.getAttributes().size());
            assertEquals(
                    Set.of(
                            LayerAttribute.read().withName("att1").withDataType("String"),
                            LayerAttribute.write().withName("att2").withDataType("String"),
                            LayerAttribute.none().withName("att3").withDataType("String")),
                    accessInfo.getAttributes());

            assertEquals(2, accessInfo.getAllowedStyles().size());
        }

        // TEST u3
        // merging attributes at higher access level
        // merging styles
        {
            RuleFilter filter =
                    new RuleFilter(SpecialFilterType.ANY).setUser(u12.getName()).setLayer("l1");
            AccessRequest request = AccessRequest.builder().user(u12).filter(filter).build();

            assertEquals(2, authorizationService.getMatchingRules(request).size());

            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());
            assertNotNull(accessInfo.getAttributes());
            assertEquals(3, accessInfo.getAttributes().size());
            assertEquals(
                    Set.of(
                            LayerAttribute.read().withName("att1").withDataType("String"),
                            LayerAttribute.write().withName("att2").withDataType("String"),
                            LayerAttribute.write().withName("att3").withDataType("String")),
                    accessInfo.getAttributes());

            assertEquals(3, accessInfo.getAllowedStyles().size());
        }

        // TEST u4
        // merging attributes to full access
        // unconstraining styles

        {
            RuleFilter filter;
            filter = new RuleFilter(SpecialFilterType.ANY).setUser(u13.getName()).setLayer("l1");
            AccessRequest request = AccessRequest.builder().user(u13).filter(filter).build();

            assertEquals(2, authorizationService.getMatchingRules(request).size());

            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());
            // LOGGER.info("attributes: " + accessInfo.getAttributes());
            assertTrue(accessInfo.getAttributes().isEmpty());
            // assertEquals(3, accessInfo.getAttributes().size());
            // assertEquals(
            // new HashSet(Arrays.asList(
            // new LayerAttribute("att1", "String", AccessType.READONLY),
            // new LayerAttribute("att2", "String", AccessType.READWRITE),
            // new LayerAttribute("att3", "String", AccessType.READWRITE))),
            // accessInfo.getAttributes());

            assertTrue(accessInfo.getAllowedStyles().isEmpty());
        }
    }

    /** Added for issue #23 */
    @Test
    public void testNullAllowableStyles() {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("u1", "g1", "g2");

        // no details for first rule
        {
            insert(Rule.allow().withPriority(30).withRolename("g2").withLayer("l1"));
        }
        // some allowed styles for second rule
        {
            Rule r1 = insert(Rule.allow().withPriority(40).withRolename("g1").withLayer("l1"));

            LayerDetails d1 =
                    LayerDetails.builder().allowedStyles(Set.of("style01", "style02")).build();

            ruleAdminService.setLayerDetails(r1.getId(), d1);
        }

        // LOGGER.info("SETUP ENDED, STARTING
        // TESTS========================================");

        assertEquals(2, ruleAdminService.count());

        // ===

        // TEST u1
        {
            RuleFilter filterU1 = new RuleFilter(SpecialFilterType.ANY).setUser(u1.getName());
            AccessRequest request = AccessRequest.builder().user(u1).filter(filterU1).build();

            // LOGGER.info("getMatchingRules ========================================");
            assertEquals(2, authorizationService.getMatchingRules(request).size());

            // LOGGER.info("getAccessInfo ========================================");
            AccessInfo accessInfo = authorizationService.getAccessInfo(request);
            assertEquals(GrantType.ALLOW, accessInfo.getGrant());

            assertTrue(accessInfo.getAllowedStyles().isEmpty());
        }
    }

    @Test
    public void testIPAddress() {

        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY);
        assertEquals(0, ruleAdminService.count(filter));

        IPAddressRange ip10 = IPAddressRange.fromCidrSignature("10.10.100.0/24");
        IPAddressRange ip192 = IPAddressRange.fromCidrSignature("192.168.0.0/16");

        Rule r1 =
                Rule.allow()
                        .withPriority(10)
                        .withRolename("g1")
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1")
                        .withAddressRange(ip10);
        Rule r2 =
                Rule.allow()
                        .withPriority(20)
                        .withRolename("g2")
                        .withService("s1")
                        .withRequest("r2")
                        .withWorkspace("w2")
                        .withLayer("l2")
                        .withAddressRange(ip10);
        Rule r3 =
                Rule.allow()
                        .withPriority(30)
                        .withRolename("g1")
                        .withService("s3")
                        .withRequest("r3")
                        .withWorkspace("w3")
                        .withLayer("l3")
                        .withAddressRange(ip192);
        Rule r4 = Rule.allow().withPriority(40).withRolename("g1");

        r1 = insert(r1);
        r2 = insert(r2);
        r3 = insert(r3);
        r4 = insert(r4);

        // test without address filtering

        assertEquals(4, getMatchingRules(null, "*", "*", "*", "*", "*", "*", "*", "*").size());
        assertEquals(3, getMatchingRules(null, "*", "g1", "*", "*", "*", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "g2", "*", "*", "*", "*", "*", "*").size());
        assertEquals(2, getMatchingRules(null, "*", "g1", "*", "*", "s1", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "*", "*", "*", "ZZ", "*", "*", "*").size());

        // test with address filtering
        assertEquals(
                3, getMatchingRules(null, "*", "*", "*", "10.10.100.4", "*", "*", "*", "*").size());
        assertEquals(
                2,
                getMatchingRules(null, "*", "g1", "*", "10.10.100.4", "*", "*", "*", "*").size());
        assertEquals(
                1, getMatchingRules(null, "*", "*", "*", "10.10.1.4", "*", "*", "*", "*").size());
        assertEquals(
                2, getMatchingRules(null, "*", "*", "*", "192.168.1.1", "*", "*", "*", "*").size());
        assertEquals(1, getMatchingRules(null, "*", "*", "*", null, "*", "*", "*", "*").size());

        List<Rule> matchingRules = getMatchingRules(null, "*", "*", "*", "BAD", "*", "*", "*", "*");
        assertEquals(0, matchingRules.size());
    }

    @Test
    public void testGetRulesForUserOnly() {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("TestUser1", "g1");

        insert(
                Rule.allow()
                        .withPriority(10)
                        .withRolename("g1")
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1"));
        insert(
                Rule.allow()
                        .withPriority(20)
                        .withRolename("g2")
                        .withService("s2")
                        .withRequest("r2")
                        .withWorkspace("w2")
                        .withLayer("l2"));
        insert(
                Rule.allow()
                        .withPriority(30)
                        .withRolename("g1")
                        .withService("s3")
                        .withRequest("r3")
                        .withWorkspace("w3")
                        .withLayer("l3"));
        insert(Rule.allow().withPriority(40).withRolename("g1"));
        insert(Rule.allow().withPriority(50).withRolename("g3a"));
        insert(Rule.allow().withPriority(60).withRolename("g3b"));

        RuleFilter filter;

        filter = createFilter(u1.getName(), null, null);
        assertEquals(3, authorizationService.getMatchingRules(AccessRequest.of(u1, filter)).size());

        filter = createFilter(u1.getName(), null, "s1");
        assertEquals(2, authorizationService.getMatchingRules(AccessRequest.of(u1, filter)).size());

        filter = createFilter(u1.getName(), null, "s3");
        assertEquals(2, authorizationService.getMatchingRules(AccessRequest.of(u1, filter)).size());

        filter = createFilter("anonymous", null, null);
        assertEquals(
                0, authorizationService.getMatchingRules(AccessRequest.of(null, filter)).size());
    }

    @Test
    public void testAdminRules() {

        final User user = createUser("auth00");

        insert(
                Rule.allow()
                        .withPriority(10)
                        .withUsername(user.getName())
                        .withService("s1")
                        .withRequest("r1")
                        .withWorkspace("w1")
                        .withLayer("l1"));

        RuleFilter filter = new RuleFilter(SpecialFilterType.ANY, true);
        filter.setWorkspace("w1");

        AccessInfo accessInfo = authorizationService.getAccessInfo(AccessRequest.of(user, filter));
        assertEquals(GrantType.ALLOW, accessInfo.getGrant());
        assertFalse(accessInfo.isAdminRights());

        // let's add a USER adminrule

        insert(AdminRule.user().withPriority(20).withUsername(user.getName()));

        accessInfo = authorizationService.getAccessInfo(AccessRequest.of(user, filter));
        assertEquals(GrantType.ALLOW, accessInfo.getGrant());
        assertFalse(accessInfo.isAdminRights());

        // let's add an ADMIN adminrule on workspace w1

        adminruleAdminService.insert(
                AdminRule.admin()
                        .withPriority(10)
                        .withUsername(user.getName())
                        .withWorkspace("w1"));

        accessInfo = authorizationService.getAccessInfo(AccessRequest.of(user, filter));
        assertEquals(GrantType.ALLOW, accessInfo.getGrant());
        assertTrue(accessInfo.isAdminRights());
    }

    // @Disabled
    @Test
    public void testMultiRoles() {
        assertEquals(0, ruleAdminService.count());

        final User u1 = createUser("TestUser1", "p1");
        final User u2 = createUser("TestUser2", "p2");
        final User u3 = createUser("TestUser3", "p1", "p2");

        insert(10, u1.getName(), "p1", null, null, "s1", "r1", null, "w1", "l1", GrantType.ALLOW);
        insert(20, u2.getName(), "p2", null, null, "s1", "r2", null, "w2", "l2", GrantType.ALLOW);
        insert(30, u1.getName(), null, null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(40, u2.getName(), null, null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(50, u3.getName(), null, null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(51, u3.getName(), "p1", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(52, u3.getName(), "p2", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(60, null, "p1", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(70, null, "p2", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(80, null, "p3", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(901, u1.getName(), "p2", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(902, u2.getName(), "p1", null, null, null, null, null, null, null, GrantType.ALLOW);
        insert(999, null, null, null, null, null, null, null, null, null, GrantType.ALLOW);

        assertRules(
                "*", "*", new Integer[] {10, 20, 30, 40, 50, 51, 52, 60, 70, 80, 901, 902, 999});
        assertRules("*", null, new Integer[] {30, 40, 50, 999});
        assertRules("*", "NO", new Integer[] {30, 40, 50, 999});
        assertRules("*", "p1", new Integer[] {10, 30, 40, 50, 51, 60, 902, 999});
        assertRules("*", "p1,NO", new Integer[] {10, 30, 40, 50, 51, 60, 902, 999});
        assertRules(
                "*", "p1,p2", new Integer[] {10, 20, 30, 40, 50, 51, 52, 60, 70, 901, 902, 999});
        assertRules(
                "*", "p1,p2,NO", new Integer[] {10, 20, 30, 40, 50, 51, 52, 60, 70, 901, 902, 999});

        assertRules((String) null, "*", new Integer[] {60, 70, 80, 999});
        assertRules((String) null, null, new Integer[] {999});
        assertRules((String) null, "NO", new Integer[] {999});
        assertRules((String) null, "p1", new Integer[] {60, 999});
        assertRules((String) null, "p1,NO", new Integer[] {60, 999});
        assertRules((String) null, "p1,p2", new Integer[] {60, 70, 999});
        assertRules((String) null, "p1,p2,NO", new Integer[] {60, 70, 999});

        assertRules("NO", "*", new Integer[] {999});
        assertRules("NO", null, new Integer[] {999});
        assertRules("NO", "NO", new Integer[] {999});
        assertRules("NO", "p1", new Integer[] {999});
        assertRules("NO", "p1,NO", new Integer[] {999});
        assertRules("NO", "p1,p2", new Integer[] {999});
        assertRules("NO", "p1,p2,NO", new Integer[] {999});

        assertRules(u1, "*", new Integer[] {10, 30, 60, 999});
        assertRules(u1, null, new Integer[] {30, 999});
        assertRules(u1, "NO", new Integer[] {30, 999});
        assertRules(u1, "p1", new Integer[] {10, 30, 60, 999});
        assertRules(u1, "p1,NO", new Integer[] {10, 30, 60, 999});
        assertRules(u1, "p1,p2", new Integer[] {10, 30, 60, 999});
        assertRules(u1, "p1,p2,NO", new Integer[] {10, 30, 60, 999});

        assertRules(u3, "*", new Integer[] {50, 51, 52, 60, 70, 999});
        assertRules(u3, null, new Integer[] {50, 999});
        assertRules(u3, "NO", new Integer[] {50, 999});
        assertRules(u3, "p1", new Integer[] {50, 51, 60, 999});
        assertRules(u3, "p2", new Integer[] {50, 52, 70, 999});
        assertRules(u3, "p1,NO", new Integer[] {50, 51, 60, 999});
        assertRules(u3, "p1,p2", new Integer[] {50, 51, 52, 60, 70, 999});
        assertRules(u3, "p1,p2,p3", new Integer[] {50, 51, 52, 60, 70, 999});
        assertRules(u3, "p1,p2,NO", new Integer[] {50, 51, 52, 60, 70, 999});
    }

    private AccessRequest createRequest(String userName, String groupName) {
        RuleFilter ruleFilter =
                new RuleFilter(userName, groupName, "*", "*", "*", "*", "*", "*", "*");
        return AccessRequest.of(null, ruleFilter);
    }

    private void assertRules(String userName, String groupName, Integer[] expectedPriorities) {
        assertRules(createRequest(userName, groupName), expectedPriorities);
    }

    private AccessRequest createRequest(@NonNull User user, String groupName) {
        RuleFilter filter =
                new RuleFilter(user.getName(), groupName, "*", "*", "*", "*", "*", "*", "*");
        return AccessRequest.of(user, filter);
    }

    private void assertRules(@NonNull User user, String groupName, Integer[] expectedPriorities) {
        assertRules(createRequest(user, groupName), expectedPriorities);
    }

    private void assertRules(AccessRequest request, Integer[] expectedPriorities) {
        RuleFilter origFilter = request.getFilter().clone();
        List<Rule> rules = authorizationService.getMatchingRules(request);

        Set<Long> pri = rules.stream().map(r -> r.getPriority()).collect(Collectors.toSet());
        Set<Long> exp =
                Arrays.asList(expectedPriorities).stream()
                        .map(i -> i.longValue())
                        .collect(Collectors.toSet());
        assertEquals(exp, pri, "Bad rule set selected for filter " + origFilter);
    }

    private List<Rule> getMatchingRules(
            @Nullable User user,
            String userName,
            String profileName,
            String instanceName,
            String sourceAddress,
            String service,
            String request,
            String workspace,
            String layer) {

        RuleFilter filter =
                new RuleFilter(
                        userName,
                        profileName,
                        instanceName,
                        sourceAddress,
                        service,
                        request,
                        null,
                        workspace,
                        layer);

        AccessRequest req = AccessRequest.builder().user(user).filter(filter).build();
        return authorizationService.getMatchingRules(req);
    }

    private AccessInfo getAccessInfo(
            @Nullable User user,
            String userName,
            String roleName,
            String instanceName,
            String sourceAddress,
            String service,
            String request,
            String workspace,
            String layer) {

        RuleFilter filter =
                new RuleFilter(
                        userName,
                        roleName,
                        instanceName,
                        sourceAddress,
                        service,
                        request,
                        null,
                        workspace,
                        layer);
        AccessRequest req = AccessRequest.builder().user(user).filter(filter).build();
        return authorizationService.getAccessInfo(req);
    }
}
