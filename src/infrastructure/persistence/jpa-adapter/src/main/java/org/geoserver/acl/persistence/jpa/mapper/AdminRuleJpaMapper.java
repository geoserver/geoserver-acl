/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.persistence.jpa.mapper;

import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
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
        uses = {IPAddressRangeJpaMapper.class})
public interface AdminRuleJpaMapper {
    static final String ANY = org.geoserver.acl.persistence.jpa.model.AdminRuleIdentifier.ANY;

    @Mapping(target = "username", expression = "java(i.username())")
    @Mapping(target = "rolename", expression = "java(i.rolename())")
    @Mapping(target = "workspace", expression = "java(i.workspace())")
    public abstract AdminRuleIdentifier toModel(org.geoserver.acl.persistence.jpa.model.AdminRuleIdentifier i);

    @Mapping(target = "username", defaultValue = ANY)
    @Mapping(target = "rolename", defaultValue = ANY)
    @Mapping(target = "workspace", defaultValue = ANY)
    public abstract org.geoserver.acl.persistence.jpa.model.AdminRuleIdentifier toEntity(AdminRuleIdentifier i);

    public abstract AdminRule toModel(org.geoserver.acl.persistence.jpa.model.AdminRule entity);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    public abstract org.geoserver.acl.persistence.jpa.model.AdminRule toEntity(AdminRule model);

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
    void updateEntity(@MappingTarget org.geoserver.acl.persistence.jpa.model.AdminRule entity, AdminRule model);
}
