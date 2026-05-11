/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import java.util.Set;
import java.util.TreeSet;
import lombok.Builder;
import lombok.NonNull;
import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.rules.GrantType;
import org.jspecify.annotations.Nullable;

/**
 * Represents the converged set of visible layer names of a specific workspace for an
 * {@link AccessSummaryRequest}.
 *
 * @param workspace the workspace name. The special value {@link #NO_WORKSPACE} represents global
 *     entities such as global layer groups.
 * @param adminAccess whether the user from the {@link AccessSummaryRequest} is an administrator
 *     for {@code workspace}.
 * @param allowed the set of visible layer names in {@code workspace} the user from the
 *     {@link AccessSummaryRequest} can somehow see, even if only under specific circumstances like
 *     for a given OWS/request combination, resulting from {@link GrantType#ALLOW allow} rules.
 * @param forbidden the set of forbidden layer names in {@code workspace} the user from the
 *     {@link AccessSummaryRequest} definitely cannot see, resulting from {@link GrantType#DENY
 *     deny} rules. Complements {@code allowed} as there may be rules allowing access to all
 *     layers in a workspace after rules denying access to specific layers in the same workspace.
 * @since 2.3
 * @see AccessSummaryRequest
 */
@Builder(builderClassName = "Builder")
public record WorkspaceAccessSummary(
        String workspace, @Nullable AdminGrantType adminAccess, Set<String> allowed, Set<String> forbidden)
        implements Comparable<WorkspaceAccessSummary> {

    public static final String NO_WORKSPACE = "";
    public static final String ANY = "*";

    public WorkspaceAccessSummary {
        if (workspace == null) workspace = ANY;
        allowed = conflate(allowed);
        forbidden = conflate(forbidden);
    }

    private static Set<String> conflate(@Nullable Set<String> layers) {
        if (layers == null || layers.isEmpty()) {
            return Set.of();
        }
        return layers.contains(ANY) ? Set.of(ANY) : Set.copyOf(layers);
    }

    public boolean hideAll() {
        return allowed.isEmpty() && forbidden.contains(ANY);
    }

    public boolean visible() {
        return !hideAll();
    }

    @Override
    public String toString() {
        return "workapce: %s: admin: %s, allowed: %s, forbidden: %s"
                .formatted(workspace, adminAccess, allowed, forbidden);
    }

    public boolean canSeeLayer(String layerName) {
        if (allowed.contains(ANY)) {
            return !forbidden.contains(layerName);
        }
        return allowed.contains(layerName);
    }

    @Override
    public int compareTo(WorkspaceAccessSummary o) {
        return workspace.compareTo(o.workspace());
    }

    public boolean isAdmin() {
        return adminAccess == AdminGrantType.ADMIN;
    }

    public boolean isUser() {
        return adminAccess == AdminGrantType.USER || isAdmin();
    }

    public static class Builder {
        @SuppressWarnings({"unused", "java:S1068", "java:S1450"})
        private String workspace = ANY;

        @SuppressWarnings({"unused", "java:S1068", "java:S1450"})
        private Set<String> allowed = new TreeSet<>();

        @SuppressWarnings({"unused", "java:S1068", "java:S1450"})
        private Set<String> forbidden = new TreeSet<>();

        public Builder allowed(@NonNull Set<String> layers) {
            this.allowed = new TreeSet<>(layers);
            forbidden.removeAll(layers);
            return this;
        }

        public Builder forbidden(@NonNull Set<String> layers) {
            this.forbidden = new TreeSet<>(layers);
            allowed.removeAll(layers);
            return this;
        }

        public Builder addAllowed(@NonNull String layer) {
            allowed.add(layer);
            forbidden.remove(layer);
            return this;
        }

        public Builder addForbidden(@NonNull String layer) {
            forbidden.add(layer);
            allowed.remove(layer);
            return this;
        }
    }
}
