/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.autoconfigure.geotools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class GeoToolsStaticContextInitializerTest {

    @Test
    void testInitializer() {
        new ApplicationContextRunner()
                .withInitializer(new GeoToolsStaticContextInitializer())
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(System.getProperty("org.geotools.referencing.forceXY"))
                                    .isEqualTo("true");
                        });
    }
}
