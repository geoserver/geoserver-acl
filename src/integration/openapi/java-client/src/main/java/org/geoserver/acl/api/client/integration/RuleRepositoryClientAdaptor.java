/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import static org.geoserver.acl.api.client.integration.ClientExceptionHelper.reason;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.api.client.DataRulesApi;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleFilterApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.domain.filter.RuleQuery;
import org.geoserver.acl.domain.rules.InsertPosition;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.domain.rules.RuleIdentifierConflictException;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.geoserver.acl.domain.rules.RuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RuleRepositoryClientAdaptor implements RuleRepository {

    private final DataRulesApi apiClient;
    private final RuleApiMapper mapper = Mappers.ruleApiMapper();
    private final EnumsApiMapper enumsMapper = Mappers.enumsApiMapper();
    private final RuleLimitsApiMapper limitsMapper = Mappers.ruleLimitsApiMapper();
    private final LayerDetailsApiMapper detailsMapper = Mappers.layerDetailsApiMapper();

    private final RuleFilterApiMapper filterMapper = Mappers.ruleFilterApiMapper();

    @Override
    public boolean existsById(@NonNull String id) {
        return apiClient.ruleExistsById(id);
    }

    @Override
    public Rule save(Rule rule) {
        Objects.requireNonNull(rule.getId(), "Rule has no id");
        try {
            org.geoserver.acl.api.model.Rule response;
            response = apiClient.updateRuleById(rule.getId(), map(rule));
            return map(response);
        } catch (HttpClientErrorException.Conflict e) {
            throw new RuleIdentifierConflictException(reason(e), e);
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public Rule create(Rule rule, InsertPosition position) {
        if (null != rule.getId()) throw new IllegalArgumentException("Rule must have no id");
        org.geoserver.acl.api.model.Rule response;
        try {
            response = apiClient.createRule(map(rule), enumsMapper.map(position));
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        } catch (HttpClientErrorException.Conflict c) {
            throw new RuleIdentifierConflictException(reason(c), c);
        }
        return map(response);
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
    public int count() {
        return apiClient.countAllRules();
    }

    @Override
    public int count(RuleFilter filter) {
        return apiClient.countRules(map(filter));
    }

    @Override
    public Stream<Rule> findAll() {
        List<org.geoserver.acl.api.model.Rule> rules = apiClient.getRules(null, null);
        return rules.stream().map(this::map);
    }

    @Override
    public Stream<Rule> findAll(RuleQuery<RuleFilter> query) {

        org.geoserver.acl.api.model.RuleFilter filter =
                query.getFilter().map(filterMapper::toApi).orElse(null);

        Integer limit = query.getLimit();
        String nextCursor = query.getNextId();
        List<org.geoserver.acl.api.model.Rule> rules =
                apiClient.queryRules(limit, nextCursor, filter);

        return rules.stream().map(this::map);
    }

    @Override
    public Optional<Rule> findById(@NonNull String id) {
        org.geoserver.acl.api.model.Rule rule;
        try {
            rule = apiClient.getRuleById(id);
        } catch (HttpClientErrorException.NotFound e) {
            rule = null;
        }
        return Optional.ofNullable(rule).map(this::map);
    }

    @Override
    public Optional<Rule> findOneByPriority(long priority) {
        org.geoserver.acl.api.model.Rule rule;
        try {
            rule = apiClient.findOneRuleByPriority(priority);
        } catch (HttpClientErrorException.NotFound e) {
            rule = null;
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException("Found multiple rules with priority " + priority);
        }
        return Optional.ofNullable(rule).map(this::map);
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
            apiClient.setRuleLimits(ruleId, limitsMapper.toApi(limits));
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public void setLayerDetails(@NonNull String ruleId, LayerDetails detailsNew) {
        try {
            apiClient.setRuleLayerDetails(ruleId, detailsMapper.map(detailsNew));
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        }
    }

    @Override
    public Optional<LayerDetails> findLayerDetailsByRuleId(@NonNull String ruleId) {
        ResponseEntity<org.geoserver.acl.api.model.LayerDetails> response;
        try {
            response = apiClient.getLayerDetailsByRuleIdWithHttpInfo(ruleId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Rule does not exist", e);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalArgumentException(reason(e), e);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        int statusCode = response.getStatusCodeValue();
        if (OK.value() == statusCode) {
            return Optional.of(detailsMapper.map(response.getBody()));
        } else if (NO_CONTENT.value() == statusCode) {
            return Optional.empty();
        }
        throw new IllegalStateException("Unexpected response status code: " + statusCode);
    }

    private org.geoserver.acl.api.model.RuleFilter map(RuleFilter filter) {
        return filterMapper.toApi(filter);
    }

    private org.geoserver.acl.api.model.Rule map(Rule rule) {
        return mapper.toApi(rule);
    }

    private Rule map(org.geoserver.acl.api.model.Rule rule) {
        return mapper.toModel(rule);
    }
}
