/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.webapi.v1.server;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.webapi.v1.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.webapi.v1.mapper.RuleApiMapper;
import org.geoserver.acl.webapi.v1.server.AuthorizationApiController;
import org.geoserver.acl.webapi.v1.server.AuthorizationApiDelegate;
import org.geoserver.acl.webapi.v1.server.AuthorizationApiDelegateImpl;
import org.geoserver.acl.webapi.v1.server.AuthorizationApiSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Provides the {@link AuthorizationApiController}
 */
@Configuration(proxyBeanMethods = false)
@Import({ModelMappersConfiguration.class, ObjectMapperConfiguration.class})
class WebapiServerApplicationServicesConfiguration {

    @Bean
    AuthorizationApiController aclAuthorizationApiController(AuthorizationApiDelegate delegate) {
        return new AuthorizationApiController(delegate);
    }

    @Bean
    AuthorizationApiDelegate aclAuthorizationApiImpl(AuthorizationService service, AuthorizationApiSupport support) {
        return new AuthorizationApiDelegateImpl(service, support);
    }

    @Bean
    AuthorizationApiSupport aclAuthorizationApiSupport(
            NativeWebRequest nativeReq, AuthorizationModelApiMapper mapper, RuleApiMapper rulesMapper) {

        return new AuthorizationApiSupport(nativeReq, mapper, rulesMapper);
    }
}
