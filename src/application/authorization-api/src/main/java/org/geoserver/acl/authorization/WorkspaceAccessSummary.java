/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static java.lang.String.format;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import org.geoserver.acl.domain.adminrules.AdminGrantType;
import org.geoserver.acl.domain.rules.GrantType;

import java.util.Set;
import java.util.TreeSet;

/**
 * Represents the converged set of visible layer names of a specific workspace for for a {@link
 * AccessSummaryRequest}
 *
 * @since 2.3
 * @see AccessSummaryRequest
 */
@Value
@Builder(builderClassName = "Builder")
public class WorkspaceAccessSummary implements Comparable<WorkspaceAccessSummary> {
    public static final String NO_WORKSPACE = "";
    public static final String ANY = "*";

    /**
     * The workspace name. The special value {@link #NO_WORKSPACE} represents global entities such
     * as global layer groups
     */
    @NonNull private String workspace;

    /**
     * Whether the user from the {@link AccessSummaryRequest} is an administrator for {@link
     * #workspace}
     */
    private AdminGrantType adminAccess;

    /**
     * The set of visible layer names in {@link #workspace} the user from the {@link
     * AccessSummaryRequest} can somehow see, even if only under specific circumstances like for a
     * given OWS/request combination, resulting from {@link GrantType#ALLOW allow} rules.
     */
    @NonNull private Set<String> allowed;

    /**
     * The set of forbidden layer names in {@link #workspace} the user from the {@link
     * AccessSummaryRequest} definitely cannot see, resulting from {@link GrantType#DENY deny}
     * rules.
     *
     * <p>Complements the {@link #allowed} list as there may be rules allowing access all layers in
     * a workspace after a rules denying access to specific layers in the same workspace.
     */
    @NonNull private Set<String> forbidden;

    @Override
    public String toString() {
        return format(
                "[%s: admin: %s, allowed: %s, forbidden: %s]",
                workspace, adminAccess, allowed, forbidden);
    }

    public boolean canSeeLayer(String layerName) {
        if (allowed.contains(ANY)) {
            return !forbidden.contains(layerName);
        }
        return allowed.contains(layerName);
    }

    public static class Builder {

        private String workspace = ANY;
        private AdminGrantType adminAccess;
        private Set<String> allowed = new TreeSet<>();
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

        public WorkspaceAccessSummary build() {
            Set<String> allowedLayers = conflate(allowed);
            Set<String> forbiddenLayers = conflate(forbidden);

            return new WorkspaceAccessSummary(
                    workspace, adminAccess, allowedLayers, forbiddenLayers);
        }

        private static Set<String> conflate(Set<String> layers) {
            return layers.contains(ANY) ? Set.of(ANY) : Set.copyOf(layers);
        }
    }

    @Override
    public int compareTo(WorkspaceAccessSummary o) {
        return workspace.compareTo(o.getWorkspace());
    }

    public boolean isAdmin() {
        return adminAccess == AdminGrantType.ADMIN;
    }

    public boolean isUser() {
        return adminAccess == AdminGrantType.USER || adminAccess == AdminGrantType.ADMIN;
    }
}
