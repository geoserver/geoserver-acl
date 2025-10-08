/* (c) 2025 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.it.wfs;

import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.GrantType.DENY;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.data.test.SystemTestData;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GetCapabilitiesTest extends AbstractAclWFSIntegrationTest {

    @Override
    protected void setUpInternal(SystemTestData testData) throws Exception {
        addUser("cite", "cite", null, List.of("ROLE_CITE"));
    }

    @Before
    public void setUpAcl() {
        // anonymous can only see cdf:* layers
        support.addRule(ALLOW, null, "ROLE_ANONYMOUS", null, null, "cdf", null, 0);
        support.addRule(ALLOW, null, "ROLE_CITE", null, null, "cite", null, 1);
        support.addRule(DENY, null, null, null, null, null, null, 10);
    }

    private List<String> getLayers(Predicate<String> prefixedNameFilter) {
        return getRawCatalog().getLayers().stream()
                .map(LayerInfo::prefixedName)
                .filter(prefixedNameFilter)
                .toList();
    }

    @Test
    public void testGetAsAdmin() throws Exception {
        List<String> allNames = getLayers(l -> true);
        loginAsAdmin();
        testGet(allNames);
    }

    @Test
    public void testGetAsAnonymous() throws Exception {
        login("anonymousUser", "", "ROLE_ANONYMOUS");
        List<String> expected = getLayers(name -> name.startsWith("cdf:"));
        testGet(expected);
    }

    @Test
    public void testGetAsRoleCITE() throws Exception {
        List<String> expected = getLayers(name -> name.startsWith("cite:"));
        login("cite", "cite", "ROLE_CITE");
        testGet(expected);
    }

    private void testGet(List<String> expected) throws Exception {
        Document doc = getAsDOM("wfs?service=WFS&version=1.0.0&request=getCapabilities");
        assertEquals("WFS_Capabilities", doc.getDocumentElement().getNodeName());

        XpathEngine xpath = XMLUnit.newXpathEngine();
        NodeList matchingNodes = xpath.getMatchingNodes("//wfs:FeatureType/wfs:Name", doc);

        expected = expected.stream().sorted().toList();
        List<String> actual = IntStream.range(0, matchingNodes.getLength())
                .mapToObj(matchingNodes::item)
                .map(Node::getTextContent)
                .sorted()
                .toList();
        assertEquals(expected, actual);
    }
}
