/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.client.integration;

import lombok.experimental.UtilityClass;
import org.geoserver.acl.api.mapper.AdminRuleApiMapper;
import org.geoserver.acl.api.mapper.AdminRuleApiMapperImpl;
import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.AuthorizationModelApiMapperImpl;
import org.geoserver.acl.api.mapper.EnumsApiMapper;
import org.geoserver.acl.api.mapper.EnumsApiMapperImpl;
import org.geoserver.acl.api.mapper.GeometryApiMapperImpl;
import org.geoserver.acl.api.mapper.LayerAttributeApiMapperImpl;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapperImpl;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapperImpl;
import org.geoserver.acl.api.mapper.RuleFilterApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapperImpl;

@UtilityClass
class Mappers {

    private RuleFilterApiMapper ruleFilterApiMapper;
    private AdminRuleApiMapperImpl adminRuleApiMapper;
    private EnumsApiMapperImpl enumsApiMapper;
    private RuleApiMapperImpl ruleApiMapper;
    private AuthorizationModelApiMapperImpl authorizationModelApiMapper;
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

    public AuthorizationModelApiMapper authorizationModelApiMapper() {
        if (null == authorizationModelApiMapper)
            authorizationModelApiMapper = new AuthorizationModelApiMapperImpl(geometryMapper());
        return authorizationModelApiMapper;
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

    private GeometryApiMapperImpl geometryMapper() {
        if (null == geometryApiMapper) geometryApiMapper = new GeometryApiMapperImpl();
        return geometryApiMapper;
    }
}
