/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import org.geoserver.acl.domain.adminrules.AdminRuleIdentifier;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        // in case something changes in the model, make the code generation fail so we
        // make sure the
        // mapper stays in sync
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {IPAddressRangeJpaMapper.class})
abstract class AdminRuleIdentifierJpaMapper {

    static final String ANY = org.geoserver.acl.jpa.model.AdminRuleIdentifier.ANY;

    @Mapping(target = "instanceName", expression = "java(i.instance())")
    @Mapping(target = "username", expression = "java(i.username())")
    @Mapping(target = "rolename", expression = "java(i.rolename())")
    @Mapping(target = "workspace", expression = "java(i.workspace())")
    public abstract AdminRuleIdentifier toModel(org.geoserver.acl.jpa.model.AdminRuleIdentifier i);

    @Mapping(target = "instance", source = "instanceName", defaultValue = ANY)
    @Mapping(target = "username", defaultValue = ANY)
    @Mapping(target = "rolename", defaultValue = ANY)
    @Mapping(target = "workspace", defaultValue = ANY)
    public abstract org.geoserver.acl.jpa.model.AdminRuleIdentifier toEntity(AdminRuleIdentifier i);
}
