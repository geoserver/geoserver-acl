/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.it.wms;

import static org.geoserver.acl.domain.rules.CatalogMode.HIDE;
import static org.geoserver.acl.domain.rules.GrantType.ALLOW;
import static org.geoserver.acl.domain.rules.LayerDetails.LayerType.VECTOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.data.test.MockData;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class GetLegendGraphicIntegrationTest extends AclWMSTestSupport {

    @Test
    public void testLegendGraphicNestedGroups() throws Exception {
        Rule r1 = support.addRule(ALLOW, null, null, null, null, null, null, 1);

        addLakesPlacesLayerGroup(LayerGroupInfo.Mode.SINGLE, "nested");
        addLakesPlacesLayerGroup(LayerGroupInfo.Mode.OPAQUE_CONTAINER, "container");

        LayerGroupInfo group = getRawCatalog().getLayerGroupByName("container");
        LayerGroupInfo nested = getRawCatalog().getLayerGroupByName("nested");
        group.getLayers().add(nested);
        group.getStyles().add(null);
        getRawCatalog().save(group);

        login("anonymousUser", "", "ROLE_ANONYMOUS");
        String url =
                "wms?service=WMS&version=1.1.1&request=GetLegendGraphic"
                        + "&layer="
                        + group.getName()
                        + "&style="
                        + "&format=image/png&width=20&height=20";
        MockHttpServletResponse response = getAsServletResponse(url);
        assertEquals(response.getContentType(), "image/png");
    }

    @Test
    public void testLegendGraphicLayerGroupStyle() throws Exception {
        final String disallowedStyleName = "forests_style";
        LayerGroupInfo group;
        {
            final String layerGroupName = "lakes_and_places_legend";
            addLakesPlacesLayerGroup(LayerGroupInfo.Mode.SINGLE, layerGroupName);

            final Catalog rawCatalog = getRawCatalog();
            group = rawCatalog.getLayerGroupByName(layerGroupName);

            // not among the allowed styles, adding it to a layergroup style
            StyleInfo polygonStyle = rawCatalog.getStyleByName("polygon");
            LayerInfo forest = rawCatalog.getLayerByName(getLayerId(MockData.FORESTS));
            forest.getStyles().add(polygonStyle);
            rawCatalog.save(forest);

            addLayerGroupStyle(group, disallowedStyleName, List.of(forest), List.of(polygonStyle));

            Rule r1 =
                    support.addRule(
                            ALLOW, null, "ROLE_ANONYMOUS", "WMS", null, "cite", "Forests", 1);
            Rule r2 = support.addRule(ALLOW, null, null, null, null, null, null, 2);

            final Set<String> allowedStyles = Set.of("Lakes", "NamedPlaces");
            support.addLayerDetails(r1, allowedStyles, Set.of(), HIDE, null, null, VECTOR);
        }

        login("anonymousUser", "", "ROLE_ANONYMOUS");

        final String urlFormat =
                "wms?service=WMS&version=1.1.1&request=GetLegendGraphic&layer="
                        + group.getName()
                        + "&style=%s"
                        + "&format=image/png&width=20&height=20";

        String url = String.format(urlFormat, ""); // default style
        MockHttpServletResponse response = getAsServletResponse(url);
        // default lg style should not fail
        assertEquals("image/png", response.getContentType());

        url =
                "wms?service=WMS&version=1.1.1&request=GetLegendGraphic"
                        + "&layer="
                        + group.getName()
                        + "&style="
                        + disallowedStyleName
                        + "&format=image/png&width=20&height=20";
        response = getAsServletResponse(url);
        // should fail the forests_style contains the not allowed polygon style
        assertEquals("application/vnd.ogc.se_xml", getBaseMimeType(response.getContentType()));
        assertTrue(response.getContentAsString().contains("style is not available on this layer"));
    }
}
