/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface OptionalApiMapper {

    default <T> Optional<T> wrapOptional(T object) {
        return Optional.ofNullable(object);
    }

    default <T> T unwrapOptional(Optional<T> object) {
        return object == null ? null : object.orElse(null);
    }
}
