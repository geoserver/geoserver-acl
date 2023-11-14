/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleIdentifier;
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
        uses = {
            EnumsApiMapper.class,
            OptionalApiMapper.class,
            LayerDetailsApiMapper.class,
            GeometryApiMapper.class,
            RuleLimitsApiMapper.class
        })
public abstract class RuleApiMapper {

    @Mapping(target = "identifier.access", source = "access")
    @Mapping(target = "identifier.username", source = "user")
    @Mapping(target = "identifier.rolename", source = "role")
    @Mapping(target = "identifier.service", source = "service")
    @Mapping(target = "identifier.request", source = "request")
    @Mapping(target = "identifier.subfield", source = "subfield")
    @Mapping(target = "identifier.workspace", source = "workspace")
    @Mapping(target = "identifier.layer", source = "layer")
    @Mapping(target = "identifier.addressRange", source = "addressRange")
    @Mapping(target = "ruleLimits", source = "limits")
    public abstract org.geoserver.acl.domain.rules.Rule toModel(
            org.geoserver.acl.api.model.Rule rule);

    @Mapping(target = "access", source = "identifier.access")
    @Mapping(target = "user", source = "identifier.username")
    @Mapping(target = "role", source = "identifier.rolename")
    @Mapping(target = "service", source = "identifier.service")
    @Mapping(target = "request", source = "identifier.request")
    @Mapping(target = "subfield", source = "identifier.subfield")
    @Mapping(target = "workspace", source = "identifier.workspace")
    @Mapping(target = "layer", source = "identifier.layer")
    @Mapping(target = "addressRange", source = "identifier.addressRange")
    @Mapping(target = "limits", source = "ruleLimits")
    public abstract org.geoserver.acl.api.model.Rule toApi(
            org.geoserver.acl.domain.rules.Rule rule);

    @Mapping(target = "identifier", ignore = true)
    @Mapping(target = "ruleLimits", ignore = true)
    abstract Rule updateEntity(
            @MappingTarget Rule.Builder entity, org.geoserver.acl.api.model.Rule dto);

    @Mapping(target = "username", source = "user")
    @Mapping(target = "rolename", source = "role")
    abstract RuleIdentifier updateIdentifier(
            @MappingTarget RuleIdentifier.Builder entity, org.geoserver.acl.api.model.Rule dto);
}
