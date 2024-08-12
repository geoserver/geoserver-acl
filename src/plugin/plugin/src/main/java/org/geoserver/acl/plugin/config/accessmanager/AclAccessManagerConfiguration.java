/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.config.accessmanager;

import org.geoserver.acl.plugin.accessmanager.ACLResourceAccessManager;
import org.geoserver.acl.plugin.config.configmanager.AclConfigurationManagerConfiguration;
import org.geoserver.acl.plugin.config.domain.client.ApiClientAclDomainServicesConfiguration;
import org.geoserver.security.ResourceAccessManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link Configuration @Configuration} for the GeoServer Access Control List {@link
 * ACLResourceAccessManager}.
 *
 * <p>{@link ACLResourceAccessManager} implements GeoServer {@link ResourceAccessManager} by
 * delegating resource access requests to the GeoServer ACL service.
 *
 * @since 1.0
 * @see AclConfigurationManagerConfiguration
 * @see ApiClientAclDomainServicesConfiguration
 * @see AccessManagerSpringConfig
 */
@Configuration
@Import({ //
    AclConfigurationManagerConfiguration.class, //
    ApiClientAclDomainServicesConfiguration.class, //
    AccessManagerSpringConfig.class
})
public class AclAccessManagerConfiguration {}
