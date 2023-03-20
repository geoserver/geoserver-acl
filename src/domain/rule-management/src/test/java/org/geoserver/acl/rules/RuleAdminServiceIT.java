/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.model.rules.InsertPosition.FROM_END;
import static org.geoserver.acl.model.rules.InsertPosition.FROM_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.assertj.core.api.AbstractObjectAssert;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.codec.Wkt;
import org.geoserver.acl.model.filter.RuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.CatalogMode;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.LayerAttribute;
import org.geoserver.acl.model.rules.LayerAttribute.AccessType;
import org.geoserver.acl.model.rules.LayerDetails;
import org.geoserver.acl.model.rules.LayerDetails.LayerType;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleLimits;
import org.geoserver.acl.model.rules.SpatialFilterType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RuleAdminServiceIT {

    protected RuleAdminService ruleAdminService;

    @BeforeEach
    protected void setUp() throws Exception {
        this.ruleAdminService = getRuleAdminService();
    }

    protected RuleAdminService getRuleAdminService() {
        return new RuleAdminService(new MemoryRuleRepository());
    }

    @Test
    void testCreateRule(TestInfo testInfo) {
        Rule insert = Rule.allow().withPriority(1).withWorkspace(testInfo.getDisplayName());
        Rule rule = ruleAdminService.insert(insert);
        assertThat(rule).isNotNull();
        assertThat(rule.getId()).isNotNull();
        assertThat(rule).isNotEqualTo(insert);
        assertThat(rule.withId(null)).isEqualTo(insert);
    }

    @Test
    void testCreateRule_duplicate_identifier(TestInfo testInfo) {
        Rule insert = Rule.allow().withPriority(1).withWorkspace(testInfo.getDisplayName());
        Rule created = ruleAdminService.insert(insert);
        assertThat(created.withId(null)).isEqualTo(insert);

        RuleIdentifierConflictException expected =
                assertThrows(
                        RuleIdentifierConflictException.class,
                        () -> ruleAdminService.insert(insert));
        assertThat(expected.getMessage())
                .contains("A Rule with the same identifier already exists");
    }

    @Test
    void testCreateRule_insertPosition_Fixed_No_Shifts_Required(TestInfo testInfo) {
        Rule r1 = ruleAdminService.insert(Rule.allow().withPriority(10).withLayer("L1"));
        assertThat(r1.getPriority()).isEqualTo(10);

        Rule r2 = ruleAdminService.insert(Rule.allow().withPriority(20).withLayer("L2"));
        assertThat(r2.getPriority()).isEqualTo(20);

        Rule r3 = ruleAdminService.insert(Rule.allow().withPriority(15).withLayer("L3"));
        assertThat(r3.getPriority()).isEqualTo(15);

        assertThat(ruleAdminService.getAll()).isEqualTo(List.of(r1, r3, r2));
    }

    @Test
    void testCreateRule_insertPosition_Fixed(TestInfo testInfo) {
        Rule r1 = ruleAdminService.insert(Rule.allow().withLayer("L1"));
        assertThat(r1.getPriority()).isEqualTo(1);

        Rule r2 = ruleAdminService.insert(Rule.allow().withLayer("L2"));
        assertThat(r2.getPriority()).isEqualTo(2);

        Rule r3 = ruleAdminService.insert(Rule.allow().withLayer("L3"));
        assertThat(r3.getPriority()).isEqualTo(3);

        // create another with priority 2, should displace r2 and r3
        Rule r4 = ruleAdminService.insert(Rule.allow().withLayer("L4").withPriority(2));
        assertThat(r4.getPriority()).isEqualTo(2);

        Rule r2Displaced = ruleAdminService.getRuleByPriority(3).orElseThrow();
        assertThat(r2Displaced.getId()).isEqualTo(r2.getId());
        assertThat(r2Displaced.getIdentifier()).isEqualTo(r2.getIdentifier());

        Rule r3Displaced = ruleAdminService.getRuleByPriority(4).orElseThrow();
        assertThat(r3Displaced.getId()).isEqualTo(r3.getId());
        assertThat(r3Displaced.getIdentifier()).isEqualTo(r3.getIdentifier());
    }

    @Test
    void testCreateRule_insertPosition_FromStart(TestInfo testInfo) {
        Rule r1 =
                ruleAdminService.insert(Rule.allow().withPriority(10).withLayer("L1"), FROM_START);
        assertThat(r1.getPriority())
                .as("With an empty repo, should have assigned priority 1")
                .isEqualTo(1);

        Rule r2 = ruleAdminService.insert(Rule.deny().withLayer("L2"), FROM_START);
        assertThat(r2.getPriority())
                .as("With requested position == 0, should have assigned priority 1")
                .isEqualTo(1);

        assertThat(ruleAdminService.get(r1.getId()).orElseThrow().getPriority())
                .as("R1 shoud have been displaced")
                .isEqualTo(2);

        Rule r3 = ruleAdminService.insert(Rule.limit().withPriority(2).withLayer("L3"), FROM_START);
        assertThat(r3.getPriority()).isEqualTo(2);
        assertThat(ruleAdminService.get(r1.getId()).orElseThrow().getPriority())
                .as("R1 shoud have been displaced")
                .isEqualTo(3);
        assertThat(ruleAdminService.get(r2.getId()).orElseThrow().getPriority())
                .as("r2 shoud have NOT been displaced")
                .isEqualTo(1);

        Rule r4 =
                ruleAdminService.insert(Rule.limit().withPriority(20).withLayer("L4"), FROM_START);
        assertThat(r4.getPriority())
                .as("r4 should have been assigned 1 + max(priority)")
                .isEqualTo(4);
    }

    @Test
    void testCreateRule_insertPosition_FromEnd(TestInfo testInfo) {
        Rule r1 = ruleAdminService.insert(Rule.allow().withPriority(10).withLayer("L1"), FROM_END);
        assertThat(r1.getPriority())
                .as("With an empty repo, should have assigned priority 1")
                .isEqualTo(1);

        Rule r2 = ruleAdminService.insert(Rule.deny().withLayer("L2"), FROM_END);
        assertThat(r2.getPriority())
                .as("With requested position == 0, should have assigned 1 + max(priority)")
                .isEqualTo(2);

        Rule r3 = ruleAdminService.insert(Rule.limit().withPriority(1).withLayer("L3"), FROM_END);
        assertThat(r3.getPriority())
                .as("With requested position == 1, should have assigned max(priority) - 1")
                .isEqualTo(2);

        assertThat(ruleAdminService.get(r2.getId()).orElseThrow().getPriority())
                .as("r2 shoud have been displaced")
                .isEqualTo(3);

        Rule r4 = ruleAdminService.insert(Rule.limit().withPriority(2).withLayer("L4"), FROM_END);
        assertThat(r4.getPriority())
                .as("With requested position == 2, should have assigned max(priority) - 2")
                .isEqualTo(2);

        assertThat(ruleAdminService.get(r3.getId()).orElseThrow().getPriority())
                .as("r3 shoud have been displaced")
                .isEqualTo(3);
        assertThat(ruleAdminService.get(r2.getId()).orElseThrow().getPriority())
                .as("r2 shoud have been displaced")
                .isEqualTo(4);

        Rule r5 = ruleAdminService.insert(Rule.limit().withPriority(20).withLayer("L5"), FROM_END);
        assertThat(r5.getPriority())
                .as("With requested position == 20, should have assigned min(priority)")
                .isEqualTo(1);

        assertThat(ruleAdminService.getAll())
                .as(
                        "Underflow priority index should result in priority 1 and displaced alll others")
                .isEqualTo(
                        List.of(
                                r5.withPriority(1),
                                r1.withPriority(2),
                                r4.withPriority(3),
                                r3.withPriority(4),
                                r2.withPriority(5)));
    }

    @Test
    void testDeleteRuleById() {
        Rule r1 = ruleAdminService.insert(Rule.allow().withPriority(10).withLayer("L1"));
        Rule r2 = ruleAdminService.insert(Rule.allow().withPriority(11).withLayer("L2"));

        assertThat(ruleAdminService.delete(r1.getId())).isTrue();
        assertThat(ruleAdminService.delete(r1.getId())).isFalse();
        assertThat(ruleAdminService.getAll()).isEqualTo(List.of(r2));

        assertThat(ruleAdminService.delete(r2.getId())).isTrue();
        assertThat(ruleAdminService.delete(r2.getId())).isFalse();
        assertThat(ruleAdminService.getAll()).isEmpty();
    }

    @Test
    void testGetAll_and_GetList_paginated() {
        assertThat(ruleAdminService.getAll()).isNotNull().isEmpty();

        final List<Rule> all = new ArrayList<>();
        IntStream.range(0, 250)
                .forEach(
                        i -> {
                            all.add(addOneForLayer(i));
                            assertThat(ruleAdminService.getAll()).isEqualTo(all);
                        });

        final int pageSize = 25;
        final int maxPages = all.size() / pageSize;
        RuleQuery<RuleFilter> query = RuleQuery.of();
        query.setLimit(1 + pageSize);
        String nextCursorId = null;
        for (int page = 0; page < maxPages; page++) {
            query.setNextId(nextCursorId);
            List<Rule> result = ruleAdminService.getAll(query).collect(Collectors.toList());
            if (result.size() > pageSize) {
                assertThat(result.size()).isEqualTo(1 + pageSize);
                nextCursorId = result.get(pageSize).getId();
                result = result.subList(0, pageSize);
            }

            int fromIndex = page * pageSize;
            List<Rule> expected = all.subList(fromIndex, pageSize + fromIndex);
            assertThat(result).isEqualTo(expected);
        }
    }

    @Disabled("not yet implemented")
    @Test
    void testQueryRules() {
        fail("Not yet implemented");
    }

    @Test
    void testGetRuleById() {
        assertThrows(NullPointerException.class, () -> ruleAdminService.get(null));

        RuleLimits limits = sampleLimits();

        Rule r1 =
                ruleAdminService.insert(
                        Rule.limit().withRuleLimits(limits).withWorkspace("ws").withLayer("l1"));
        Rule r2 = ruleAdminService.insert(Rule.allow().withWorkspace("ws").withLayer("l1"));
        Rule r3 = ruleAdminService.insert(Rule.deny());

        assertGet(r1).isEqualTo(r1);
        assertGet(r2).isEqualTo(r2);
        assertGet(r3).isEqualTo(r3);

        assertThat(ruleAdminService.get("abcd")).isEmpty();
    }

    @Test
    void testFindOneRuleByPriority() {
        assertThat(ruleAdminService.getRuleByPriority(1)).isEmpty();

        RuleLimits limits = sampleLimits();
        Rule r1 =
                ruleAdminService.insert(
                        Rule.limit()
                                .withPriority(1_000)
                                .withRuleLimits(limits)
                                .withWorkspace("ws")
                                .withLayer("l1"));

        Rule r2 =
                ruleAdminService.insert(
                        Rule.allow().withPriority(1_000_1).withWorkspace("ws").withLayer("l1"));

        Rule r3 = ruleAdminService.insert(Rule.deny().withPriority(1_000_000));

        assertThat(ruleAdminService.getRuleByPriority(1_000)).isPresent().get().isEqualTo(r1);
        assertThat(ruleAdminService.getRuleByPriority(1_000_1)).isPresent().get().isEqualTo(r2);
        assertThat(ruleAdminService.getRuleByPriority(1_000_000)).isPresent().get().isEqualTo(r3);

        assertThat(ruleAdminService.getRuleByPriority(1)).isEmpty();
    }

    @Test
    void testCountAllRules() {
        IntStream.range(0, 10)
                .forEach(
                        i -> {
                            assertThat(ruleAdminService.count()).isEqualTo(i);
                            addOneForLayer(i);
                        });
    }

    private Rule addOneForLayer(int i) {
        return ruleAdminService.insert(Rule.limit().withWorkspace("ws").withLayer("l_" + i));
    }

    @Disabled("not yet implemented")
    @Test
    void testCountRules_Filter() {
        fail("Not yet implemented");
    }

    @Test
    void testSetRuleAllowedStyles_preconditions() {
        Rule r1 = addOneForLayer(1);
        Rule r2 = addOneForLayer(2);

        assertThat(ruleAdminService.getAllowedStyles(r1.getId())).isEmpty();
        assertThat(ruleAdminService.getAllowedStyles(r2.getId())).isEmpty();

        final Set<String> styles = Set.of("style1", "style2");

        final Class<IllegalArgumentException> expectedType = IllegalArgumentException.class;
        IllegalArgumentException ex;

        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setAllowedStyles("blahblahblah", styles));
        assertThat(ex.getMessage()).contains("Invalid id");

        final String id = r2.getId();
        ruleAdminService.delete(id);

        ex = assertThrows(expectedType, () -> ruleAdminService.setAllowedStyles(id, styles));
        assertThat(ex.getMessage()).contains("Rule " + id + " does not exist");

        Rule ruleWithNoLayer = ruleAdminService.insert(Rule.deny());
        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setAllowedStyles(ruleWithNoLayer.getId(), styles));
        assertThat(ex.getMessage()).contains("Rule has no layer, can't set allowed styles");
    }

    @Test
    void testSetGetRuleAllowedStyles() {
        Rule r1 = addOneForLayer(1);
        Rule r2 = addOneForLayer(2);

        String r1Id = r1.getId();

        assertThat(ruleAdminService.getAllowedStyles(r1.getId())).isEmpty();
        assertThat(ruleAdminService.getAllowedStyles(r2.getId())).isEmpty();

        final Set<String> styles = Set.of("style1", "style2");
        String reason =
                assertThrows(
                                IllegalArgumentException.class,
                                () -> ruleAdminService.setAllowedStyles(r1Id, styles))
                        .getMessage();
        assertThat(reason).contains("Rule has no details associated");

        r1 = ruleAdminService.update(r1.withAccess(GrantType.ALLOW));
        ruleAdminService.setLayerDetails(
                r1Id, LayerDetails.builder().spatialFilterType(SpatialFilterType.CLIP).build());

        ruleAdminService.setAllowedStyles(r1Id, styles);

        assertThat(ruleAdminService.getAllowedStyles(r1.getId())).isEqualTo(styles);
        assertThat(ruleAdminService.getAllowedStyles(r2.getId())).isEmpty();

        ruleAdminService.setAllowedStyles(r1.getId(), null);
        assertThat(ruleAdminService.getAllowedStyles(r1.getId())).isEmpty();
        assertThat(ruleAdminService.getAllowedStyles(r2.getId())).isEmpty();
    }

    @Test
    void testSetLayerDetails_preconditions() {
        Rule limit = ruleAdminService.insert(Rule.limit());
        Rule deny = ruleAdminService.insert(Rule.deny());

        LayerDetails details1 = sampleDetails(1);

        final Class<IllegalArgumentException> expectedType = IllegalArgumentException.class;

        IllegalArgumentException ex;
        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setLayerDetails("blahblahblah", details1));
        assertThat(ex.getMessage()).contains("Invalid id");

        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setLayerDetails(limit.getId(), details1));
        assertThat(ex.getMessage()).contains("Rule is not of ALLOW type");

        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setLayerDetails(deny.getId(), details1));
        assertThat(ex.getMessage()).contains("Rule is not of ALLOW type");

        final String id = deny.getId();
        ruleAdminService.delete(id);

        ex = assertThrows(expectedType, () -> ruleAdminService.setLayerDetails(id, details1));
        assertThat(ex.getMessage()).contains("Rule " + id + " does not exist");

        Rule allowWithNoLayer =
                ruleAdminService.insert(Rule.allow().withWorkspace("ws").withLayer(null));
        ex =
                assertThrows(
                        expectedType,
                        () -> ruleAdminService.setLayerDetails(allowWithNoLayer.getId(), details1));
        assertThat(ex.getMessage()).contains("Rule does not refer to a fixed layer");
    }

    @Test
    void testSetGetLayerDetails() {
        Rule r1 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l1"));
        Rule r2 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l2"));

        final LayerDetails details1 = sampleDetails(1);
        final LayerDetails details2 = sampleDetails(2);

        assertThat(ruleAdminService.getLayerDetails(r1)).isEmpty();
        assertThat(ruleAdminService.getLayerDetails(r1.getId())).isEmpty();
        assertThat(ruleAdminService.getLayerDetails(r2)).isEmpty();
        assertThat(ruleAdminService.getLayerDetails(r2.getId())).isEmpty();

        ruleAdminService.setLayerDetails(r1.getId(), details1);
        assertThat(ruleAdminService.getLayerDetails(r1)).isPresent().get().isEqualTo(details1);

        ruleAdminService.setLayerDetails(r2.getId(), details2);
        assertThat(ruleAdminService.getLayerDetails(r2)).isPresent().get().isEqualTo(details2);

        ruleAdminService.setLayerDetails(r1.getId(), null);
        assertThat(ruleAdminService.getLayerDetails(r1)).isEmpty();
        assertThat(ruleAdminService.getLayerDetails(r2)).isPresent().get().isEqualTo(details2);

        ruleAdminService.setLayerDetails(r2.getId(), null);
        assertThat(ruleAdminService.getLayerDetails(r2)).isEmpty();
    }

    @Test
    void testUpdateRuleById_allows_several_limit_rules() {
        Rule r1 = ruleAdminService.insert(Rule.limit().withWorkspace("ws1").withLayer("l1"));
        Rule r2 = ruleAdminService.insert(Rule.limit().withWorkspace("ws1").withLayer("l2"));
        assertThat(r1.getId()).isNotSameAs(r2.getId());

        r1 = r1.withRuleLimits(sampleLimits());

        assertThat(ruleAdminService.update(r1)).isEqualTo(r1);

        Rule r2WithDupIdentifier = r2.withLayer("l1");
        assertThat(r2WithDupIdentifier.getIdentifier()).isEqualTo(r1.getIdentifier());

        Rule updated = ruleAdminService.update(r2WithDupIdentifier);
        assertThat(updated).isEqualTo(r2WithDupIdentifier);

        final Rule deleted = r2;
        assertTrue(ruleAdminService.delete(deleted.getId()));
        String msg =
                assertThrows(IllegalArgumentException.class, () -> ruleAdminService.update(deleted))
                        .getMessage();
        assertThat(msg).contains("Rule " + deleted.getId() + " does not exist");
    }

    @Test
    void testUpdateRuleById_fails_with_non_limit_duplicate_identifier() {
        Rule limit1 = ruleAdminService.insert(Rule.limit().withWorkspace("ws1").withLayer("l1"));
        Rule limit2 = ruleAdminService.insert(Rule.limit().withWorkspace("ws1").withLayer("l2"));
        Rule allow = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l2"));
        Rule deny = ruleAdminService.insert(Rule.deny().withWorkspace("ws1").withLayer("l2"));

        // this is allowed
        Rule allowToLimit = allow.withAccess(GrantType.LIMIT);
        assertThat(ruleAdminService.update(allowToLimit)).isEqualTo(allowToLimit);
        assertThat(ruleAdminService.getAll())
                .isEqualTo(List.of(limit1, limit2, allowToLimit, deny));
        // revert
        ruleAdminService.update(allowToLimit.withAccess(GrantType.ALLOW));

        final List<Rule> expected = List.of(limit1, limit2, allow, deny);
        assertThat(ruleAdminService.getAll()).isEqualTo(expected);

        // this is not, there's another deny with the same exact coordinates
        Rule allowToDeny = allow.withAccess(GrantType.DENY);
        String ex;
        ex =
                assertThrows(
                                RuleIdentifierConflictException.class,
                                () -> ruleAdminService.update(allowToDeny))
                        .getMessage();
        assertThat(ex).contains("A Rule with the same identifier already exists");
        assertThat(ruleAdminService.getAll()).as("Nothing should have changed").isEqualTo(expected);

        // same thing the other way around
        Rule denyToAllow = deny.withAccess(GrantType.ALLOW);
        ex =
                assertThrows(
                                RuleIdentifierConflictException.class,
                                () -> ruleAdminService.update(denyToAllow))
                        .getMessage();
        assertThat(ex).contains("A Rule with the same identifier already exists");
        assertThat(ruleAdminService.getAll()).as("Nothing should have changed").isEqualTo(expected);
    }

    @Test
    void testUpdateRuleById_shifts_other_priorities_if_needed() {
        Rule r1 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l1"));
        Rule r2 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l2"));
        Rule r3 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l3"));
        Rule r4 = ruleAdminService.insert(Rule.allow().withWorkspace("ws1").withLayer("l4"));
        assertEquals(1, r1.getPriority());
        assertEquals(2, r2.getPriority());
        assertEquals(3, r3.getPriority());
        assertEquals(4, r4.getPriority());

        // moving r2 one slot up, should only swap with r1, ending in
        // r2(1),r1(2),r3(3),r4(4)
        r2 = r2.withPriority(1);
        assertThat(ruleAdminService.update(r2)).isEqualTo(r2);

        assertGet(r1)
                .as("r1 should have been displaced to priority 2")
                .isEqualTo(r1.withPriority(2));

        assertGet(r3).as("r3 should have kept priority 3").isEqualTo(r3);

        assertGet(r4).as("r4 should have kept priority 4").isEqualTo(r4);

        // moving r3 three slots up, should shift r1 and r2 only, ending in
        // r3(1),r2(2),r1(3),r4(4)
        r3 = r3.withPriority(1);
        assertThat(ruleAdminService.update(r3)).isEqualTo(r3);

        assertGet(r2)
                .as("r2 should have been displaced to priority 2")
                .isEqualTo(r2.withPriority(2));

        assertGet(r1)
                .as("r1 should have been displaced to priority 3")
                .isEqualTo(r1.withPriority(3));

        assertGet(r4).as("r4 should have kept priority 4").isEqualTo(r4);
    }

    @Test
    void testSetLimits() {
        final RuleLimits limits = sampleLimits();

        Rule limit = ruleAdminService.insert(Rule.limit());
        Rule allow = ruleAdminService.insert(Rule.allow());
        Rule deny = ruleAdminService.insert(Rule.deny());

        // no failure if setting limits to null on a non-limit rule
        ruleAdminService.setLimits(allow.getId(), null);
        ruleAdminService.setLimits(deny.getId(), null);
        assertGet(allow).isEqualTo(allow);
        assertGet(deny).isEqualTo(deny);

        // expected failure if setting limits to a non-limit rule
        IllegalArgumentException ex;
        ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ruleAdminService.setLimits(allow.getId(), limits));
        assertThat(ex.getMessage()).contains("Rule is not of LIMIT type");

        ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ruleAdminService.setLimits(deny.getId(), limits));
        assertThat(ex.getMessage()).contains("Rule is not of LIMIT type");

        ruleAdminService.delete(deny.getId());
        ex =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ruleAdminService.setLimits(deny.getId(), limits));
        assertThat(ex.getMessage()).contains("Rule " + deny.getId() + " does not exist");

        ruleAdminService.setLimits(limit.getId(), limits);
        assertGet(limit).isEqualTo(limit.withRuleLimits(limits));

        ruleAdminService.setLimits(limit.getId(), null);
        assertGet(limit).isEqualTo(limit.withRuleLimits(null));
    }

    @Test
    void testShiftRulesByPriority() {
        Rule r1 = addOneForLayer(1);
        Rule r2 = addOneForLayer(2);
        Rule r3 = addOneForLayer(3);
        Rule r4 = addOneForLayer(4);
        Rule r5 = addOneForLayer(5);
        assertThat(ruleAdminService.count()).isEqualTo(5);

        assertThat(ruleAdminService.shift(1, 10)).isEqualTo(5);
        List<Rule> expected = List.of(r1, r2, r3, r4, r5);
        assertPriorities(expected, List.of(11, 12, 13, 14, 15));

        assertThat(ruleAdminService.shift(13, 5)).isEqualTo(3);
        assertPriorities(expected, List.of(11, 12, 18, 19, 20));

        assertThat(ruleAdminService.shift(20, 1)).isEqualTo(1);
        assertPriorities(expected, List.of(11, 12, 18, 19, 21));

        assertThat(ruleAdminService.shift(22, 1)).isEqualTo(-1);

        String msg =
                assertThrows(IllegalArgumentException.class, () -> ruleAdminService.shift(1, -1))
                        .getMessage();
        assertThat(msg).contains("Positive offset required");
    }

    private void assertPriorities(List<Rule> expectedOrder, List<Integer> expectedPriorities) {
        assertThat(expectedOrder.size())
                .as("mismatch in expectations")
                .isEqualTo(expectedPriorities.size());
        List<Rule> all = ruleAdminService.getAll().collect(Collectors.toList());
        assertThat(all.size()).isEqualTo(expectedPriorities.size());

        for (int i = 0; i < expectedOrder.size(); i++) {
            int p = expectedPriorities.get(i);
            Rule expected = expectedOrder.get(i).withPriority(p);
            Rule actual = all.get(i);
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void testSwapPriority() {
        Rule r1 = addOneForLayer(1);
        Rule r2 = addOneForLayer(2);
        Rule r3 = addOneForLayer(3);
        Rule r4 = addOneForLayer(4);
        Rule r5 = addOneForLayer(5);
        Rule deleted = addOneForLayer(6);
        ruleAdminService.delete(deleted.getId());

        String reason =
                assertThrows(
                                IllegalArgumentException.class,
                                () -> ruleAdminService.swapPriority(deleted.getId(), r1.getId()))
                        .getMessage();
        assertThat(reason).contains("Rule " + deleted.getId() + " does not exist");
        reason =
                assertThrows(
                                IllegalArgumentException.class,
                                () -> ruleAdminService.swapPriority(r1.getId(), deleted.getId()))
                        .getMessage();
        assertThat(reason).contains("Rule " + deleted.getId() + " does not exist");

        final List<Integer> expectedPriorities = List.of(1, 2, 3, 4, 5);
        assertPriorities(List.of(r1, r2, r3, r4, r5), expectedPriorities);

        ruleAdminService.swapPriority(r1.getId(), r5.getId());
        assertPriorities(List.of(r5, r2, r3, r4, r1), expectedPriorities);

        ruleAdminService.swapPriority(r3.getId(), r2.getId());
        assertPriorities(List.of(r5, r3, r2, r4, r1), expectedPriorities);
    }

    private AbstractObjectAssert<?, Rule> assertGet(Rule rule) {
        return assertThat(get(rule)).isPresent().get();
    }

    private Optional<Rule> get(Rule r) {
        return get(r.getId());
    }

    private Optional<Rule> get(String id) {
        return ruleAdminService.get(id);
    }

    private RuleLimits sampleLimits() {
        RuleLimits limits =
                RuleLimits.builder()
                        .allowedArea(world())
                        .catalogMode(CatalogMode.CHALLENGE)
                        .spatialFilterType(SpatialFilterType.CLIP)
                        .build();
        return limits;
    }

    private MultiPolygon<?> world() {
        return (MultiPolygon<?>)
                Wkt.fromWkt(
                        "SRID=4326;MULTIPOLYGON (((-180 -90, -180 90, 180 90, 180 -90, -180 -90)))");
    }

    private LayerDetails sampleDetails(int i) {
        return LayerDetails.builder()
                .area(world())
                .allowedStyles(Set.of("s-" + i + "-1", "s-" + i + "-2"))
                .attributes(sampleAttributes())
                .catalogMode(CatalogMode.MIXED)
                .cqlFilterRead("intAtt = 1")
                .cqlFilterWrite("stringAtt = 'Foo'")
                .defaultStyle("def_style_" + i)
                .spatialFilterType(SpatialFilterType.CLIP)
                .type(LayerType.VECTOR)
                .build();
    }

    private Set<LayerAttribute> sampleAttributes() {
        LayerAttribute att1 =
                LayerAttribute.builder().name("intAtt").access(AccessType.READONLY).build();
        LayerAttribute att2 = att1.withName("stringAtt");
        return Set.of(att1, att2);
    }
}
