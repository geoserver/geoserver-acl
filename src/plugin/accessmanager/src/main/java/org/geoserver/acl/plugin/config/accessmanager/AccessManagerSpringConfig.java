/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.accessmanager;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.plugin.accessmanager.ACLDispatcherCallback;
import org.geoserver.acl.plugin.accessmanager.ACLResourceAccessManager;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfigProvider;
import org.geoserver.acl.plugin.accessmanager.wps.WPSHelper;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.LocalWorkspaceCatalog;
import org.geoserver.security.impl.LayerGroupContainmentCache;
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
            LayerGroupContainmentCache groupsCache,
            WPSHelper wpsHelper) {

        return new ACLResourceAccessManager(aclService, groupsCache, configProvider, wpsHelper);
    }

    @Bean
    public ACLDispatcherCallback aclDispatcherCallback(
            AuthorizationService aclAuthorizationService,
            @Qualifier("rawCatalog") Catalog catalog,
            AccessManagerConfigProvider configProvider) {

        LocalWorkspaceCatalog localWorkspaceCatalog = new LocalWorkspaceCatalog(catalog);
        return new ACLDispatcherCallback(aclAuthorizationService, localWorkspaceCatalog, configProvider);
    }

    @Bean
    WPSHelper aclWpsHelper(AuthorizationService aclAuthService) {
        return new WPSHelper(aclAuthService);
    }
}
