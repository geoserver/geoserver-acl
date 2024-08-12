/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.wps;

import org.geoserver.acl.plugin.accessmanager.wps.ChainStatusHolder;
import org.geoserver.acl.plugin.wps.DefaultExecutionIdRetriever;
import org.geoserver.acl.plugin.wps.WPSProcessListener;
import org.geoserver.wps.resource.WPSResourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
// spring with no spring boot equivalent to @ConditionalOnClass(WPSResourceManager.class)
@Conditional(value = WPSResourceManagerClassCondition.class)
public class AclWpsIntegrationConfiguration {

    @Bean
    ChainStatusHolder aclWpsChainStatusHolder() {
        return new ChainStatusHolder();
    }

    @Bean
    DefaultExecutionIdRetriever aclWpsExecutionIdRetriever(WPSResourceManager wpsManager) {
        return new DefaultExecutionIdRetriever(wpsManager);
    }

    @Bean
    WPSProcessListener aclWpsProcessListener(ChainStatusHolder statusHolder) {
        return new WPSProcessListener(statusHolder);
    }
}
