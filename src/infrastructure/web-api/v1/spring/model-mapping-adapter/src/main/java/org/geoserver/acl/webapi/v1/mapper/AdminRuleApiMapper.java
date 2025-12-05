/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.mapper;

import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {OptionalApiMapper.class, GeometryApiMapper.class, EnumsApiMapper.class})
public interface AdminRuleApiMapper {

    @Mapping(target = "identifier.username", source = "user")
    @Mapping(target = "identifier.rolename", source = "role")
    @Mapping(target = "identifier.workspace", source = "workspace")
    @Mapping(target = "identifier.addressRange", source = "addressRange")
    org.geoserver.acl.domain.adminrules.AdminRule toModel(org.geoserver.acl.webapi.v1.model.AdminRule rule);

    @Mapping(target = "user", source = "identifier.username")
    @Mapping(target = "role", source = "identifier.rolename")
    @Mapping(target = "workspace", source = "identifier.workspace")
    @Mapping(target = "addressRange", source = "identifier.addressRange")
    public abstract org.geoserver.acl.webapi.v1.model.AdminRule toApi(
            org.geoserver.acl.domain.adminrules.AdminRule rule);

    @Mapping(target = "identifier", ignore = true)
    AdminRule updateEntity(@MappingTarget AdminRule.Builder entity, org.geoserver.acl.webapi.v1.model.AdminRule dto);

    @Mapping(target = "username", source = "user")
    @Mapping(target = "rolename", source = "role")
    AdminRuleIdentifier updateIdentifier(
            @MappingTarget AdminRuleIdentifier.Builder entity, org.geoserver.acl.webapi.v1.model.AdminRule dto);

    default AdminRule patch(
            org.geoserver.acl.domain.adminrules.AdminRule target, org.geoserver.acl.webapi.v1.model.AdminRule source) {

        AdminRuleIdentifier identifier = updateIdentifier(target.getIdentifier().toBuilder(), source);

        org.geoserver.acl.domain.adminrules.AdminRule patched = updateEntity(target.toBuilder(), source);

        return patched.withIdentifier(identifier);
    }
}
