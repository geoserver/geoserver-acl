/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.configmanager;

import org.geoserver.acl.authorization.AuthorizationService;
import org.geoserver.acl.plugin.accessmanager.config.AclConfigurationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

@Configuration
public class AclConfigurationManagerConfiguration {

    @Primary
    @Bean
    AclConfigurationManager aclConfigurationManager(AuthorizationService authService, Environment env) {
        String basePath = env.getProperty("geoserver.acl.client.basePath");
        AclConfigurationManager configManager = new AclConfigurationManager(authService);
        configManager.getConfiguration().setServiceUrl(basePath);
        return configManager;
    }
}
