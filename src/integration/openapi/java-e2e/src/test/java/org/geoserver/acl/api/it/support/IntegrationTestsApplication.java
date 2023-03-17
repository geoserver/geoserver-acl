/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.it.support;

import org.geoserver.acl.api.server.config.RulesApiConfiguration;
import org.geoserver.acl.config.domain.AdminRuleAdminServiceConfiguration;
import org.geoserver.acl.config.domain.RuleAdminServiceConfiguration;
import org.geoserver.acl.integration.jpa.config.AuthorizationJPAIntegrationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    ConfigurationPropertiesTestConfiguration.class,
    RulesApiConfiguration.class,
    RuleAdminServiceConfiguration.class,
    AdminRuleAdminServiceConfiguration.class,
    AuthorizationJPAIntegrationConfiguration.class
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
