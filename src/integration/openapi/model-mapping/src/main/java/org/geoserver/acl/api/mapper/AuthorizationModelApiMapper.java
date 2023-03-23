/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.api.model.AccessInfo;
import org.geoserver.acl.api.model.AccessRequest;
import org.geoserver.acl.api.model.AdminAccessInfo;
import org.geoserver.acl.api.model.AdminAccessRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {GeometryApiMapper.class, RuleFilterApiMapper.class})
public interface AuthorizationModelApiMapper {

    AccessRequest toApi(org.geoserver.acl.model.authorization.AccessRequest request);

    org.geoserver.acl.model.authorization.AccessRequest toModel(AccessRequest request);

    AccessInfo toApi(org.geoserver.acl.model.authorization.AccessInfo grant);

    org.geoserver.acl.model.authorization.AccessInfo toModel(AccessInfo grant);

    AdminAccessRequest toApi(org.geoserver.acl.model.authorization.AdminAccessRequest request);

    org.geoserver.acl.model.authorization.AdminAccessRequest toModel(AdminAccessRequest request);

    AdminAccessInfo toApi(org.geoserver.acl.model.authorization.AdminAccessInfo grant);

    org.geoserver.acl.model.authorization.AdminAccessInfo toModel(AdminAccessInfo grant);
}
