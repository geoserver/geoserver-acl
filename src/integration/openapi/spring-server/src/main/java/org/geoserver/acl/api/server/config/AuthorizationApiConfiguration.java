/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.server.config;

import org.geoserver.acl.api.mapper.AuthorizationModelApiMapper;
import org.geoserver.acl.api.mapper.RuleApiMapper;
import org.geoserver.acl.api.server.AuthorizationApiController;
import org.geoserver.acl.api.server.AuthorizationApiDelegate;
import org.geoserver.acl.api.server.authorization.AuthorizationApiImpl;
import org.geoserver.acl.api.server.support.AuthorizationApiSupport;
import org.geoserver.acl.model.authorization.AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.NativeWebRequest;

@Configuration(proxyBeanMethods = false)
@Import({ApiObjectModelMappersConfiguration.class, JacksonObjectMapperConfiguration.class})
public class AuthorizationApiConfiguration {

    @Bean
    AuthorizationApiController aclAuthorizationApiController(AuthorizationApiDelegate delegate) {
        return new AuthorizationApiController(delegate);
    }

    @Bean
    AuthorizationApiDelegate aclAuthorizationApiImpl(
            AuthorizationService service, AuthorizationApiSupport support) {
        return new AuthorizationApiImpl(service, support);
    }

    @Bean
    AuthorizationApiSupport aclAuthorizationApiSupport(
            NativeWebRequest nativeReq,
            AuthorizationModelApiMapper mapper,
            RuleApiMapper rulesMapper) {

        return new AuthorizationApiSupport(nativeReq, mapper, rulesMapper);
    }
}
