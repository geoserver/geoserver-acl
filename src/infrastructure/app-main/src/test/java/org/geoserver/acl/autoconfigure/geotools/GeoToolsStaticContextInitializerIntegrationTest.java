/* (c) 2025  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.geotools;

import static org.assertj.core.api.Assertions.assertThat;

import org.geoserver.acl.app.AccesControlListApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that {@link GeoToolsStaticContextInitializer} is automatically
 * loaded via {@code META-INF/spring.factories}
 * and runs before the application context is initialized.
 */
@SpringBootTest(classes = AccesControlListApplication.class)
@ActiveProfiles("dev")
class GeoToolsStaticContextInitializerIntegrationTest {

    /**
     * Verify that the GeoTools system property was set by the initializer before
     * the Spring context was initialized. This tests the auto-configuration mechanism.
     */
    @Test
    void testInitializerAutoConfigured() {
        assertThat(System.getProperty("org.geotools.referencing.forceXY"))
                .as("GeoToolsStaticContextInitializer should have set org.geotools.referencing.forceXY=true")
                .isEqualTo("true");
    }
}
