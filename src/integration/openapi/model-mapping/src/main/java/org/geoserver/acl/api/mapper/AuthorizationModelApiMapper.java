/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.api.model.AccessInfo;
import org.geoserver.acl.api.model.AccessRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {GeometryApiMapper.class, RuleFilterApiMapper.class})
public interface AuthorizationModelApiMapper {

    @Mapping(target = "username", source = "user.name")
    @Mapping(target = "roles", source = "user.roles")
    AccessRequest toApi(org.geoserver.acl.model.authorization.AccessRequest request);

    @Mapping(target = "user.name", source = "username")
    @Mapping(target = "user.roles", source = "roles")
    org.geoserver.acl.model.authorization.AccessRequest toModel(AccessRequest request);

    AccessInfo toApi(org.geoserver.acl.model.authorization.AccessInfo grant);

    org.geoserver.acl.model.authorization.AccessInfo toModel(AccessInfo grant);
}
