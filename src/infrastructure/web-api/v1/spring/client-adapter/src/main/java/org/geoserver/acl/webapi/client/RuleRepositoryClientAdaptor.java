/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.client;

import static org.geoserver.acl.webapi.client.ClientExceptionHelper.reason;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.InsertPosition;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.geoserver.acl.webapi.v1.client.DataRulesApi;
import org.geoserver.acl.webapi.v1.mapper.AclApiModelMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
class RuleRepositoryClientAdaptor implements RuleRepository {

    private final DataRulesApi apiClient;
    private final AclApiModelMapper mapper = new AclApiModelMapper();

    @Override
    public boolean existsById(@NonNull String id) {
        return apiClient.ruleExistsById(id);
    }

    @Override
    public Rule save(Rule rule) {
        Objects.requireNonNull(rule.id(), "Rule has no id");
        try {
            String id = rule.id();
            org.geoserver.acl.webapi.v1.model.Rule apiRule = mapper.toApi(rule);

            org.geoserver.acl.webapi.v1.model.Rule response;
            response = apiClient.updateRuleById(id, apiRule);
            return mapper.toModel(response);
        } catch (HttpClientErrorException.Conflict e) {
            throw new RuleIdentifierConflictException(reason(e), e);
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public Rule create(Rule rule, InsertPosition position) {
        if (null != rule.id()) throw new IllegalArgumentException("Rule must have no id");
        org.geoserver.acl.webapi.v1.model.Rule response;
        try {
            org.geoserver.acl.webapi.v1.model.Rule apiRule = mapper.toApi(rule);
            org.geoserver.acl.webapi.v1.model.InsertPosition apiPosition = mapper.toApi(position);
            response = apiClient.createRule(apiRule, apiPosition);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        } catch (HttpClientErrorException.Conflict c) {
            throw new RuleIdentifierConflictException(reason(c), c);
        }
        return mapper.toModel(response);
    }

    @Override
    public boolean deleteById(@NonNull String id) {
        try {
            apiClient.deleteRuleById(id);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
        return true;
    }

    @Override
    public int deleteAll() {
        return apiClient.deleteAllRules();
    }

    @Override
    public int count() {
        return apiClient.countAllRules();
    }

    @Override
    public int count(RuleFilter filter) {
        org.geoserver.acl.webapi.v1.model.RuleFilter apiFilter = mapper.toApi(filter);
        return apiClient.countRules(apiFilter);
    }

    @Override
    public Stream<Rule> findAll() {
        List<org.geoserver.acl.webapi.v1.model.Rule> rules = apiClient.getRules(null, null);
        return rules.stream().map(mapper::toModel);
    }

    @Override
    public Stream<Rule> findAll(RuleQuery<RuleFilter> query) {

        org.geoserver.acl.webapi.v1.model.RuleFilter filter =
                query.getFilter().map(mapper::toApi).orElse(null);

        Integer limit = query.getLimit();
        String nextCursor = query.getNextId();
        List<org.geoserver.acl.webapi.v1.model.Rule> rules = apiClient.queryRules(limit, nextCursor, filter);

        return rules.stream().map(mapper::toModel);
    }

    @Override
    public Optional<Rule> findById(@NonNull String id) {
        org.geoserver.acl.webapi.v1.model.Rule rule;
        try {
            rule = apiClient.getRuleById(id);
        } catch (HttpClientErrorException.NotFound e) {
            rule = null;
        }
        return Optional.ofNullable(rule).map(mapper::toModel);
    }

    @Override
    public Optional<Rule> findOneByPriority(long priority) {
        org.geoserver.acl.webapi.v1.model.Rule rule;
        try {
            rule = apiClient.findOneRuleByPriority(priority);
        } catch (HttpClientErrorException.NotFound e) {
            rule = null;
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException("Found multiple rules with priority " + priority);
        }
        return Optional.ofNullable(rule).map(mapper::toModel);
    }

    @Override
    public int shift(long priorityStart, long offset) {
        try {
            return apiClient.shiftRulesByPriority(priorityStart, offset);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public void swap(@NonNull String id1, @NonNull String id2) {
        try {
            apiClient.swapRules(id1, id2);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public void setAllowedStyles(@NonNull String ruleId, Set<String> styles) {
        try {
            apiClient.setRuleAllowedStyles(ruleId, styles);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public void setLimits(@NonNull String ruleId, RuleLimits limits) {
        try {
            org.geoserver.acl.webapi.v1.model.RuleLimits apiLimits = mapper.toApi(limits);
            apiClient.setRuleLimits(ruleId, apiLimits);
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public void setLayerDetails(@NonNull String ruleId, LayerDetails detailsNew) {
        try {
            org.geoserver.acl.webapi.v1.model.LayerDetails apiDetails = mapper.toApi(detailsNew);
            apiClient.setRuleLayerDetails(ruleId, apiDetails);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public Optional<LayerDetails> findLayerDetailsByRuleId(@NonNull String ruleId) {
        ResponseEntity<org.geoserver.acl.webapi.v1.model.LayerDetails> response;
        try {
            response = apiClient.getLayerDetailsByRuleIdWithHttpInfo(ruleId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Rule does not exist", e);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        HttpStatusCode statusCode = response.getStatusCode();
        if (OK.value() == statusCode.value()) {
            org.geoserver.acl.webapi.v1.model.@Nullable LayerDetails apiDetails = response.getBody();
            return Optional.of(mapper.toModel(apiDetails));
        }
        if (NO_CONTENT.value() == statusCode.value()) {
            return Optional.empty();
        }
        throw new IllegalStateException("Unexpected response status code: " + statusCode);
    }
}
