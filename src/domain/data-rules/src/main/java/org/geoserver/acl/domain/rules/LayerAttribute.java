/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Per-attribute access permission carried by a {@link LayerDetails}.
 *
 * <p>Models column-level access control for a single feature type attribute: which attribute
 * (by {@code name} and optional {@code dataType}) and the {@link AccessType} the user has on
 * it (none, read-only, or read-write). A {@link LayerDetails} aggregates these into the set of
 * visible and writable attributes for a layer.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record LayerAttribute(String name, @Nullable String dataType, @Nullable AccessType access) {

    public enum AccessType {

        /** No access to the resource. */
        NONE,

        /** Read only access. */
        READONLY,

        /** Full access. */
        READWRITE;

        /**
         * Returns the more restrictive of {@code a1} and {@code a2} (NONE wins, then READONLY,
         * then READWRITE).
         *
         * <p>Used for AND-ing rules within a chain (every applicable rule has to permit, so the
         * most restrictive grant wins). A {@code null} argument is treated as the strictest
         * possible value and {@link #NONE} is returned.
         */
        public static AccessType stricter(@Nullable AccessType a1, @Nullable AccessType a2) {
            if (a1 == null || a2 == null) {
                return NONE; // should not happen
            }
            if (a1 == NONE || a2 == NONE) {
                return NONE;
            }
            if (a1 == READONLY || a2 == READONLY) {
                return READONLY;
            }
            return READWRITE;
        }

        /**
         * Returns the more permissive of {@code a1} and {@code a2} (READWRITE wins, then READONLY,
         * then NONE).
         *
         * <p>Mirror of {@link #stricter(AccessType, AccessType)} for OR-ing rules across roles
         * (the most permissive grant wins). A {@code null} argument is treated as "no opinion" and
         * the other operand is returned unchanged; both null returns {@link #NONE}.
         */
        public static AccessType lenient(@Nullable AccessType a1, @Nullable AccessType a2) {
            if (a1 == null) return a2 == null ? NONE : a2;
            if (a2 == null) return a1;
            if (a1 == READWRITE || a2 == READWRITE) return READWRITE;
            if (a1 == READONLY || a2 == READONLY) return READONLY;
            return NONE;
        }
    }
}
