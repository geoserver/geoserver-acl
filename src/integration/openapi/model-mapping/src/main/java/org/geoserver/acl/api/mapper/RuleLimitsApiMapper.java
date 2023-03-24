/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        //        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GeometryApiMapper.class, EnumsApiMapper.class})
public interface RuleLimitsApiMapper {

    org.geoserver.acl.api.model.RuleLimits toApi(org.geoserver.acl.domain.rules.RuleLimits limits);

    @Mapping(target = "catalogMode", defaultValue = "HIDE")
    @Mapping(target = "spatialFilterType", defaultValue = "INTERSECT")
    org.geoserver.acl.domain.rules.RuleLimits toModel(
            org.geoserver.acl.api.model.RuleLimits limits);
}
