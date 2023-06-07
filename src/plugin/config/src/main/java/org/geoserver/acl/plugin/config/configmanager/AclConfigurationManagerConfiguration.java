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
import org.springframework.util.StringUtils;

@Configuration
public class AclConfigurationManagerConfiguration {

    @Primary
    @Bean
    AclConfigurationManager aclConfigurationManager(
            AuthorizationService authService, Environment env) {
        String basePath = env.getProperty("geoserver.acl.client.basePath");
        String instanceName = env.getProperty("geoserver.acl.instanceName");
        AclConfigurationManager configManager = new AclConfigurationManager(authService);
        configManager.getConfiguration().setServiceUrl(basePath);
        if (StringUtils.hasText(instanceName)) {
            configManager.getConfiguration().setInstanceName(instanceName);
        }
        return configManager;
    }
}
