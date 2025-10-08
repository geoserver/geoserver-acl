/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.it.wms;

import java.util.List;
import org.geoserver.acl.plugin.accessmanager.ACLResourceAccessManager;
import org.geoserver.acl.plugin.it.support.AclIntegrationTestSupport;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.geoserver.wms.WMSTestSupport;
import org.junit.After;
import org.junit.Before;

public class AbstractAclWMSIntegrationTest extends WMSTestSupport {

    protected AclIntegrationTestSupport support;
    protected ACLResourceAccessManager accessManager;

    @Override
    protected void setUpSpring(List<String> springContextLocations) {
        super.setUpSpring(springContextLocations);
        springContextLocations.add("classpath*:/applicationContext-test.xml");
    }

    @Before
    public void beforeEeach() {
        support = new AclIntegrationTestSupport(() -> GeoServerSystemTestSupport.applicationContext);
        support.before();
        accessManager = applicationContext.getBean(ACLResourceAccessManager.class);
        // reset default config
        accessManager.getConfig().initDefaults();
    }

    @After
    public void afterEach() {
        support.after();
    }

    protected Catalog getRawCatalog() {
        return support.getRawCatalog();
    }

    protected LayerGroupInfo addLakesPlacesLayerGroup(LayerGroupInfo.Mode mode, String name) throws Exception {

        Catalog catalog = support.getRawCatalog();
        LayerGroupInfo group = super.createLakesPlacesLayerGroup(catalog, name, mode, null);
        support.createdLayerGroups.add(group);
        return group;
    }
}
