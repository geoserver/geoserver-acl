/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.autoconfigure.wps;

import org.geoserver.acl.plugin.autoconfigure.accessmanager.ConditionalOnAclEnabled;
import org.geoserver.acl.plugin.config.wps.AclWpsIntegrationConfiguration;
import org.geoserver.wps.resource.WPSResourceManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnAclEnabled
@ConditionalOnBean(WPSResourceManager.class)
@Import({AclWpsIntegrationConfiguration.class})
public class AclWpsAutoConfiguration {}
