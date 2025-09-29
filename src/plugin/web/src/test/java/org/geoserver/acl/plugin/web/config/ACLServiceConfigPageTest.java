/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license (org.geoserver.geofence.GeofencePageTest)
 */
package org.geoserver.acl.plugin.web.config;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.acl.plugin.accessmanager.config.AclConfigurationManager;
import org.geoserver.acl.plugin.web.support.AclWicketTestSupport;
import org.geoserver.platform.GeoServerExtensions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ACLServiceConfigPageTest extends AclWicketTestSupport {

    public @Rule TemporaryFolder configFolder = new TemporaryFolder();

    private AclConfigurationManager configManager;
    private File testConfigFile;

    @Before
    @Override
    public void beforeEach() throws IOException {
        super.beforeEach();
        configManager = GeoServerExtensions.bean(AclConfigurationManager.class);
        assertNotNull(configManager);
        testConfigFile = configFolder.newFile("test-config.properties");
        configManager.setConfigLocation(testConfigFile.toURI());

        login();
        tester.startPage(ACLServiceConfigPage.class);
    }

    @Override
    protected void setUpSpring(List<String> springContextLocations) {
        super.setUpSpring(springContextLocations);
        springContextLocations.add("classpath*:/applicationContext-test.xml");
    }

    @Test
    public void testErrorEmptyURL() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("servicesUrl", "");
        tester.clickLink("form:test", true);
        tester.assertRenderedPage(ACLServiceConfigPage.class);

        tester.assertContains("is required");
    }
}
