/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.rules;

/**
 * How spatial area constraints are applied to layer features.
 *
 * <p>Determines behavior when a rule includes a geographic area restriction (via
 * {@link LayerDetails#getArea()} or {@link RuleLimits#getAllowedArea()}). The two modes differ
 * in how they handle features that partially overlap the allowed area.
 *
 * <p>Visual example - polygon extending beyond allowed area:
 * <pre>
 *   +------------------+
 *   | Allowed Area     |
 *   |        +---------|-------+
 *   |        | Feature |       |
 *   |        |         |       |
 *   +--------|---------|       |
 *            |                 |
 *            +-----------------+
 * </pre>
 * INTERSECT: Returns entire feature (including part outside)
 * CLIP: Returns only the portion inside
 *
 * @since 1.0
 * @see LayerDetails#getSpatialFilterType()
 * @see RuleLimits#getSpatialFilterType()
 */
public enum SpatialFilterType {
    /**
     * Return entire features that intersect the allowed area (default).
     * <p>User sees complete geometry including portions outside the restricted area.
     * Faster than CLIP since no geometry clipping required.
     * <p>Use case: Show complete roads passing through a city, even if parts extend outside.
     */
    INTERSECT,

    /**
     * Clip features to the allowed area boundary.
     * <p>Cut geometries at boundary - only the portion inside is returned. Strict geographic containment.
     * More expensive than INTERSECT due to clipping operations.
     * <p>Use case: Show only the portion of parcels within a jurisdiction.
     * <p>Note: Clipped features may have modified geometries (e.g., polygons may become multipolygons).
     */
    CLIP
}
