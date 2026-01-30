/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.RuleAdminService;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.geoserver.acl.webapi.v1.model.LayerDetails;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.geoserver.acl.webapi.v1.model.RuleFilter;
import org.geoserver.acl.webapi.v1.model.RuleLimits;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@IsAuthenticated
public class DataRulesApiDelegateImpl implements DataRulesApiDelegate {

    private final @NonNull RuleAdminService service;
    private final @NonNull DataRulesApiSupport support;

    @Override
    @IsAdmin
    public ResponseEntity<Rule> createRule(@NonNull Rule rule, InsertPosition position) {
        org.geoserver.acl.domain.rules.Rule model = support.toModel(rule);
        org.geoserver.acl.domain.rules.Rule created;
        try {
            if (null == position) {
                created = service.insert(model);
            } else {
                created = service.insert(model, support.toRulesModel(position));
            }
        } catch (RuleIdentifierConflictException conflict) {
            return support.error(CONFLICT, conflict.getMessage());
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
        support.setPreferredGeometryEncoding();
        return ResponseEntity.status(HttpStatus.CREATED).body(support.toApi(created));
    }

    @Override
    @IsAdmin
    public ResponseEntity<Void> deleteRuleById(@NonNull String id) {

        boolean deleted = service.delete(id);
        HttpStatus status = deleted ? OK : NOT_FOUND;
        return ResponseEntity.status(status).build();
    }

    @Override
    @IsAdmin
    public ResponseEntity<Integer> deleteAllRules() {
        return ResponseEntity.ok(service.deleteAll());
    }

    @Override
    public ResponseEntity<List<Rule>> getRules(Integer limit, String nextCursor) {
        return query(RuleQuery.of(limit, nextCursor));
    }

    @Override
    public ResponseEntity<List<Rule>> queryRules( //
            @Nullable Integer limit, @Nullable String nextCursor, @Nullable RuleFilter ruleFilter) {

        org.geoserver.acl.domain.rules.RuleFilter filter = support.map(ruleFilter);

        return query(RuleQuery.of(filter, limit, nextCursor));
    }

    private ResponseEntity<List<Rule>> query(RuleQuery<org.geoserver.acl.domain.rules.RuleFilter> query) {
        List<org.geoserver.acl.domain.rules.Rule> list;

        // handle cursor-based pagination.
        final Integer requestedLimit = query.getLimit();
        if (requestedLimit != null) {
            query.setLimit(query.getLimit() + 1);
        }
        try {
            list = service.getAll(query).toList();
            query.setLimit(requestedLimit); // avoid side effect once the method returns
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }

        support.setPreferredGeometryEncoding();
        List<Rule> body = list.stream().map(support::toApi).toList();
        String nextCursor;
        if (requestedLimit != null && body.size() > requestedLimit) {
            nextCursor = body.get(requestedLimit).getId();
            body = body.subList(0, requestedLimit);
        } else {
            nextCursor = null;
        }
        return ResponseEntity.ok().header("X-ACL-NEXTCURSOR", nextCursor).body(body);
    }

    @Override
    public ResponseEntity<Rule> getRuleById(@NonNull String id) {
        Optional<org.geoserver.acl.domain.rules.Rule> found = service.get(id);
        support.setPreferredGeometryEncoding();

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    @Override
    public ResponseEntity<Rule> findOneRuleByPriority(Long priority) {
        Optional<org.geoserver.acl.domain.rules.Rule> found;
        try {
            found = service.getRuleByPriority(priority);
        } catch (IllegalStateException multipleResults) {
            return ResponseEntity.status(CONFLICT).build();
        }
        support.setPreferredGeometryEncoding();
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
    @IsAdmin
    public ResponseEntity<Void> setRuleAllowedStyles(@NonNull String id, Set<String> requestBody) {
        try {
            service.setAllowedStyles(id, requestBody);
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.status(OK).build();
    }

    @Override
    public ResponseEntity<LayerDetails> getLayerDetailsByRuleId(@NonNull String id) {
        try {
            support.setPreferredGeometryEncoding();
            LayerDetails details =
                    service.getLayerDetails(id).map(support::toApi).orElse(null);
            return ResponseEntity.status(details == null ? NO_CONTENT : OK).body(details);
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @IsAdmin
    public ResponseEntity<Void> setRuleLayerDetails(@NonNull String id, LayerDetails layerDetails) {
        try {
            org.geoserver.acl.domain.rules.LayerDetails ld = support.toModel(layerDetails);
            service.setLayerDetails(id, ld);
            return ResponseEntity.status(OK).build();
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @IsAdmin
    public ResponseEntity<Void> setRuleLimits(@NonNull String id, RuleLimits ruleLimits) {
        try {
            service.setLimits(id, support.toModel(ruleLimits));
            return ResponseEntity.status(NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @IsAdmin
    public ResponseEntity<Integer> shiftRulesByPriority(Long priorityStart, Long offset) {
        try {
            int affectedCount = service.shift(priorityStart, offset);
            return ResponseEntity.ok(affectedCount);
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @IsAdmin
    public ResponseEntity<Void> swapRules(@NonNull String id, @NonNull String id2) {
        try {
            service.swapPriority(id, id2);
            return ResponseEntity.status(NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    @IsAdmin
    public ResponseEntity<Rule> updateRuleById(@NonNull String id, Rule patchBody) {
        final org.geoserver.acl.domain.rules.Rule rule = service.get(id).orElse(null);
        if (null == rule) {
            return support.error(NOT_FOUND, "Rule " + id + " does not exist");
        }
        if (null != patchBody.getId() && !id.equals(patchBody.getId())) {
            return support.error(
                    BAD_REQUEST,
                    "Request body supplied a different id ("
                            + patchBody.getId()
                            + ") than the requested rule id: "
                            + id);
        }

        org.geoserver.acl.domain.rules.Rule patched = support.mergePatch(rule);

        try {
            org.geoserver.acl.domain.rules.Rule updated = service.update(patched);
            support.setPreferredGeometryEncoding();
            return ResponseEntity.status(OK).body(support.toApi(updated));
        } catch (RuleIdentifierConflictException e) {
            return support.error(CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }
}
