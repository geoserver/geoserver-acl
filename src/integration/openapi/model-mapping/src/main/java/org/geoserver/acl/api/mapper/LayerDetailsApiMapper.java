/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.model.rules.LayerDetails;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        // nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {GeometryApiMapper.class, LayerAttributeApiMapper.class, EnumsApiMapper.class})
public abstract class LayerDetailsApiMapper {

    @Mapping(target = "allowedArea", source = "area")
    @Mapping(target = "layerAttributes", source = "attributes")
    public abstract org.geoserver.acl.api.model.LayerDetails map(LayerDetails ld);

    @Mapping(target = "area", source = "allowedArea")
    @Mapping(target = "attributes", source = "layerAttributes")
    public abstract LayerDetails map(org.geoserver.acl.api.model.LayerDetails ld);

    @Mapping(target = "area", source = "allowedArea")
    @Mapping(target = "attributes", source = "layerAttributes")
    public abstract LayerDetails updateDetails(
            @MappingTarget LayerDetails.Builder target,
            org.geoserver.acl.api.model.LayerDetails source);
}
