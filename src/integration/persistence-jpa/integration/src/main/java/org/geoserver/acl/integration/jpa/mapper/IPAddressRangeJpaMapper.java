/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import org.geoserver.acl.model.rules.IPAddressRange;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        // in case something changes in the model, make the code generation fail so we make sure the
        // mapper stays in sync
        unmappedTargetPolicy = ReportingPolicy.ERROR)
interface IPAddressRangeJpaMapper {

    default org.geoserver.acl.jpa.model.IPAddressRange toEntity(IPAddressRange model) {
        if (null == model) {
            return org.geoserver.acl.jpa.model.IPAddressRange.noData();
        }
        Long low = model.getLow();
        Long high =
                model.getHigh() == null
                        ? org.geoserver.acl.jpa.model.IPAddressRange.NULL
                        : model.getHigh();
        Integer size = model.getSize();
        return new org.geoserver.acl.jpa.model.IPAddressRange(low, high, size);
    }

    default IPAddressRange toModel(org.geoserver.acl.jpa.model.IPAddressRange entity) {
        if (isNotEmpty(entity)) {
            long low = entity.getLow();
            Long high =
                    entity.getHigh() == org.geoserver.acl.jpa.model.IPAddressRange.NULL
                            ? null
                            : entity.getHigh();
            int size = entity.getSize();
            return IPAddressRange.builder().low(low).high(high).size(size).build();
        }
        return null;
    }

    @Condition
    static boolean isNotEmpty(org.geoserver.acl.jpa.model.IPAddressRange jpa) {
        return jpa != null && !jpa.isEmpty();
    }
}
