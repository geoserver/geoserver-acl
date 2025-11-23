/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.webapi.v1.model.AccessInfo;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AccessSummary;
import org.geoserver.acl.webapi.v1.model.AccessSummaryRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessInfo;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@IsAuthenticated
public class AuthorizationApiDelegateImpl implements AuthorizationApiDelegate {

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

    @Override
    public ResponseEntity<AccessSummary> getUserAccessSummary(AccessSummaryRequest request) {
        org.geoserver.acl.authorization.AccessSummaryRequest modelRequest;
        org.geoserver.acl.authorization.AccessSummary modelResponse;

        modelRequest = support.toModel(request);
        modelResponse = service.getUserAccessSummary(modelRequest);
        AccessSummary apiResponse = support.toApi(modelResponse);
        return ResponseEntity.ok(apiResponse);
    }
}
