/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.autoconfigure.accessmanager;

import org.geoserver.acl.plugin.config.accessmanager.AccessManagerSpringConfig;
import org.geoserver.acl.plugin.config.configmanager.AclConfigurationManagerConfiguration;
import org.geoserver.acl.plugin.config.domain.client.ApiClientAclDomainServicesConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * {@link AutoConfiguration @AutoConfiguration} for the GeoServer Access Control List {@link
 * ACLResourceAccessManager}.
 *
 * <p>{@link ACLResourceAccessManager} implements GeoServer {@link ResourceAccessManager} by
 * delegating resource access requests to the GeoServer ACL service.
 *
 * @since 1.0
 */
@AutoConfiguration
@ConditionalOnAclEnabled
@Import({ //
    AclConfigurationManagerConfiguration.class, //
    ApiClientAclDomainServicesConfiguration.class, //
    AccessManagerSpringConfig.class
})
public class AclAccessManagerAutoConfiguration {}
