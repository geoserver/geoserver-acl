/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.server;

import lombok.NonNull;
import org.geoserver.acl.webapi.v1.mapper.AdminRuleApiMapper;
import org.geoserver.acl.webapi.v1.model.AdminRule;
import org.springframework.web.context.request.NativeWebRequest;

public class WorkspaceAdminRulesApiSupport
        extends ApiImplSupport<AdminRule, org.geoserver.acl.domain.adminrules.AdminRule> {

    public WorkspaceAdminRulesApiSupport(@NonNull NativeWebRequest nativeRequest, AdminRuleApiMapper mapper) {
        super(nativeRequest, mapper::toApi, mapper::toModel);
    }
}
