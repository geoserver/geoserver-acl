/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.client;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geoserver.acl.authorization.AccessInfo;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.authorization.AdminAccessInfo;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.webapi.v1.client.AuthorizationApi;
import org.geoserver.acl.webapi.v1.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.webapi.v1.mapper.AuthorizationModelApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.RuleApiMapper;

@RequiredArgsConstructor
@Slf4j
class AuthorizationServiceClientAdaptor implements AuthorizationService {

    private final @NonNull AuthorizationApi apiClient;

    private final @NonNull RuleApiMapper ruleMapper = DomainMappers.ruleApiMapper();

    private final @NonNull AuthorizationModelApiMapper mapper =
            new AuthorizationModelApiMapperImpl(DomainMappers.geometryMapper());

    @Override
    public AccessInfo getAccessInfo(@NonNull AccessRequest request) {
        org.geoserver.acl.webapi.v1.model.AccessRequest apiRequest;
        org.geoserver.acl.webapi.v1.model.AccessInfo apiResponse;

        try {
            apiRequest = mapper.toApi(request);
            apiResponse = apiClient.getAccessInfo(apiRequest);
            return mapper.toModel(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting access info for {}", request, e);
            throw e;
        }
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(@NonNull AdminAccessRequest request) {
        org.geoserver.acl.webapi.v1.model.AdminAccessRequest apiRequest;
        org.geoserver.acl.webapi.v1.model.AdminAccessInfo apiResponse;

        try {
            apiRequest = mapper.toApi(request);
            apiResponse = apiClient.getAdminAuthorization(apiRequest);
            return mapper.toModel(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting admin access info for {}", request, e);
            throw e;
        }
    }

    @Override
    public List<Rule> getMatchingRules(@NonNull AccessRequest request) {
        org.geoserver.acl.webapi.v1.model.AccessRequest apiRequest;
        List<org.geoserver.acl.webapi.v1.model.Rule> apiResponse;

        try {
            apiRequest = mapper.toApi(request);
            apiResponse = apiClient.getMatchingRules(apiRequest);
            return apiResponse.stream().map(ruleMapper::toModel).collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error("Error getting matching rules for {}", request, e);
            throw e;
        }
    }

    @Override
    public AccessSummary getUserAccessSummary(AccessSummaryRequest request) {

        org.geoserver.acl.webapi.v1.model.AccessSummaryRequest apiRequest;
        org.geoserver.acl.webapi.v1.model.AccessSummary apiResponse;

        try {
            apiRequest = mapper.toApi(request);
            apiResponse = apiClient.getUserAccessSummary(apiRequest);
            return mapper.toModel(apiResponse);
        } catch (RuntimeException e) {
            log.error("Error getting user access summary for {}", request, e);
            throw e;
        }
    }
}
