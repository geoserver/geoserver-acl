/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.mapper;

import org.geoserver.acl.domain.rules.LayerDetails;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GeometryApiMapper.class, LayerAttributeApiMapper.class, EnumsApiMapper.class})
public abstract class LayerDetailsApiMapper {

    @Mapping(target = "allowedArea", source = "area")
    @Mapping(target = "layerAttributes", source = "attributes")
    public abstract org.geoserver.acl.webapi.v1.model.LayerDetails map(LayerDetails ld);

    @Mapping(target = "area", source = "allowedArea")
    @Mapping(target = "attributes", source = "layerAttributes")
    @Mapping(target = "catalogMode", defaultValue = "HIDE")
    @Mapping(target = "spatialFilterType", defaultValue = "INTERSECT")
    public abstract LayerDetails map(org.geoserver.acl.webapi.v1.model.LayerDetails ld);
}
