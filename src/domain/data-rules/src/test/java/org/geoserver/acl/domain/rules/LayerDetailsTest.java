/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LayerDetailsTest {

    @BeforeEach
    void setUp() throws Exception {}

    @Test
    void testBuilder_allowedStyles_immutable() {
        LayerDetails ld = LayerDetails.builder()
                .allowedStyles(new HashSet<>(Set.of("s1", "s2")))
                .build();
        assertThat(ld.getAllowedStyles()).isEqualTo(Set.of("s1", "s2"));
        assertThrows(
                UnsupportedOperationException.class, () -> ld.getAllowedStyles().add("nono"));
    }

    @Test
    void testBuilder_attributes_immutable() {
        Set<LayerAttribute> atts = Set.of(LayerAttribute.read().withName("att1"));
        LayerDetails ld = LayerDetails.builder().attributes(new HashSet<>(atts)).build();
        assertThat(ld.getAttributes()).isEqualTo(atts);
        assertThrows(UnsupportedOperationException.class, () -> ld.getAttributes()
                .add(LayerAttribute.write().withName("att2")));
    }
}
