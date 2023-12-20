/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.support;

import lombok.NonNull;

import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.model.AdminRule;
import org.springframework.web.context.request.NativeWebRequest;

public class WorkspaceAdminRulesApiSupport
        extends ApiImplSupport<AdminRule, org.geoserver.acl.domain.adminrules.AdminRule> {

    public WorkspaceAdminRulesApiSupport(
            @NonNull NativeWebRequest nativeRequest, AdminRuleApiMapper mapper) {
        super(nativeRequest, mapper::toApi, mapper::toModel);
    }
}
