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

    AccessRequest toApi(org.geoserver.acl.authorization.AccessRequest request);

    org.geoserver.acl.authorization.AccessRequest toModel(AccessRequest request);

    AccessInfo toApi(org.geoserver.acl.authorization.AccessInfo grant);

    org.geoserver.acl.authorization.AccessInfo toModel(AccessInfo grant);

    AdminAccessRequest toApi(org.geoserver.acl.authorization.AdminAccessRequest request);

    org.geoserver.acl.authorization.AdminAccessRequest toModel(AdminAccessRequest request);

    AdminAccessInfo toApi(org.geoserver.acl.authorization.AdminAccessInfo grant);

    org.geoserver.acl.authorization.AdminAccessInfo toModel(AdminAccessInfo grant);
}
