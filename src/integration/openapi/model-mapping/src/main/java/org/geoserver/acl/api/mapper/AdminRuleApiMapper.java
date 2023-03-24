/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

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
public abstract class AdminRuleApiMapper {

    @Mapping(target = "identifier.instanceName", source = "instance")
    @Mapping(target = "identifier.username", source = "user")
    @Mapping(target = "identifier.rolename", source = "role")
    @Mapping(target = "identifier.workspace", source = "workspace")
    @Mapping(target = "identifier.addressRange", source = "addressRange")
    public abstract org.geoserver.acl.domain.adminrules.AdminRule toModel(
            org.geoserver.acl.api.model.AdminRule rule);

    @Mapping(target = "instance", source = "identifier.instanceName")
    @Mapping(target = "user", source = "identifier.username")
    @Mapping(target = "role", source = "identifier.rolename")
    @Mapping(target = "workspace", source = "identifier.workspace")
    @Mapping(target = "addressRange", source = "identifier.addressRange")
    public abstract org.geoserver.acl.api.model.AdminRule toApi(
            org.geoserver.acl.domain.adminrules.AdminRule rule);

    @Mapping(target = "identifier", ignore = true)
    abstract AdminRule updateEntity(
            @MappingTarget AdminRule.Builder entity, org.geoserver.acl.api.model.AdminRule dto);

    @Mapping(target = "instanceName", source = "instance")
    @Mapping(target = "username", source = "user")
    @Mapping(target = "rolename", source = "role")
    abstract AdminRuleIdentifier updateIdentifier(
            @MappingTarget AdminRuleIdentifier.Builder entity,
            org.geoserver.acl.api.model.AdminRule dto);

    public AdminRule patch(
            org.geoserver.acl.domain.adminrules.AdminRule target,
            org.geoserver.acl.api.model.AdminRule source) {

        AdminRuleIdentifier identifier =
                updateIdentifier(target.getIdentifier().toBuilder(), source);

        org.geoserver.acl.domain.adminrules.AdminRule patched =
                updateEntity(target.toBuilder(), source);

        return patched.withIdentifier(identifier);
    }
}
