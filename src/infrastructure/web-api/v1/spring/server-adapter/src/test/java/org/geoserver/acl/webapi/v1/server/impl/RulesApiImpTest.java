/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.geoserver.acl.config.webapi.v1.server.ApiServerConfiguration;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.InsertPosition;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.geoserver.acl.webapi.v1.model.SetFilter;
import org.geoserver.acl.webapi.v1.model.TextFilter;
import org.geoserver.acl.webapi.v1.server.DataRulesApiDelegateImpl;
import org.geoserver.acl.webapi.v1.server.DataRulesApiSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ApiServerConfiguration.class, properties = "spring.main.banner-mode=off")
class RulesApiImpTest {

    private @MockitoBean RuleAdminService rules;
    private @MockitoBean AdminRuleAdminService adminRules;

    private @Autowired DataRulesApiSupport support;
    private @Autowired DataRulesApiDelegateImpl api;

    @Test
    void testCreateRule() {
        Rule ret = Rule.allow().withId("1");
        when(rules.insert(Rule.allow())).thenReturn(ret);

        assertResponse(() -> api.createRule(support.toApi(Rule.allow()), null), CREATED, ret);

        verify(rules, times(1)).insert(Rule.allow());
        verifyNoMoreInteractions(rules);
        clearInvocations(rules);

        when(rules.insert(any(), any())).thenThrow(new RuleIdentifierConflictException("Duplicate identifier"));

        assertError(create(Rule.deny(), InsertPosition.FROM_END), CONFLICT, "Duplicate identifier");
        verify(rules, times(1)).insert(Rule.deny(), InsertPosition.FROM_END);
    }

    private Supplier<ResponseEntity<org.geoserver.acl.webapi.v1.model.Rule>> create(
            Rule modelRule, InsertPosition modelPos) {
        return () -> api.createRule(support.toApi(modelRule), support.toApi(modelPos));
    }

    private void assertResponse(
            Supplier<ResponseEntity<org.geoserver.acl.webapi.v1.model.Rule>> call, HttpStatus status, Rule expected) {

        ResponseEntity<org.geoserver.acl.webapi.v1.model.Rule> responseEntity = call.get();
        assertThat(responseEntity.getStatusCode()).isEqualTo(status);
        assertThat(responseEntity.getBody()).isEqualTo(support.toApi(expected));
    }

    private <T> void assertError(Supplier<ResponseEntity<T>> call, HttpStatus status, String reason) {

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
        when(rules.getAll(expectedQuery)).thenReturn(expected.stream());

        List<Rule> actual = assertList(() -> api.getRules(null, null), OK);
        assertThat(actual).isEqualTo(expected);
        verify(rules, times(1)).getAll(expectedQuery);
    }

    private List<Rule> assertList(
            Supplier<ResponseEntity<List<org.geoserver.acl.webapi.v1.model.Rule>>> call, HttpStatus status) {
        ResponseEntity<List<org.geoserver.acl.webapi.v1.model.Rule>> response = call.get();
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().stream().map(support::toModel).toList();
    }

    @Test
    void testQueryRules() {
        org.geoserver.acl.webapi.v1.model.RuleFilter apiFilter = new org.geoserver.acl.webapi.v1.model.RuleFilter();
        apiFilter.setUser(new TextFilter().value("alice"));
        apiFilter.setRoles(new SetFilter().values(Set.of("ROLE_USER")));
        apiFilter.setWorkspace(new TextFilter().value("ws1"));
        apiFilter.setLayer(new TextFilter().value("layer1"));

        RuleFilter expectedFilter = support.map(apiFilter);
        RuleQuery<RuleFilter> expectedQuery = RuleQuery.of(expectedFilter);

        List<Rule> expected = List.of(Rule.allow().withId("r1"), Rule.deny().withId("r2"));
        when(rules.getAll(expectedQuery)).thenReturn(expected.stream());

        List<Rule> actual = assertList(() -> api.queryRules(null, null, apiFilter), OK);
        assertThat(actual).isEqualTo(expected);
        verify(rules, times(1)).getAll(expectedQuery);
    }

    @Test
    void testQueryRules_nullFilterMatchesAll() {
        RuleQuery<RuleFilter> expectedQuery = RuleQuery.of((RuleFilter) null);
        List<Rule> expected = List.of(Rule.allow().withId("r1"));
        when(rules.getAll(expectedQuery)).thenReturn(expected.stream());

        List<Rule> actual = assertList(() -> api.queryRules(null, null, null), OK);
        assertThat(actual).isEqualTo(expected);
        verify(rules, times(1)).getAll(expectedQuery);
    }

    @Test
    void testQueryRules_propagatesBadRequest() {
        when(rules.getAll(any())).thenThrow(new IllegalArgumentException("bad query"));

        ResponseEntity<List<org.geoserver.acl.webapi.v1.model.Rule>> response = api.queryRules(null, null, null);
        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getHeaders().get("X-Reason")).singleElement();
        assertThat(response.getHeaders().get("X-Reason").get(0)).contains("bad query");
    }

    @Test
    void testGetRuleById() {
        Rule found = Rule.allow().withId("id1");
        when(rules.get("id1")).thenReturn(Optional.of(found));
        when(rules.get("id2")).thenReturn(Optional.empty());

        assertResponse(() -> api.getRuleById("id1"), OK, found);
        assertResponse(() -> api.getRuleById("id2"), NOT_FOUND, null);
    }

    @Test
    void testFindOneRuleByPriority() {
        Rule found = Rule.allow().withId("id1");
        when(rules.getRuleByPriority(1000L)).thenReturn(Optional.of(found));
        when(rules.getRuleByPriority(1001L)).thenReturn(Optional.empty());

        assertResponse(() -> api.findOneRuleByPriority(1000L), OK, found);
        assertResponse(() -> api.findOneRuleByPriority(1001L), NOT_FOUND, null);
    }

    @Test
    void testCountAllRules() {
        when(rules.count()).thenReturn(37);

        assertThat(api.countAllRules().getStatusCode()).isEqualTo(OK);
        assertThat(api.countAllRules().getBody()).isEqualByComparingTo(37);
    }

    @Test
    void testCountRules() {
        org.geoserver.acl.webapi.v1.model.RuleFilter apiFilter = new org.geoserver.acl.webapi.v1.model.RuleFilter();
        apiFilter.setUser(new TextFilter().value("alice"));
        apiFilter.setRoles(new SetFilter().values(Set.of("ROLE_USER")));
        apiFilter.setWorkspace(new TextFilter().value("ws1"));

        RuleFilter expectedFilter = support.map(apiFilter);
        when(rules.count(expectedFilter)).thenReturn(42);

        ResponseEntity<Integer> response = api.countRules(apiFilter);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualByComparingTo(42);
        verify(rules, times(1)).count(expectedFilter);
    }

    @Test
    void testCountRules_nullFilterMatchesAll() {
        when(rules.count((RuleFilter) null)).thenReturn(7);

        ResponseEntity<Integer> response = api.countRules(null);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualByComparingTo(7);
        verify(rules, times(1)).count((RuleFilter) null);
    }

    @Test
    void testRuleExistsById() {
        Rule found = Rule.allow().withId("id1");
        when(rules.get("id1")).thenReturn(Optional.of(found));
        when(rules.get("id2")).thenReturn(Optional.empty());

        assertThat(api.ruleExistsById("id1").getStatusCode()).isEqualTo(OK);
        assertThat(api.ruleExistsById("id1").getBody()).isTrue();

        assertThat(api.ruleExistsById("id2").getStatusCode()).isEqualTo(OK);
        assertThat(api.ruleExistsById("id2").getBody()).isFalse();
    }

    @Test
    void testSetRuleAllowedStyles() {
        doThrow(new IllegalArgumentException("message1")).when(rules).setAllowedStyles("id1", Set.of());
        assertError(() -> api.setRuleAllowedStyles("id1", Set.of()), BAD_REQUEST, "message1");
        clearInvocations(rules);

        assertThat(api.setRuleAllowedStyles("id1", Set.of("s1", "s2")).getStatusCode())
                .isEqualTo(OK);

        verify(rules, times(1)).setAllowedStyles("id1", Set.of("s1", "s2"));
    }
}
