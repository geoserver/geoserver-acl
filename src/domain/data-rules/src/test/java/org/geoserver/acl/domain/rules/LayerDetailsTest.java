/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import org.geoserver.acl.domain.rules.LayerAttribute.AccessType;
import org.junit.jupiter.api.Test;

class LayerDetailsTest {

    @Test
    void testBuilder_allowedStyles_immutable() {
        LayerDetails ld = LayerDetails.builder()
                .allowedStyles(new HashSet<>(Set.of("s1", "s2")))
                .build();
        Set<String> allowedStyles = ld.allowedStyles();
        assertThat(allowedStyles).isEqualTo(Set.of("s1", "s2"));
        assertThatThrownBy(() -> allowedStyles.add("nono")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testBuilder_attributes_immutable() {
        LayerAttribute ro = LayerAttribute.builder()
                .name("att1")
                .access(AccessType.READONLY)
                .build();
        LayerAttribute rw = LayerAttribute.builder()
                .name("att2")
                .access(AccessType.READWRITE)
                .build();

        Set<LayerAttribute> atts = Set.of(ro);
        LayerDetails ld = LayerDetails.builder().attributes(new HashSet<>(atts)).build();
        Set<LayerAttribute> attributes = ld.attributes();
        assertThat(attributes).isEqualTo(atts);

        assertThatThrownBy(() -> attributes.add(rw)).isInstanceOf(UnsupportedOperationException.class);
    }
}
