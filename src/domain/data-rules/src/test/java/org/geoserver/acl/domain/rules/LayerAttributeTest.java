/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.NONE;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READONLY;
import static org.geoserver.acl.domain.rules.LayerAttribute.AccessType.READWRITE;

import org.geoserver.acl.domain.rules.LayerAttribute.AccessType;
import org.junit.jupiter.api.Test;

class LayerAttributeTest {

    @Test
    void lenient_picksMostPermissive_whenBothNonNull() {
        assertThat(AccessType.lenient(READWRITE, READONLY)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(READONLY, READWRITE)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(READWRITE, NONE)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(NONE, READWRITE)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(READONLY, NONE)).isEqualTo(READONLY);
        assertThat(AccessType.lenient(NONE, READONLY)).isEqualTo(READONLY);
        assertThat(AccessType.lenient(READWRITE, READWRITE)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(READONLY, READONLY)).isEqualTo(READONLY);
        assertThat(AccessType.lenient(NONE, NONE)).isEqualTo(NONE);
    }

    @Test
    void lenient_propagatesNonNullArg_whenOneIsNull() {
        assertThat(AccessType.lenient(null, READWRITE)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(READWRITE, null)).isEqualTo(READWRITE);
        assertThat(AccessType.lenient(null, READONLY)).isEqualTo(READONLY);
        assertThat(AccessType.lenient(READONLY, null)).isEqualTo(READONLY);
        assertThat(AccessType.lenient(null, NONE)).isEqualTo(NONE);
        assertThat(AccessType.lenient(NONE, null)).isEqualTo(NONE);
    }

    @Test
    void lenient_bothNull_returnsNone() {
        assertThat(AccessType.lenient(null, null)).isEqualTo(NONE);
    }
}
