/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import org.geoserver.acl.domain.filter.predicate.SubnetV4Utils;
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

    default org.geoserver.acl.jpa.model.IPAddressRange toEntity(String cidrNotation) {
        if (null == cidrNotation) {
            return org.geoserver.acl.jpa.model.IPAddressRange.noData();
        }
        SubnetV4Utils su = new SubnetV4Utils(cidrNotation);
        long low = su.getInfo().getAddressAsInteger();
        int size = su.getInfo().getMaskSize();
        long high = org.geoserver.acl.jpa.model.IPAddressRange.NULL;
        return new org.geoserver.acl.jpa.model.IPAddressRange(low, high, size);
    }

    default String toModel(org.geoserver.acl.jpa.model.IPAddressRange entity) {
        if (isNotEmpty(entity)) {
            long low = entity.getLow();
            int size = entity.getSize();
            SubnetV4Utils su = new SubnetV4Utils(low, size);
            return su.getInfo().getCidrSignature();
        }
        return null;
    }

    @Condition
    static boolean isNotEmpty(org.geoserver.acl.jpa.model.IPAddressRange jpa) {
        return jpa != null && !jpa.isEmpty();
    }
}
