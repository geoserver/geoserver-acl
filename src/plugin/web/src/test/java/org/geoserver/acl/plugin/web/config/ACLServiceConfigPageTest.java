/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license (org.geoserver.geofence.GeofencePageTest)
 */
package org.geoserver.acl.plugin.web.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.acl.plugin.accessmanager.config.AclConfigurationManager;
import org.geoserver.acl.plugin.web.support.AclWicketTestSupport;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.GeoServerHomePage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Ignore("TODO")
public class ACLServiceConfigPageTest extends AclWicketTestSupport {

    public @Rule TemporaryFolder configFolder = new TemporaryFolder();

    private AclConfigurationManager configManager;
    private File testConfigFile;

    @Before
    @Override
    public void beforeEach() throws Exception {
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

    /**
     * @FIXME This test fails in 2.6
     */
    @Test
    public void testSave() throws URISyntaxException, IOException {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("instanceName", "new-gs");
        ft.submit("submit");
        tester.assertRenderedPage(GeoServerHomePage.class);

        assertTrue(testConfigFile.length() > 0);
    }

    /**
     * @FIXME This test fails in 2.6
     */
    @Test
    public void testCancel() throws URISyntaxException, IOException {
        FormTester ft = tester.newFormTester("form");
        ft.submit("cancel");
        tester.assertRenderedPage(GeoServerHomePage.class);
        assertEquals(0, testConfigFile.length());
    }

    @Test
    public void testErrorEmptyInstance() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("instanceName", "");
        ft.submit("submit");
        tester.assertRenderedPage(ACLServiceConfigPage.class);

        tester.assertContains("is required");
    }

    @Test
    public void testErrorEmptyURL() {
        FormTester ft = tester.newFormTester("form");
        ft.setValue("servicesUrl", "");
        ft.submit("submit");
        tester.assertRenderedPage(ACLServiceConfigPage.class);

        tester.assertContains("is required");
    }

    @Test
    public void testErrorWrongURL() {
        @SuppressWarnings("unchecked")
        TextField<String> servicesUrl =
                ((TextField<String>) tester.getComponentFromLastRenderedPage("form:servicesUrl"));
        servicesUrl.setDefaultModel(new Model<>("fakeurl"));

        tester.clickLink("form:test", true);

        tester.assertContains("RemoteAccessException");
    }
}
