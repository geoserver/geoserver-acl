/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.config.webapi.v1.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({WebapiServerDomainServicesConfiguration.class, WebapiServerApplicationServicesConfiguration.class})
public class ApiServerConfiguration {}
