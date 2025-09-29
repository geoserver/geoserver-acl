/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfig;
import org.geoserver.acl.plugin.util.ACLTestUtils;
import org.geoserver.platform.resource.Resource;
import org.geoserver.test.GeoServerTestSupport;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.UrlResource;

@Ignore
public class AclConfigurationManagerTest extends GeoServerTestSupport {

    AclPropertyPlaceholderConfigurer configurer;

    AclConfigurationManager manager;

    @Override
    protected void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("xlink", "http://www.w3.org/1999/xlink");
        namespaces.put("wfs", "http://www.opengis.net/wfs");
        namespaces.put("wcs", "http://www.opengis.net/wcs/1.1.1");
        namespaces.put("gml", "http://www.opengis.net/gml");
        getTestData().registerNamespaces(namespaces);
        XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(namespaces));
    }

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        manager = applicationContext.getBean(AclConfigurationManager.class);

        configurer =
                (AclPropertyPlaceholderConfigurer) applicationContext.getBean(AclPropertyPlaceholderConfigurer.class);
        configurer.setLocation(new UrlResource(this.getClass().getResource("/test-config.properties")));
    }

    @Test
    public void testSave() throws Exception {
        ACLTestUtils.emptyFile("test-config.properties");

        AccessManagerConfig config = new AccessManagerConfig();
        config.setAllowRemoteAndInlineLayers(true);
        config.setGrantWriteToWorkspacesToAuthenticatedUsers(true);
        config.setUseRolesToFilter(true);
        config.setAcceptedRoles(List.of("A", "B"));

        manager.setConfiguration(config);

        Resource configurationFile = configurer.getConfigFile();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(configurationFile.out()))) {
            writer.write("newUserProperty=custom_property_value\n");
        }

        manager.storeConfiguration();

        File configFile = configurer.getConfigFile().file();
        LOGGER.info("Config file is " + configFile);

        String content = ACLTestUtils.readConfig(configFile);
        assertTrue(content.contains("fakeservice"));
        assertTrue(content.contains("TEST_INSTANCE"));
        assertFalse(content.contains("custom_property_value"));
    }
}
