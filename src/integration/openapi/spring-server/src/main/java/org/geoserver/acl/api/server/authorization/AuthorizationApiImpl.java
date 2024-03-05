/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.authorization;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.acl.api.model.AccessInfo;
import org.geoserver.acl.api.model.AccessRequest;
import org.geoserver.acl.api.model.AdminAccessInfo;
import org.geoserver.acl.api.model.AdminAccessRequest;
import org.geoserver.acl.api.model.Rule;
import org.geoserver.acl.api.server.AuthorizationApiDelegate;
import org.geoserver.acl.api.server.support.AuthorizationApiSupport;
import org.geoserver.acl.api.server.support.IsAuthenticated;
import org.geoserver.acl.authorization.AuthorizationService;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RequiredArgsConstructor
@IsAuthenticated
public class AuthorizationApiImpl implements AuthorizationApiDelegate {

    private final @NonNull AuthorizationService service;
    private final @NonNull AuthorizationApiSupport support;

    @Override
    public ResponseEntity<AccessInfo> getAccessInfo(AccessRequest request) {
        org.geoserver.acl.authorization.AccessRequest modelRequest;
        org.geoserver.acl.authorization.AccessInfo modelResponse;

        modelRequest = support.toModel(request);
        modelResponse = service.getAccessInfo(modelRequest);

        support.setPreferredGeometryEncoding();
        AccessInfo apiResponse = support.toApi(modelResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<AdminAccessInfo> getAdminAuthorization(AdminAccessRequest request) {
        org.geoserver.acl.authorization.AdminAccessRequest modelRequest;
        org.geoserver.acl.authorization.AdminAccessInfo modelResponse;

        modelRequest = support.toModel(request);
        modelResponse = service.getAdminAuthorization(modelRequest);

        support.setPreferredGeometryEncoding();
        AdminAccessInfo apiResponse = support.toApi(modelResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    public ResponseEntity<List<Rule>> getMatchingRules(AccessRequest accessRequest) {
        org.geoserver.acl.authorization.AccessRequest modelRequest;
        List<org.geoserver.acl.domain.rules.Rule> modelResponse;

        modelRequest = support.toModel(accessRequest);
        modelResponse = service.getMatchingRules(modelRequest);

        support.setPreferredGeometryEncoding();
        List<Rule> apiResponse = modelResponse.stream().map(support::toApi).toList();
        return ResponseEntity.ok(apiResponse);
    }
}
