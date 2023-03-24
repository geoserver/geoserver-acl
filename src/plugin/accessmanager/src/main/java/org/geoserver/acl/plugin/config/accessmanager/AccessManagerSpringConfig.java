package org.geoserver.acl.plugin.config.accessmanager;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.plugin.accessmanager.ACLDispatcherCallback;
import org.geoserver.acl.plugin.accessmanager.ACLResourceAccessManager;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfigProvider;
import org.geoserver.acl.plugin.accessmanager.wps.WPSHelper;
import org.geoserver.catalog.Catalog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessManagerSpringConfig {

    @Bean
    public ACLResourceAccessManager aclAccessManager(
            AuthorizationService aclService,
            @Qualifier("rawCatalog") Catalog catalog,
            AccessManagerConfigProvider configProvider,
            WPSHelper wpsHelper) {
        return new ACLResourceAccessManager(aclService, catalog, configProvider, wpsHelper);
    }

    @Bean
    public ACLDispatcherCallback aclDispatcherCallback(
            AuthorizationService aclAuthorizationService,
            @Qualifier("rawCatalog") Catalog catalog,
            AccessManagerConfigProvider configProvider) {
        return new ACLDispatcherCallback(aclAuthorizationService, catalog, configProvider);
    }

    @Bean
    WPSHelper aclWpsHelper(AuthorizationService aclAuthService) {
        return new WPSHelper(aclAuthService);
    }
}
