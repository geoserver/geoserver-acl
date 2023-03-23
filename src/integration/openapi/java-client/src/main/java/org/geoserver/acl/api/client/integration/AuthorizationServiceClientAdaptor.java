/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.api.client.AuthorizationApi;
import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.model.authorization.AccessInfo;
import org.geoserver.acl.model.authorization.AccessRequest;
import org.geoserver.acl.model.authorization.AdminAccessInfo;
import org.geoserver.acl.model.authorization.AdminAccessRequest;
import org.geoserver.acl.model.authorization.AuthorizationService;
import org.geoserver.acl.model.rules.Rule;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AuthorizationServiceClientAdaptor implements AuthorizationService {

    private final @NonNull AuthorizationApi apiClient;
    private final @NonNull AuthorizationModelApiMapper mapper;
    private final @NonNull RuleApiMapper ruleMapper;

    @Override
    public AccessInfo getAccessInfo(AccessRequest request) {
        org.geoserver.acl.api.model.AccessRequest apiRequest;
        org.geoserver.acl.api.model.AccessInfo apiResponse;

        apiRequest = mapper.toApi(request);
        apiResponse = apiClient.getAccessInfo(apiRequest);

        return mapper.toModel(apiResponse);
    }

    @Override
    public AdminAccessInfo getAdminAuthorization(AdminAccessRequest request) {
        org.geoserver.acl.api.model.AdminAccessRequest apiRequest;
        org.geoserver.acl.api.model.AdminAccessInfo apiResponse;

        apiRequest = mapper.toApi(request);
        apiResponse = apiClient.getAdminAuthorization(apiRequest);

        return mapper.toModel(apiResponse);
    }

    @Override
    public List<Rule> getMatchingRules(AccessRequest request) {
        org.geoserver.acl.api.model.AccessRequest apiRequest;
        List<org.geoserver.acl.api.model.Rule> apiResponse;

        apiRequest = mapper.toApi(request);
        apiResponse = apiClient.getMatchingRules(apiRequest);

        return apiResponse.stream().map(ruleMapper::toModel).collect(Collectors.toList());
    }
}
