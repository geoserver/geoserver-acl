/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.it.webapi.support;

import org.geoserver.acl.config.application.ApplicationServicesConfiguration;
import org.geoserver.acl.config.domain.DomainServicesConfiguration;
import org.geoserver.acl.config.persistence.jpa.JPAIntegrationConfiguration;
import org.geoserver.acl.config.webapi.v1.server.ApiServerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    ConfigurationPropertiesTestConfiguration.class,
    ApiServerConfiguration.class,
    DomainServicesConfiguration.class,
    ApplicationServicesConfiguration.class,
    JPAIntegrationConfiguration.class
})
public class IntegrationTestsApplication {

    public static void main(String... args) {
        try {
            SpringApplication.run(IntegrationTestsApplication.class, args);
        } catch (RuntimeException e) {
            System.exit(-1);
        }
    }
}
