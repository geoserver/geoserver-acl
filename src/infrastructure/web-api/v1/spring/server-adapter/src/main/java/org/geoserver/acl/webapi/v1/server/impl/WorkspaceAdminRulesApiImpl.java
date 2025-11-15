/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifierConflictException;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.webapi.v1.model.AdminRule;
import org.geoserver.acl.webapi.v1.model.AdminRuleFilter;
import org.geoserver.acl.webapi.v1.model.InsertPosition;
import org.geoserver.acl.webapi.v1.server.WorkspaceAdminRulesApiDelegate;
import org.geoserver.acl.webapi.v1.server.support.IsAdmin;
import org.geoserver.acl.webapi.v1.server.support.IsAuthenticated;
import org.geoserver.acl.webapi.v1.server.support.WorkspaceAdminRulesApiSupport;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@IsAuthenticated
public class WorkspaceAdminRulesApiImpl implements WorkspaceAdminRulesApiDelegate {

    private final @NonNull AdminRuleAdminService service;
    private final @NonNull WorkspaceAdminRulesApiSupport support;

    public @Override ResponseEntity<Integer> countAllAdminRules() {
        return ResponseEntity.ok(service.count());
    }

    public @Override ResponseEntity<Integer> countAdminRules(AdminRuleFilter adminRuleFilter) {
        return ResponseEntity.ok(service.count(support.map(adminRuleFilter)));
    }

    @IsAdmin
    public @Override ResponseEntity<AdminRule> createAdminRule(AdminRule adminRule, InsertPosition position) {

        try {
            org.geoserver.acl.domain.adminrules.AdminRule rule;
            if (position == null) {
                rule = service.insert(support.toModel(adminRule));
            } else {
                rule = service.insert(support.toModel(adminRule), support.toAdminRulesModel(position));
            }
            return ResponseEntity.ok(support.toApi(rule));
        } catch (AdminRuleIdentifierConflictException e) {
            return support.error(CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }

    @IsAdmin
    public @Override ResponseEntity<Void> deleteAdminRuleById(@NonNull String id) {
        boolean deleted = service.delete(id);
        return ResponseEntity.status(deleted ? OK : NOT_FOUND).build();
    }

    @Override
    @IsAdmin
    public ResponseEntity<Integer> deleteAllAdminRules() {
        return ResponseEntity.ok(service.deleteAll());
    }

    public @Override ResponseEntity<Boolean> adminRuleExistsById(@NonNull String id) {
        return ResponseEntity.ok(service.exists(id));
    }

    public @Override ResponseEntity<List<AdminRule>> findAllAdminRules(Integer limit, String nextCursor) {
        return query(RuleQuery.of(limit, nextCursor));
    }

    public @Override ResponseEntity<AdminRule> findFirstAdminRule(AdminRuleFilter adminRuleFilter) {

        org.geoserver.acl.domain.adminrules.AdminRule match =
                service.getFirstMatch(support.map(adminRuleFilter)).orElse(null);

        return ResponseEntity.status(null == match ? NOT_FOUND : OK).body(support.toApi(match));
    }

    public @Override ResponseEntity<AdminRule> findOneAdminRuleByPriority(Long priority) {
        Optional<org.geoserver.acl.domain.adminrules.AdminRule> found = service.getRuleByPriority(priority);

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    public @Override ResponseEntity<List<AdminRule>> findAdminRules(
            Integer limit, String nextCursor, AdminRuleFilter adminRuleFilter) {

        org.geoserver.acl.domain.adminrules.AdminRuleFilter filter = support.map(adminRuleFilter);

        return query(RuleQuery.of(filter, limit, nextCursor));
    }

    private ResponseEntity<List<AdminRule>> query(
            RuleQuery<org.geoserver.acl.domain.adminrules.AdminRuleFilter> query) {

        List<org.geoserver.acl.domain.adminrules.AdminRule> list;

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

        List<AdminRule> body = list.stream().map(support::toApi).toList();
        String nextCursor;
        if (requestedLimit != null && body.size() > requestedLimit) {
            nextCursor = body.get(requestedLimit).getId();
        } else {
            nextCursor = null;
        }
        return ResponseEntity.ok().header("X-ACL-NEXTCURSOR", nextCursor).body(body);
    }

    public @Override ResponseEntity<AdminRule> getAdminRuleById(@NonNull String id) {

        Optional<org.geoserver.acl.domain.adminrules.AdminRule> found = service.get(id);

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    @IsAdmin
    public @Override ResponseEntity<Integer> shiftAdminRulesByPriority(
            @NonNull Long priorityStart, @NonNull Long offset) {
        return ResponseEntity.ok(service.shift(priorityStart, offset));
    }

    @IsAdmin
    public @Override ResponseEntity<Void> swapAdminRules(@NonNull String id, @NonNull String id2) {
        service.swap(id, id2);
        return ResponseEntity.status(OK).build();
    }

    @IsAdmin
    public @Override ResponseEntity<AdminRule> updateAdminRule(@NonNull String id, AdminRule patchBody) {
        org.geoserver.acl.domain.adminrules.AdminRule rule = service.get(id).orElse(null);
        if (null == rule) {
            return support.error(NOT_FOUND, "AdminRule " + id + " does not exist");
        }
        if (null != patchBody.getId() && !id.equals(patchBody.getId())) {
            return support.error(
                    BAD_REQUEST,
                    "Request body supplied a different id ("
                            + patchBody.getId()
                            + ") than the requested rule id: "
                            + id);
        }

        org.geoserver.acl.domain.adminrules.AdminRule patched = support.mergePatch(rule);

        try {
            org.geoserver.acl.domain.adminrules.AdminRule updated = service.update(patched);
            return ResponseEntity.status(OK).body(support.toApi(updated));
        } catch (AdminRuleIdentifierConflictException e) {
            return support.error(CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }
}
