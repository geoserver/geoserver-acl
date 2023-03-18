/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.rules;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.adminrules.AdminRuleAdminService;
import org.geoserver.acl.adminrules.AdminRuleIdentifierConflictException;
import org.geoserver.acl.api.model.AdminRule;
import org.geoserver.acl.api.model.AdminRuleFilter;
import org.geoserver.acl.api.model.InsertPosition;
import org.geoserver.acl.api.server.AdminRulesApiDelegate;
import org.geoserver.acl.api.server.support.AdminRulesApiSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AdminRulesApiImpl implements AdminRulesApiDelegate {

    private final @NonNull AdminRuleAdminService service;
    private final @NonNull AdminRulesApiSupport support;

    public @Override ResponseEntity<Integer> countAllAdminRules() {
        return ResponseEntity.ok(service.count());
    }

    public @Override ResponseEntity<Integer> countAdminRules(AdminRuleFilter adminRuleFilter) {
        return ResponseEntity.ok(service.count(support.map(adminRuleFilter)));
    }

    public @Override ResponseEntity<AdminRule> createAdminRule(
            AdminRule adminRule, InsertPosition position) {

        org.geoserver.acl.model.adminrules.AdminRule rule;
        if (position == null) {
            rule = service.insert(support.toModel(adminRule));
        } else {
            rule = service.insert(support.toModel(adminRule), support.toModel(position));
        }
        return ResponseEntity.ok(support.toApi(rule));
    }

    public @Override ResponseEntity<Void> deleteAdminRuleById(@NonNull String id) {
        boolean deleted = service.delete(id);
        return ResponseEntity.status(deleted ? OK : NOT_FOUND).build();
    }

    public @Override ResponseEntity<Boolean> adminRuleExistsById(@NonNull String id) {
        return ResponseEntity.ok(service.exists(id));
    }

    public @Override ResponseEntity<List<AdminRule>> findAllAdminRules(Integer page, Integer size) {

        org.geoserver.acl.model.filter.AdminRuleFilter filter = null;
        List<org.geoserver.acl.model.adminrules.AdminRule> matches;
        matches = service.getList(filter, page, size);

        List<AdminRule> body = matches.stream().map(support::toApi).collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    public @Override ResponseEntity<AdminRule> findFirstAdminRule(AdminRuleFilter adminRuleFilter) {

        org.geoserver.acl.model.adminrules.AdminRule match =
                service.getFirstMatch(support.map(adminRuleFilter)).orElse(null);

        return ResponseEntity.status(null == match ? NOT_FOUND : OK).body(support.toApi(match));
    }

    public @Override ResponseEntity<AdminRule> findOneAdminRuleByPriority(Long priority) {
        Optional<org.geoserver.acl.model.adminrules.AdminRule> found =
                service.getRuleByPriority(priority);

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    public @Override ResponseEntity<List<AdminRule>> findAdminRules(
            Integer page, Integer size, AdminRuleFilter adminRuleFilter) {

        org.geoserver.acl.model.filter.AdminRuleFilter filter = support.map(adminRuleFilter);

        List<org.geoserver.acl.model.adminrules.AdminRule> matches =
                service.getList(filter, page, size);
        return ResponseEntity.ok(matches.stream().map(support::toApi).collect(Collectors.toList()));
    }

    public @Override ResponseEntity<AdminRule> getAdminRuleById(@NonNull String id) {

        Optional<org.geoserver.acl.model.adminrules.AdminRule> found = service.get(id);

        return ResponseEntity.status(found.isPresent() ? OK : NOT_FOUND)
                .body(found.map(support::toApi).orElse(null));
    }

    public @Override ResponseEntity<Integer> shiftAdminRulesByPiority(
            @NonNull Long priorityStart, @NonNull Long offset) {
        return ResponseEntity.ok(service.shift(priorityStart, offset));
    }

    public @Override ResponseEntity<Void> swapAdminRules(@NonNull String id, @NonNull String id2) {
        service.swap(id, id2);
        return ResponseEntity.status(OK).build();
    }

    public @Override ResponseEntity<AdminRule> updateAdminRule(
            @NonNull String id, AdminRule patchBody) {
        org.geoserver.acl.model.adminrules.AdminRule rule =
                service.get(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

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

        org.geoserver.acl.model.adminrules.AdminRule patched = support.mergePatch(rule);

        try {
            org.geoserver.acl.model.adminrules.AdminRule updated = service.update(patched);
            return ResponseEntity.status(OK).body(support.toApi(updated));
        } catch (AdminRuleIdentifierConflictException e) {
            return support.error(CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return support.error(BAD_REQUEST, e.getMessage());
        }
    }
}
