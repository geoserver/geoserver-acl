/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import static org.geoserver.acl.api.client.integration.ClientExceptionHelper.reason;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.adminrules.AdminRuleIdentifierConflictException;
import org.geoserver.acl.adminrules.AdminRuleRepository;
import org.geoserver.acl.api.client.AdminRulesApi;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.geoserver.acl.api.mapper.RuleFilterApiMapper;
import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.filter.AdminRuleFilter;
import org.geoserver.acl.model.filter.RuleQuery;
import org.geoserver.acl.model.rules.InsertPosition;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class AdminRuleRepositoryClientAdaptor implements AdminRuleRepository {

    private final AdminRulesApi apiClient;
    private final AdminRuleApiMapper mapper;
    private final EnumsApiMapper enumsMapper;
    private final RuleFilterApiMapper filterMapper = new RuleFilterApiMapper();

    @Override
    public AdminRule create(AdminRule rule, InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("AdminRule must have no id");

        try {
            org.geoserver.acl.api.model.AdminRule result =
                    apiClient.createAdminRule(map(rule), map(position));
            return mapper.toModel(result);
        } catch (HttpClientErrorException.Conflict c) {
            throw new AdminRuleIdentifierConflictException(reason(c), c);
        }
    }

    @Override
    public AdminRule save(AdminRule rule) {
        Objects.requireNonNull(rule.getId(), "AdminRule has no id");
        try {
            org.geoserver.acl.api.model.AdminRule response;
            response = apiClient.updateAdminRule(rule.getId(), mapper.toApi(rule));
            return mapper.toModel(response);
        } catch (HttpClientErrorException.Conflict c) {
            throw new AdminRuleIdentifierConflictException(reason(c), c);
        }
    }

    @Override
    public Optional<AdminRule> findById(@NonNull String id) {
        org.geoserver.acl.api.model.AdminRule rule;
        try {
            rule = apiClient.getAdminRuleById(id);
        } catch (HttpClientErrorException.NotFound e) {
            rule = null;
        }
        return Optional.ofNullable(rule).map(this::map);
    }

    @Override
    public Stream<AdminRule> findAll() {
        Integer limit = null;
        String nextCursor = null;
        return apiClient.findAllAdminRules(limit, nextCursor).stream().map(this::map);
    }

    @Override
    public Stream<AdminRule> findAll(RuleQuery<AdminRuleFilter> query) {
        org.geoserver.acl.api.model.AdminRuleFilter filter =
                query.getFilter().map(filterMapper::map).orElse(null);

        Integer limit = query.getLimit();
        String nextCursor = query.getNextId();
        return apiClient.findAdminRules(limit, nextCursor, filter).stream().map(this::map);
    }

    @Override
    public Optional<AdminRule> findFirst(AdminRuleFilter adminRuleFilter) {

        org.geoserver.acl.api.model.AdminRule found;
        try {
            found = apiClient.findFirstAdminRule(map(adminRuleFilter));
        } catch (HttpClientErrorException.NotFound e) {
            found = null;
        }

        return Optional.ofNullable(found).map(this::map);
    }

    @Override
    public int count() {
        return apiClient.countAllAdminRules();
    }

    @Override
    public int count(AdminRuleFilter filter) {
        return apiClient.countAdminRules(map(filter));
    }

    @Override
    public int shiftPriority(long priorityStart, long offset) {
        return apiClient.shiftAdminRulesByPiority(priorityStart, offset);
    }

    @Override
    public void swap(@NonNull String id1, @NonNull String id2) {
        apiClient.swapAdminRules(id1, id2);
    }

    @Override
    public boolean deleteById(@NonNull String id) {
        try {
            apiClient.deleteAdminRuleById(id);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public Optional<AdminRule> findOneByPriority(long priority) {
        org.geoserver.acl.api.model.AdminRule found;
        try {
            found = apiClient.findOneAdminRuleByPriority(priority);
        } catch (HttpClientErrorException.NotFound e) {
            found = null;
        }
        return Optional.ofNullable(found).map(this::map);
    }

    private org.geoserver.acl.api.model.AdminRuleFilter map(AdminRuleFilter filter) {
        return filterMapper.map(filter);
    }

    private org.geoserver.acl.api.model.AdminRule map(AdminRule rule) {
        return mapper.toApi(rule);
    }

    private AdminRule map(org.geoserver.acl.api.model.AdminRule rule) {
        return mapper.toModel(rule);
    }

    private org.geoserver.acl.api.model.InsertPosition map(InsertPosition position) {
        return enumsMapper.map(position);
    }
}
