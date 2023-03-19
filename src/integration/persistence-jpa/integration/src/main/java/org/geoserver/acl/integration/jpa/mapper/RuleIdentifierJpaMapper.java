/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import org.geoserver.acl.model.rules.RuleIdentifier;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        // in case something changes in the model, make the code generation fail so we make sure the
        // mapper stays in sync
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {IPAddressRangeJpaMapper.class})
abstract class RuleIdentifierJpaMapper {

    static final String ANY = org.geoserver.acl.jpa.model.RuleIdentifier.ANY;

    @Mapping(target = "instanceName", expression = "java(i.instance())")
    @Mapping(target = "username", expression = "java(i.username())")
    @Mapping(target = "rolename", expression = "java(i.rolename())")
    @Mapping(target = "service", expression = "java(i.service())")
    @Mapping(target = "request", expression = "java(i.request())")
    @Mapping(target = "subfield", expression = "java(i.subfield())")
    @Mapping(target = "workspace", expression = "java(i.workspace())")
    @Mapping(target = "layer", expression = "java(i.layer())")
    public abstract RuleIdentifier toModel(org.geoserver.acl.jpa.model.RuleIdentifier i);

    @Mapping(target = "instance", source = "instanceName", defaultValue = ANY)
    @Mapping(target = "username", defaultValue = ANY)
    @Mapping(target = "rolename", defaultValue = ANY)
    @Mapping(target = "service", defaultValue = ANY)
    @Mapping(target = "request", defaultValue = ANY)
    @Mapping(target = "subfield", defaultValue = ANY)
    @Mapping(target = "workspace", defaultValue = ANY)
    @Mapping(target = "layer", defaultValue = ANY)
    public abstract org.geoserver.acl.jpa.model.RuleIdentifier toEntity(RuleIdentifier i);
}
