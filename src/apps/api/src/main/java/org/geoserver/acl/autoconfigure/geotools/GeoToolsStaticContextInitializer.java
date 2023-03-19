/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.acl.autoconfigure.geotools;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ApplicationContextInitializer} to initialize GeoTools
 *
 * <p>With an {@code ApplicationContextInitializer} we make sure required initializations run before
 * even loading the spring beans.
 *
 * @since 1.0
 */
public class GeoToolsStaticContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }
}
