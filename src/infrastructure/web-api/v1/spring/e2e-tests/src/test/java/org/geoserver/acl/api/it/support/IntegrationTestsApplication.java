/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.support;

import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.AuthorizationServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.geoserver.acl.integration.jpa.config.JPAIntegrationConfiguration;
import org.geoserver.acl.webapi.v1.server.config.AuthorizationApiConfiguration;
import org.geoserver.acl.webapi.v1.server.config.RulesApiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    ConfigurationPropertiesTestConfiguration.class,
    RulesApiConfiguration.class,
    AuthorizationApiConfiguration.class,
    RuleAdminServiceConfiguration.class,
    AuthorizationServiceConfiguration.class,
    AdminRuleAdminServiceConfiguration.class,
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
