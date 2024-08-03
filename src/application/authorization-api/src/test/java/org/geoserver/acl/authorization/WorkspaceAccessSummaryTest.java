/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.authorization.WorkspaceAccessSummary.builder;

import org.geoserver.acl.authorization.WorkspaceAccessSummary.Builder;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.junit.jupiter.api.Test;

import java.util.Set;

class WorkspaceAccessSummaryTest {

    @Test
    void buildDefaults() {
        WorkspaceAccessSummary was = builder().build();
        assertThat(was.getWorkspace()).isEqualTo("*");
        assertThat(was.getAllowed()).isEmpty();
        assertThat(was.getForbidden()).isEmpty();
        assertThat(was.getAdminAccess()).isNull();
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isFalse();
    }

    @Test
    void buildOnlyWorkspace() {
        WorkspaceAccessSummary was = builderWithWorkspace().build();
        assertThat(was.getWorkspace()).isEqualTo("cite");
        assertThat(was.getAllowed()).isEmpty();
        assertThat(was.getForbidden()).isEmpty();
        assertThat(was.getAdminAccess()).isNull();
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isFalse();
    }

    @Test
    void buildAdmin() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().adminAccess(AdminGrantType.ADMIN).build();
        assertThat(was.getWorkspace()).isEqualTo("cite");
        assertThat(was.getAdminAccess()).isEqualTo(AdminGrantType.ADMIN);
        assertThat(was.isAdmin()).isTrue();
        assertThat(was.isUser()).isTrue();
    }

    @Test
    void buildUser() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().adminAccess(AdminGrantType.USER).build();
        assertThat(was.getWorkspace()).isEqualTo("cite");
        assertThat(was.getAdminAccess()).isEqualTo(AdminGrantType.USER);
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isTrue();
    }

    @Test
    void allowedLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().allowed(Set.of("*")).build();
        assertThat(was.getAllowed()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().allowed(Set.of("layer1", "layer2")).build();
        assertThat(was.getAllowed()).isEqualTo(Set.of("layer1", "layer2"));
    }

    @Test
    void addAllowedLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().addAllowed("*").build();
        assertThat(was.getWorkspace()).isEqualTo("cite");
        assertThat(was.getAllowed()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().addAllowed("layer1").addAllowed("layer2").build();
        assertThat(was.getAllowed()).isEqualTo(Set.of("layer1", "layer2"));
    }

    @Test
    void allowedLayersConflates() {
        WorkspaceAccessSummary was =
                builderWithWorkspace()
                        .addAllowed("layer1")
                        .addAllowed("layer2")
                        .addAllowed("*")
                        .build();
        assertThat(was.getAllowed()).isEqualTo(Set.of("*"));

        was =
                builderWithWorkspace()
                        .addAllowed("layer1")
                        .addAllowed("layer2")
                        .addAllowed("*")
                        .addAllowed("layer3")
                        .addAllowed("layer4")
                        .build();
        assertThat(was.getAllowed()).isEqualTo(Set.of("*"));
    }

    @Test
    void forbiddenLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().forbidden(Set.of("*")).build();
        assertThat(was.getForbidden()).isEqualTo(Set.of("*"));
        assertThat(was.getAllowed()).isEmpty();

        was = builderWithWorkspace().forbidden(Set.of("l1", "l2")).build();
        assertThat(was.getForbidden()).isEqualTo(Set.of("l1", "l2"));
        assertThat(was.getAllowed()).isEmpty();
    }

    @Test
    void addForbiddenLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().addForbidden("*").build();
        assertThat(was.getForbidden()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().addForbidden("l1").addForbidden("l2").build();
        assertThat(was.getForbidden()).isEqualTo(Set.of("l1", "l2"));
        assertThat(was.getAllowed()).isEmpty();
    }

    @Test
    void canSeeLayer() {
        var was = builderWithWorkspace().addForbidden("*").addAllowed("L1").build();
        assertThat(was.canSeeLayer("L1")).isTrue();
        assertThat(was.canSeeLayer("L2")).isFalse();

        was = builderWithWorkspace().addAllowed("*").addForbidden("L1").build();
        assertThat(was.canSeeLayer("L1")).isFalse();
        assertThat(was.canSeeLayer("L2")).isTrue();

        was = builderWithWorkspace().addAllowed("L1").addForbidden("L2").build();
        assertThat(was.canSeeLayer("L1")).isTrue();
        assertThat(was.canSeeLayer("L2")).isFalse();

        was = builderWithWorkspace().addForbidden("L2").addAllowed("L1").build();
        assertThat(was.canSeeLayer("L1")).isTrue();
        assertThat(was.canSeeLayer("L2")).isFalse();

        was = builderWithWorkspace().addAllowed("L1").addForbidden("L1").addForbidden("L2").build();
        assertThat(was.canSeeLayer("L2")).isFalse();
        assertThat(was.canSeeLayer("L1")).isFalse();
    }

    private Builder builderWithWorkspace() {
        return builder().workspace("cite");
    }
}
