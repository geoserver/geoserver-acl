/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.geoserver.acl.api.server.config.RulesApiConfiguration;
import org.geoserver.acl.api.server.support.DataRulesApiSupport;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.InsertPosition;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SpringBootTest(classes = RulesApiConfiguration.class, properties = "spring.main.banner-mode=off")
class RulesApiImpTest {

    private @MockBean RuleAdminService rules;
    private @MockBean AdminRuleAdminService adminRules;

    private @Autowired DataRulesApiSupport support;
    private @Autowired DataRulesApiImpl api;

    @Test
    void testCreateRule() {
        Rule ret = Rule.allow().withId("1");
        when(rules.insert(eq(Rule.allow()))).thenReturn(ret);

        assertResponse(() -> api.createRule(support.toApi(Rule.allow()), null), CREATED, ret);

        verify(rules, times(1)).insert(eq(Rule.allow()));
        verifyNoMoreInteractions(rules);
        clearInvocations(rules);

        when(rules.insert(any(), any()))
                .thenThrow(new RuleIdentifierConflictException("Duplicate identifier"));

        assertError(create(Rule.deny(), InsertPosition.FROM_END), CONFLICT, "Duplicate identifier");
        verify(rules, times(1)).insert(eq(Rule.deny()), eq(InsertPosition.FROM_END));
    }

    private Supplier<ResponseEntity<org.geoserver.acl.api.model.Rule>> create(
            Rule modelRule, InsertPosition modelPos) {
        return () -> api.createRule(support.toApi(modelRule), support.toApi(modelPos));
    }

    private void assertResponse(
            Supplier<ResponseEntity<org.geoserver.acl.api.model.Rule>> call,
            HttpStatus status,
            Rule expected) {

        ResponseEntity<org.geoserver.acl.api.model.Rule> responseEntity = call.get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        assertThat(responseEntity.getBody()).isEqualTo(support.toApi(expected));
    }

    private <T> void assertError(
            Supplier<ResponseEntity<T>> call, HttpStatus status, String reason) {

        ResponseEntity<T> responseEntity = call.get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        assertThat(responseEntity.getHeaders().get("X-Reason")).singleElement();
        assertThat(responseEntity.getHeaders().get("X-Reason").get(0)).contains(reason);
    }

    @Test
    void testDeleteRuleById() {
        when(rules.delete("id1")).thenReturn(true);
        when(rules.delete("id2")).thenReturn(false);
        assertThat(api.deleteRuleById("id1").getStatusCode()).isEqualTo(OK);
        assertThat(api.deleteRuleById("id2").getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    void testGetRules() {
        RuleQuery<RuleFilter> expectedQuery = RuleQuery.of();
        List<Rule> expected = List.of(Rule.allow(), Rule.deny());
        when(rules.getAll(eq(expectedQuery))).thenReturn(expected.stream());

        List<Rule> actual = assertList(() -> api.getRules(null, null), OK);
        assertThat(actual).isEqualTo(expected);
        verify(rules, times(1)).getAll(eq(expectedQuery));
    }

    private List<Rule> assertList(
            Supplier<ResponseEntity<List<org.geoserver.acl.api.model.Rule>>> call,
            HttpStatus status) {
        ResponseEntity<List<org.geoserver.acl.api.model.Rule>> response = call.get();
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().stream().map(support::toModel).collect(Collectors.toList());
    }

    @Disabled("Filter mapping not yet implemented")
    @Test
    void testQueryRules() {
        // org.geoserver.acl.api.model.RuleFilter filter;
        // api.queryRules(1, 10, 1000, filter);
        fail("Not yet implemented");
    }

    @Test
    void testGetRuleById() {
        Rule found = Rule.allow().withId("id1");
        when(rules.get(eq("id1"))).thenReturn(Optional.of(found));
        when(rules.get(eq("id2"))).thenReturn(Optional.empty());

        assertResponse(() -> api.getRuleById("id1"), OK, found);
        assertResponse(() -> api.getRuleById("id2"), NOT_FOUND, null);
    }

    @Test
    void testFindOneRuleByPriority() {
        Rule found = Rule.allow().withId("id1");
        when(rules.getRuleByPriority(eq(1000L))).thenReturn(Optional.of(found));
        when(rules.getRuleByPriority(eq(1001L))).thenReturn(Optional.empty());

        assertResponse(() -> api.findOneRuleByPriority(1000L), OK, found);
        assertResponse(() -> api.findOneRuleByPriority(1001L), NOT_FOUND, null);
    }

    @Test
    void testCountAllRules() {
        when(rules.count()).thenReturn(37);

        assertThat(api.countAllRules().getStatusCode()).isEqualByComparingTo(OK);
        assertThat(api.countAllRules().getBody()).isEqualByComparingTo(37);
    }

    @Disabled("Filter mapping not yet implemented")
    @Test
    void testCountRules() {
        fail("Not yet implemented");
    }

    @Test
    void testRuleExistsById() {
        Rule found = Rule.allow().withId("id1");
        when(rules.get(eq("id1"))).thenReturn(Optional.of(found));
        when(rules.get(eq("id2"))).thenReturn(Optional.empty());

        assertThat(api.ruleExistsById("id1").getStatusCode()).isEqualByComparingTo(OK);
        assertThat(api.ruleExistsById("id1").getBody()).isTrue();

        assertThat(api.ruleExistsById("id2").getStatusCode()).isEqualByComparingTo(OK);
        assertThat(api.ruleExistsById("id2").getBody()).isFalse();
    }

    @Test
    void testSetRuleAllowedStyles() {
        doThrow(new IllegalArgumentException("message1"))
                .when(rules)
                .setAllowedStyles("id1", Set.of());
        assertError(() -> api.setRuleAllowedStyles("id1", Set.of()), BAD_REQUEST, "message1");
        clearInvocations(rules);

        assertThat(api.setRuleAllowedStyles("id1", Set.of("s1", "s2")).getStatusCode())
                .isEqualTo(OK);

        verify(rules, times(1)).setAllowedStyles(eq("id1"), eq(Set.of("s1", "s2")));
    }
}
