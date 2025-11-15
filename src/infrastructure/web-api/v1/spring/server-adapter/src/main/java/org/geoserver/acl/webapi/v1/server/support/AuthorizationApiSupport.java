/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server.support;

import lombok.NonNull;
import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.AccessSummaryRequest;
import org.geoserver.acl.webapi.v1.model.AccessInfo;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessInfo;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.geoserver.acl.webapi.v1.model.Rule;
import org.springframework.web.context.request.NativeWebRequest;

public class AuthorizationApiSupport extends ApiImplSupport<AccessInfo, org.geoserver.acl.authorization.AccessInfo> {

    private final RuleApiMapper rulesMapper;
    private final AuthorizationModelApiMapper mapper;

    public AuthorizationApiSupport(
            @NonNull NativeWebRequest nativeRequest,
            @NonNull AuthorizationModelApiMapper mapper,
            @NonNull RuleApiMapper rulesMapper) {

        super(nativeRequest, mapper::toApi, mapper::toModel);
        this.mapper = mapper;
        this.rulesMapper = rulesMapper;
    }

    public Rule toApi(org.geoserver.acl.domain.rules.Rule rule) {
        return rulesMapper.toApi(rule);
    }

    public AccessRequest toApi(org.geoserver.acl.authorization.AccessRequest request) {
        return mapper.toApi(request);
    }

    public org.geoserver.acl.authorization.AccessRequest toModel(AccessRequest request) {
        return mapper.toModel(request);
    }

    public AdminAccessRequest toApi(org.geoserver.acl.authorization.AdminAccessRequest request) {
        return mapper.toApi(request);
    }

    public org.geoserver.acl.authorization.AdminAccessRequest toModel(AdminAccessRequest request) {
        return mapper.toModel(request);
    }

    public AdminAccessInfo toApi(org.geoserver.acl.authorization.AdminAccessInfo access) {
        return mapper.toApi(access);
    }

    public org.geoserver.acl.authorization.AdminAccessInfo toModel(AdminAccessInfo access) {
        return mapper.toModel(access);
    }

    public AccessSummaryRequest toModel(org.geoserver.acl.webapi.v1.model.AccessSummaryRequest request) {
        return mapper.toModel(request);
    }

    public org.geoserver.acl.webapi.v1.model.AccessSummary toApi(AccessSummary response) {
        return mapper.toApi(response);
    }
}
