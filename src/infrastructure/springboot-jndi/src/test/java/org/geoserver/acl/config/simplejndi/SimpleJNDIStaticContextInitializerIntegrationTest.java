/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.acl.config.simplejndi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import org.geoserver.acl.simplejndi.SimpleNamingContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test to verify that {@link SimpleJNDIStaticContextInitializer} is automatically
 * loaded via {@code META-INF/spring.factories}
 * and runs before the application context is initialized.
 */
@SpringBootTest(classes = SimpleJNDIStaticContextInitializerIntegrationTest.TestApplication.class)
class SimpleJNDIStaticContextInitializerIntegrationTest {

    @SpringBootApplication
    static class TestApplication {
        // Minimal test application
    }

    /**
     * Verify that the JNDI context was initialized by the initializer before
     * the Spring context was initialized. This tests the auto-configuration mechanism.
     */
    @Test
    void testInitializerAutoConfigured() throws NamingException {
        assertThat(NamingManager.hasInitialContextFactoryBuilder())
                .as("SimpleJNDIStaticContextInitializer should have set the InitialContextFactoryBuilder")
                .isTrue();

        InitialContext initialContext = new InitialContext();
        assertThat(initialContext).isNotNull();

        Context ctx = NamingManager.getInitialContext(new Hashtable<>());
        assertThat(ctx)
                .as("JNDI context should be an instance of SimpleNamingContext")
                .isInstanceOf(SimpleNamingContext.class);
    }
}
