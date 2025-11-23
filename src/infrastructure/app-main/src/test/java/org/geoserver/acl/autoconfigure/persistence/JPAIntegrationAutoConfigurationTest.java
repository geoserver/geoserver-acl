/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.config.simplejndi.JNDIDataSourceAutoConfiguration;
import org.geoserver.acl.config.simplejndi.SimpleJNDIStaticContextInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JPAIntegrationAutoConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JPAIntegrationAutoConfiguration.class));

    @BeforeEach
    void setUp() throws Exception {}

    @Test
    void testDbConfigWithURL() {
        runner.withPropertyValues("geoserver.acl.datasource.url=jdbc:h2:mem:geoserver-acl")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }

    @Test
    void testDbConfigWithJNDI() {
        runner.withInitializer(new SimpleJNDIStaticContextInitializer())
                .withConfiguration(AutoConfigurations.of(
                        JNDIDataSourceAutoConfiguration.class, JPAIntegrationAutoConfiguration.class))
                .withPropertyValues(
                        "jndi.datasources.acltest.url: jdbc:h2:mem:acltest", //
                        "jndi.datasources.acltest.username: sa", //
                        "jndi.datasources.acltest.password: sa", //
                        "geoserver.acl.datasource.jndiName=java:comp/env/jdbc/acltest")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                });
    }
}
