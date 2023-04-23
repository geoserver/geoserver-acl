/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.autoconfigure.webui;

import org.geoserver.acl.plugin.autoconfigure.accessmanager.ConditionalOnAclEnabled;
import org.geoserver.acl.plugin.config.webui.ACLWebUIConfiguration;
import org.geoserver.web.GeoServerBasePage;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnAclEnabled
@ConditionalOnClass(GeoServerBasePage.class)
@ConditionalOnProperty(
        prefix = "geoserver.web-ui.acl",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import({ACLWebUIConfiguration.class})
public class AclWebUIAutoConfiguration {}
