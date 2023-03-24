/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import org.geoserver.acl.domain.adminrules.AdminRule;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        // in case something changes in the model, make the code generation fail so we make sure the
        // mapper stays in sync
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {AdminRuleIdentifierJpaMapper.class})
public interface AdminRuleJpaMapper {

    public abstract AdminRule toModel(org.geoserver.acl.jpa.model.AdminRule entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    public abstract org.geoserver.acl.jpa.model.AdminRule toEntity(AdminRule model);

    static String encodeId(Long id) {
        return id == null ? null : Long.toHexString(id);
    }

    static Long decodeId(String id) {
        return id == null ? null : Long.decode("0x" + id);
    }

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(@MappingTarget org.geoserver.acl.jpa.model.AdminRule entity, AdminRule model);
}
