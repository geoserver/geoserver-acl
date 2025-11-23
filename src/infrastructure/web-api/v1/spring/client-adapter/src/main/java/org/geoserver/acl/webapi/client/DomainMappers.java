/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.client;

import lombok.experimental.UtilityClass;
import org.geoserver.acl.webapi.v1.mapper.AdminRuleApiMapper;
import org.geoserver.acl.webapi.v1.mapper.AdminRuleApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.EnumsApiMapper;
import org.geoserver.acl.webapi.v1.mapper.EnumsApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.GeometryApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.LayerAttributeApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.webapi.v1.mapper.LayerDetailsApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.RuleApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleApiMapperImpl;
import org.geoserver.acl.webapi.v1.mapper.RuleFilterApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleLimitsApiMapperImpl;

@UtilityClass
class DomainMappers {

    private RuleFilterApiMapper ruleFilterApiMapper;
    private AdminRuleApiMapperImpl adminRuleApiMapper;
    private EnumsApiMapperImpl enumsApiMapper;
    private RuleApiMapperImpl ruleApiMapper;
    private RuleLimitsApiMapperImpl ruleLimitsApiMapper;
    private LayerDetailsApiMapperImpl layerDetailsApiMapper;
    private GeometryApiMapperImpl geometryApiMapper;

    public RuleFilterApiMapper ruleFilterApiMapper() {
        if (null == ruleFilterApiMapper) ruleFilterApiMapper = new RuleFilterApiMapper();
        return ruleFilterApiMapper;
    }

    public AdminRuleApiMapper adminRuleApiMapper() {
        if (null == adminRuleApiMapper) adminRuleApiMapper = new AdminRuleApiMapperImpl(enumsApiMapper());
        return adminRuleApiMapper;
    }

    public EnumsApiMapper enumsApiMapper() {
        if (null == enumsApiMapper) enumsApiMapper = new EnumsApiMapperImpl();
        return enumsApiMapper;
    }

    public RuleApiMapper ruleApiMapper() {
        if (null == ruleApiMapper) ruleApiMapper = new RuleApiMapperImpl(enumsApiMapper(), ruleLimitsApiMapper());
        return ruleApiMapper;
    }

    public RuleLimitsApiMapper ruleLimitsApiMapper() {
        if (null == ruleLimitsApiMapper)
            ruleLimitsApiMapper = new RuleLimitsApiMapperImpl(geometryMapper(), enumsApiMapper());
        return ruleLimitsApiMapper;
    }

    public LayerDetailsApiMapper layerDetailsApiMapper() {
        if (null == layerDetailsApiMapper)
            layerDetailsApiMapper = new LayerDetailsApiMapperImpl(
                    geometryMapper(), new LayerAttributeApiMapperImpl(enumsApiMapper()), enumsApiMapper());
        return layerDetailsApiMapper;
    }

    public GeometryApiMapperImpl geometryMapper() {
        if (null == geometryApiMapper) geometryApiMapper = new GeometryApiMapperImpl();
        return geometryApiMapper;
    }
}
