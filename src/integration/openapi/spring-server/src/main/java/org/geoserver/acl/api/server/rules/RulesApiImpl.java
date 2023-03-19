/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.rules;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.api.model.InsertPosition;
import org.geoserver.acl.api.model.LayerDetails;
import org.geoserver.acl.api.model.Rule;
import org.geoserver.acl.api.model.RuleFilter;
import org.geoserver.acl.api.model.RuleLimits;
import org.geoserver.acl.api.server.RulesApiDelegate;
import org.geoserver.acl.api.server.support.RulesApiSupport;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.rules.RuleAdminService;
import org.geoserver.acl.rules.RuleIdentifierConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RulesApiImpl implements RulesApiDelegate {

    private final @NonNull RuleAdminService service;
    private final @NonNull RulesApiSupport support;

    @Override
    public ResponseEntity<Rule> createRule(@NonNull Rule rule, InsertPosition position) {
        org.geoserver.acl.model.rules.Rule model = support.toModel(rule);
        org.geoserver.acl.model.rules.Rule created;
        try {
            if (null == position) {
                created = service.insert(model);
            } else {
                created = service.insert(model, support.toModel(position));
            }
        } catch (RuleIdentifierConflictException conflict) {
            return error(CONFLICT, conflict.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(support.toApi(created));
    }

    private <T> ResponseEntity<T> error(HttpStatus code, String reason) {
        return ResponseEntity.status(code).header("X-Reason", reason).build();
    }

    @Override
    public ResponseEntity<Void> deleteRuleById(@NonNull String id) {

        boolean deleted = service.delete(id);
        HttpStatus status = deleted ? OK : NOT_FOUND;
        return ResponseEntity.status(status).build();
    }

    @Override
    public ResponseEntity<List<Rule>> getRules(Integer page, Integer size) {
        return query(RuleQuery.of(page, size));
    }

    @Override
    public ResponseEntity<List<Rule>> queryRules( //
            @Nullable Integer page, @Nullable Integer size, @Nullable RuleFilter ruleFilter) {

        org.geoserver.acl.model.filter.RuleFilter filter = support.map(ruleFilter);

        RuleQuery<org.geoserver.acl.model.filter.RuleFilter> query =
                RuleQuery.of(filter, page, size);

        return query(query);
    }

    private ResponseEntity<List<Rule>> query(
            RuleQuery<org.geoserver.acl.model.filter.RuleFilter> query) {
        List<org.geoserver.acl.model.rules.Rule> list;
        try {
            list = service.getAll(query);
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }

        List<Rule> body = list.stream().map(support::toApi).collect(Collectors.toList());

        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Rule> getRuleById(@NonNull String id) {
        Optional<org.geoserver.acl.model.rules.Rule> found = service.get(id);

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    @Override
    public ResponseEntity<Rule> findOneRuleByPriority(Long priority) {
        Optional<org.geoserver.acl.model.rules.Rule> found;
        try {
            found = service.getRuleByPriority(priority);
        } catch (IllegalStateException multipleResults) {
            return ResponseEntity.status(CONFLICT).build();
        }
        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    @Override
    public ResponseEntity<Integer> countAllRules() {
        return ResponseEntity.ok(service.count());
    }

    @Override
    public ResponseEntity<Integer> countRules(RuleFilter ruleFilter) {
        return ResponseEntity.ok(service.count(support.map(ruleFilter)));
    }

    @Override
    public ResponseEntity<Boolean> ruleExistsById(@NonNull String id) {

        return ResponseEntity.ok(service.get(id).isPresent());
    }

    @Override
    public ResponseEntity<Void> setRuleAllowedStyles(@NonNull String id, Set<String> requestBody) {
        try {
            service.setAllowedStyles(id, requestBody);
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.status(OK).build();
    }

    @Override
    public ResponseEntity<LayerDetails> getLayerDetailsByRuleId(@NonNull String id) {
        try {
            LayerDetails details = service.getLayerDetails(id).map(support::toApi).orElse(null);
            return ResponseEntity.status(details == null ? NO_CONTENT : OK).body(details);
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> setRuleLayerDetails(@NonNull String id, LayerDetails layerDetails) {
        try {
            org.geoserver.acl.model.rules.LayerDetails ld = support.toModel(layerDetails);
            service.setLayerDetails(id, ld);
            return ResponseEntity.status(OK).build();
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> setRuleLimits(@NonNull String id, RuleLimits ruleLimits) {
        try {
            service.setLimits(id, support.toModel(ruleLimits));
            return ResponseEntity.status(NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Integer> shiftRulesByPriority(Long priorityStart, Long offset) {
        try {
            int affectedCount = service.shift(priorityStart, offset);
            return ResponseEntity.ok(affectedCount);
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> swapRules(@NonNull String id, @NonNull String id2) {
        try {
            service.swapPriority(id, id2);
            return ResponseEntity.status(NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Rule> updateRuleById(@NonNull String id, Rule patchBody) {
        final org.geoserver.acl.model.rules.Rule rule = service.get(id).orElse(null);
        if (null == rule) {
            return error(NOT_FOUND, "Rule " + id + " does not exist");
        }
        if (null != patchBody.getId() && !id.equals(patchBody.getId())) {
            return error(
                    BAD_REQUEST,
                    "Request body supplied a different id ("
                            + patchBody.getId()
                            + ") than the requested rule id: "
                            + id);
        }

        org.geoserver.acl.model.rules.Rule patched = support.mergePatch(rule);

        try {
            org.geoserver.acl.model.rules.Rule updated = service.update(patched);
            return ResponseEntity.status(OK).body(support.toApi(updated));
        } catch (RuleIdentifierConflictException e) {
            return error(CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(BAD_REQUEST, e.getMessage());
        }
    }
}
