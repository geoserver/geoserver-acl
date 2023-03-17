/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.model.rules.IPAddressRange;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import javax.annotation.Nullable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IPAddressRangeApiMapper {

    default String addressRangeToString(@Nullable IPAddressRange range) {
        return IPAddressRange.getCidrSignature(range);
    }

    default IPAddressRange stringToAddressRange(@Nullable String range) {
        return IPAddressRange.fromCidrSignature(range);
    }
}
