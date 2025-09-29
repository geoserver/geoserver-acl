/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.authorization.AccessSummaryRequest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AccessSummaryRequestTest {

    @Test
    void testPreconditions() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder()::build);
        assertThat(ex)
                .hasMessageContaining(
                        "AccessSummaryRequest requires user and roles not to be null or empty at the same time");
    }

    @Test
    void testBuild() {
        AccessSummaryRequest req = builder()
                .user("user")
                .roles("ROLE_ADMINISTRATOR", "ROLE_AUTHENTICATED")
                .build();
        assertThat(req.getUser()).isEqualTo("user");
        assertThat(req.getRoles()).containsExactlyInAnyOrder("ROLE_ADMINISTRATOR", "ROLE_AUTHENTICATED");

        req = builder().user("user").roles(Set.of("ROLE_1")).build();
        assertThat(req.getRoles()).containsExactlyInAnyOrder("ROLE_1");

        req = builder().user("user").roles(Set.of("ROLE_1", "ROLE_2", "ROLE_3")).build();
        assertThat(req.getRoles()).containsExactlyInAnyOrder("ROLE_1", "ROLE_2", "ROLE_3");
    }

    @Test
    void testNullUserAllowedIfRolesIsNotEmpty() {
        AccessSummaryRequest req = builder().roles("ROLE_ANONYMOUS").build();
        assertThat(req.getUser()).isNull();
        assertThat(req.getRoles()).isEqualTo(Set.of("ROLE_ANONYMOUS"));
    }
}
