/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.support;

import lombok.NonNull;

import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.model.AccessInfo;
import org.geoserver.acl.api.model.AccessRequest;
import org.geoserver.acl.api.model.AdminAccessInfo;
import org.geoserver.acl.api.model.AdminAccessRequest;
import org.geoserver.acl.api.model.Rule;
import org.springframework.web.context.request.NativeWebRequest;

public class AuthorizationApiSupport
        extends ApiImplSupport<AccessInfo, org.geoserver.acl.model.authorization.AccessInfo> {

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

    public Rule toApi(org.geoserver.acl.model.rules.Rule rule) {
        return rulesMapper.toApi(rule);
    }

    public AccessRequest toApi(org.geoserver.acl.model.authorization.AccessRequest request) {
        return mapper.toApi(request);
    }

    public org.geoserver.acl.model.authorization.AccessRequest toModel(AccessRequest request) {
        return mapper.toModel(request);
    }

    public AdminAccessRequest toApi(
            org.geoserver.acl.model.authorization.AdminAccessRequest request) {
        return mapper.toApi(request);
    }

    public org.geoserver.acl.model.authorization.AdminAccessRequest toModel(
            AdminAccessRequest request) {
        return mapper.toModel(request);
    }

    public AdminAccessInfo toApi(org.geoserver.acl.model.authorization.AdminAccessInfo access) {
        return mapper.toApi(access);
    }

    public org.geoserver.acl.model.authorization.AdminAccessInfo toModel(AdminAccessInfo access) {
        return mapper.toModel(access);
    }
}
