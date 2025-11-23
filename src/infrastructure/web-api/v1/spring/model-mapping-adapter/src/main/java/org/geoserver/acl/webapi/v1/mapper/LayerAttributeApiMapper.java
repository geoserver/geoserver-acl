/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.mapper;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {EnumsApiMapper.class})
public abstract class LayerAttributeApiMapper {

    public abstract org.geoserver.acl.webapi.v1.model.LayerAttribute map(LayerAttribute la);

    public abstract LayerAttribute map(org.geoserver.acl.webapi.v1.model.LayerAttribute la);

    Set<LayerAttribute> unwrapAttributes(Set<org.geoserver.acl.webapi.v1.model.LayerAttribute> value) {

        return Optional.ofNullable(value).orElse(Set.of()).stream()
                .map(this::map)
                .collect(Collectors.toSet());
    }

    Set<org.geoserver.acl.webapi.v1.model.LayerAttribute> wrapAttributes(Set<LayerAttribute> value) {
        if (value == null || value.isEmpty()) return null;

        Set<org.geoserver.acl.webapi.v1.model.LayerAttribute> latts =
                value.stream().map(this::map).collect(Collectors.toSet());
        return latts;
    }
}
