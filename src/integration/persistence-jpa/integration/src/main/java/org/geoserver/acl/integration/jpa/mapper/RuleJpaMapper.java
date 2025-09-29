/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.integration.jpa.mapper;

import java.util.Optional;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleIdentifier;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {IPAddressRangeJpaMapper.class},
        // in case something changes in the model, make the code generation fail so we
        // make sure the
        // mapper stays in sync
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RuleJpaMapper {
    static final String ANY = org.geoserver.acl.jpa.model.RuleIdentifier.ANY;

    @Mapping(target = "username", expression = "java(i.username())")
    @Mapping(target = "rolename", expression = "java(i.rolename())")
    @Mapping(target = "service", expression = "java(i.service())")
    @Mapping(target = "request", expression = "java(i.request())")
    @Mapping(target = "subfield", expression = "java(i.subfield())")
    @Mapping(target = "workspace", expression = "java(i.workspace())")
    @Mapping(target = "layer", expression = "java(i.layer())")
    public abstract RuleIdentifier toModel(org.geoserver.acl.jpa.model.RuleIdentifier i);

    @Mapping(target = "username", defaultValue = ANY)
    @Mapping(target = "rolename", defaultValue = ANY)
    @Mapping(target = "service", defaultValue = ANY)
    @Mapping(target = "request", defaultValue = ANY)
    @Mapping(target = "subfield", defaultValue = ANY)
    @Mapping(target = "workspace", defaultValue = ANY)
    @Mapping(target = "layer", defaultValue = ANY)
    public abstract org.geoserver.acl.jpa.model.RuleIdentifier toEntity(RuleIdentifier i);

    Rule toModel(org.geoserver.acl.jpa.model.Rule entity);

    @Mapping(target = "layerDetails", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    org.geoserver.acl.jpa.model.Rule toEntity(Rule model);

    @Mapping(target = "layerDetails", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(@MappingTarget org.geoserver.acl.jpa.model.Rule entity, Rule model);

    default org.geoserver.acl.jpa.model.LayerDetails toEntity(Optional<LayerDetails> value) {
        return value == null ? null : value.map(this::toEntity).orElse(null);
    }

    org.geoserver.acl.jpa.model.LayerDetails toEntity(org.geoserver.acl.domain.rules.LayerDetails value);

    org.geoserver.acl.domain.rules.LayerDetails toModel(org.geoserver.acl.jpa.model.LayerDetails value);

    org.geoserver.acl.jpa.model.RuleLimits toEntity(org.geoserver.acl.domain.rules.RuleLimits value);

    org.geoserver.acl.domain.rules.RuleLimits toModel(org.geoserver.acl.jpa.model.RuleLimits value);

    static String encodeId(Long id) {
        return id == null ? null : Long.toHexString(id);
    }

    static Long decodeId(String id) {
        try {
            return id == null ? null : Long.decode("0x" + id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
    }
}
