/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.support;

import lombok.NonNull;
import org.geoserver.acl.api.mapper.LayerDetailsApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.mapper.RuleLimitsApiMapper;
import org.geoserver.acl.api.model.Rule;
import org.geoserver.acl.domain.rules.LayerDetails;
import org.geoserver.acl.domain.rules.RuleLimits;
import org.springframework.web.context.request.NativeWebRequest;

public class DataRulesApiSupport extends ApiImplSupport<Rule, org.geoserver.acl.domain.rules.Rule> {

    private final @NonNull LayerDetailsApiMapper layerDetailsMapper;
    private final @NonNull RuleLimitsApiMapper limitsMapper;

    public DataRulesApiSupport(
            @NonNull NativeWebRequest nativeRequest,
            RuleApiMapper mapper,
            LayerDetailsApiMapper layerDetailsMapper,
            RuleLimitsApiMapper limitsMapper) {

        super(nativeRequest, mapper::toApi, mapper::toModel);
        this.layerDetailsMapper = layerDetailsMapper;
        this.limitsMapper = limitsMapper;
    }

    public RuleLimits toModel(org.geoserver.acl.api.model.RuleLimits ruleLimits) {
        return limitsMapper.toModel(ruleLimits);
    }

    public LayerDetails toModel(org.geoserver.acl.api.model.LayerDetails layerDetails) {
        return layerDetailsMapper.map(layerDetails);
    }

    public org.geoserver.acl.api.model.LayerDetails toApi(LayerDetails layerDetails) {
        return layerDetailsMapper.map(layerDetails);
    }
}
