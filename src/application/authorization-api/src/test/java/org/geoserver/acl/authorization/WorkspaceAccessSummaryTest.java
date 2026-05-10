/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.geoserver.acl.authorization.WorkspaceAccessSummary.builder;

import java.util.Set;
import org.geoserver.acl.authorization.WorkspaceAccessSummary.Builder;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.junit.jupiter.api.Test;

class WorkspaceAccessSummaryTest {

    @Test
    void buildDefaults() {
        WorkspaceAccessSummary was = builder().build();
        assertThat(was.workspace()).isEqualTo("*");
        assertThat(was.allowed()).isEmpty();
        assertThat(was.forbidden()).isEmpty();
        assertThat(was.adminAccess()).isNull();
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isFalse();
    }

    @Test
    void buildOnlyWorkspace() {
        WorkspaceAccessSummary was = builderWithWorkspace().build();
        assertThat(was.workspace()).isEqualTo("cite");
        assertThat(was.allowed()).isEmpty();
        assertThat(was.forbidden()).isEmpty();
        assertThat(was.adminAccess()).isNull();
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isFalse();
    }

    @Test
    void buildAdmin() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().adminAccess(AdminGrantType.ADMIN).build();
        assertThat(was.workspace()).isEqualTo("cite");
        assertThat(was.adminAccess()).isEqualTo(AdminGrantType.ADMIN);
        assertThat(was.isAdmin()).isTrue();
        assertThat(was.isUser()).isTrue();
    }

    @Test
    void buildUser() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().adminAccess(AdminGrantType.USER).build();
        assertThat(was.workspace()).isEqualTo("cite");
        assertThat(was.adminAccess()).isEqualTo(AdminGrantType.USER);
        assertThat(was.isAdmin()).isFalse();
        assertThat(was.isUser()).isTrue();
    }

    @Test
    void allowedLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().allowed(Set.of("*")).build();
        assertThat(was.allowed()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().allowed(Set.of("layer1", "layer2")).build();
        assertThat(was.allowed()).isEqualTo(Set.of("layer1", "layer2"));
    }

    @Test
    void addAllowedLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().addAllowed("*").build();
        assertThat(was.workspace()).isEqualTo("cite");
        assertThat(was.allowed()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().addAllowed("layer1").addAllowed("layer2").build();
        assertThat(was.allowed()).isEqualTo(Set.of("layer1", "layer2"));
    }

    @Test
    void allowedLayersConflates() {
        WorkspaceAccessSummary was = builderWithWorkspace()
                .addAllowed("layer1")
                .addAllowed("layer2")
                .addAllowed("*")
                .build();
        assertThat(was.allowed()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace()
                .addAllowed("layer1")
                .addAllowed("layer2")
                .addAllowed("*")
                .addAllowed("layer3")
                .addAllowed("layer4")
                .build();
        assertThat(was.allowed()).isEqualTo(Set.of("*"));
    }

    @Test
    void forbiddenLayers() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().forbidden(Set.of("*")).build();
        assertThat(was.forbidden()).isEqualTo(Set.of("*"));
        assertThat(was.allowed()).isEmpty();

        was = builderWithWorkspace().forbidden(Set.of("l1", "l2")).build();
        assertThat(was.forbidden()).isEqualTo(Set.of("l1", "l2"));
        assertThat(was.allowed()).isEmpty();
    }

    @Test
    void addForbiddenLayers() {
        WorkspaceAccessSummary was = builderWithWorkspace().addForbidden("*").build();
        assertThat(was.forbidden()).isEqualTo(Set.of("*"));

        was = builderWithWorkspace().addForbidden("l1").addForbidden("l2").build();
        assertThat(was.forbidden()).isEqualTo(Set.of("l1", "l2"));
        assertThat(was.allowed()).isEmpty();
    }

    @Test
    void canSeeLayer() {
        WorkspaceAccessSummary was =
                builderWithWorkspace().addForbidden("*").addAllowed("L1").build();
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

        was = builderWithWorkspace()
                .addAllowed("L1")
                .addForbidden("L1")
                .addForbidden("L2")
                .build();
        assertThat(was.canSeeLayer("L2")).isFalse();
        assertThat(was.canSeeLayer("L1")).isFalse();
    }

    private Builder builderWithWorkspace() {
        return builder().workspace("cite");
    }
}
