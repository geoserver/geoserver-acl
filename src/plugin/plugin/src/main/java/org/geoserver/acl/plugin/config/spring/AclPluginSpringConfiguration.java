/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.spring;

import org.geoserver.acl.plugin.config.accessmanager.AclAccessManagerConfiguration;
import org.geoserver.acl.plugin.config.cache.CachingAuthorizationServicePluginConfiguration;
import org.geoserver.acl.plugin.config.condition.AclEnabledCondition;
import org.geoserver.acl.plugin.config.webui.ACLWebUIConfiguration;
import org.geoserver.acl.plugin.config.wps.AclWpsIntegrationConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @since 2.3
 */
@Configuration
@Conditional(AclEnabledCondition.class)
@Import({
    AclAccessManagerConfiguration.class,
    ACLWebUIConfiguration.class,
    AclWpsIntegrationConfiguration.class,
    CachingAuthorizationServicePluginConfiguration.class
})
public class AclPluginSpringConfiguration {}
