/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geoserver.data.test.MockData;
import org.geoserver.platform.GeoServerExtensions;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;

public class ServicesTest extends AclBaseTest {

    void loginAsCite() {
        login("cite", "cite", "ROLE_CITE_ADMIN");
    }

    void loginAsSf() {
        login("sf", "sf", "ROLE_SF_ADMIN");
    }

    /** Enable the Spring Security auth filters, otherwise there will be no auth */
    @Override
    protected List<javax.servlet.Filter> getFilters() {
        return Collections.singletonList(
                (javax.servlet.Filter) GeoServerExtensions.bean("filterChainProxy"));
    }

    @Test
    public void testAdmin() throws Exception {
        this.username = "admin";
        this.password = "geoserver";

        // check from the caps he can access everything
        Document dom = getAsDOM("wms?request=GetCapabilities&version=1.1.1&service=WMS");
        // print(dom);

        assertXpathEvaluatesTo("11", "count(//Layer[starts-with(Name, 'cite:')])", dom);
        assertXpathEvaluatesTo("3", "count(//Layer[starts-with(Name, 'sf:')])", dom);
        assertXpathEvaluatesTo("8", "count(//Layer[starts-with(Name, 'cdf:')])", dom);
    }

    @Test
    public void testCiteCapabilities() throws Exception {
        this.username = "cite";
        this.password = "cite";

        // check from the caps he can access cite and sf, but not others
        Document dom = getAsDOM("wms?request=GetCapabilities&version=1.1.1&service=WMS");

        assertXpathEvaluatesTo("11", "count(//Layer[starts-with(Name, 'cite:')])", dom);
        assertXpathEvaluatesTo("3", "count(//Layer[starts-with(Name, 'sf:')])", dom);
        assertXpathEvaluatesTo("8", "count(//Layer[starts-with(Name, 'cdf:')])", dom);
    }

    @Test
    public void testCiteLayers() throws Exception {
        loginAsCite();
        this.username = "cite";
        this.password = "cite";

        // try a getfeature on a sf layer
        MockHttpServletResponse response =
                getAsServletResponse(
                        "wfs?service=wfs&version=1.0.0&request=getfeature&typeName="
                                + getLayerId(MockData.GENERICENTITY));
        assertEquals(200, response.getStatus());
        assertEquals("text/xml;charset=UTF-8", response.getContentType());
        String content = response.getContentAsString();
        LOGGER.info("Content: " + content);
        //        assertTrue(content.contains("Unknown namespace [sf]"));
        assertTrue(content.contains("Feature type sf:GenericEntity unknown"));
    }
}
