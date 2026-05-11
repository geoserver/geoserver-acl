/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import lombok.Builder;
import lombok.With;
import org.geolatte.geom.MultiPolygon;
import org.jspecify.annotations.Nullable;

/**
 * Access restrictions for LIMIT grant type rules.
 *
 * <p>Defines constraints for {@link GrantType#LIMIT} rules: geographic and catalog visibility
 * restrictions. Simpler than {@link LayerDetails} (ALLOW rules) - focuses on spatial constraints
 * without attribute-level control or CQL filters.
 *
 * <p>Comparison:
 * <table border="1">
 * <tr><th>Feature</th><th>RuleLimits</th><th>LayerDetails</th></tr>
 * <tr><td>Spatial constraint</td><td>X</td><td>X</td></tr>
 * <tr><td>Catalog mode</td><td>X</td><td>X</td></tr>
 * <tr><td>CQL filters</td><td>-</td><td>X</td></tr>
 * <tr><td>Allowed styles</td><td>-</td><td>X</td></tr>
 * <tr><td>Attribute permissions</td><td>-</td><td>X</td></tr>
 * </table>
 *
 * <p>Example:
 * <pre>{@code
 * Rule limitRule = Rule.limit()
 *     .withUsername("field_worker")
 *     .withWorkspace("parcels")
 *     .withRuleLimits(RuleLimits.clip()
 *         .withAllowedArea(jurisdictionBoundary)
 *         .withCatalogMode(CatalogMode.HIDE))
 *     .withPriority(100);
 * }</pre>
 *
 * @param allowedArea geographic area constraint (MultiPolygon geometry). If specified, access is
 *     restricted to features within or intersecting this area, depending on
 *     {@code spatialFilterType}. Uses geolatte-geom for infrastructure-agnostic geometry
 *     representation. Can be {@code null} for no spatial restriction (limit rule affects only
 *     catalog visibility).
 * @param spatialFilterType how to apply the spatial area constraint. Defaults to
 *     {@link SpatialFilterType#INTERSECT}.
 * @param catalogMode how the layer appears in GetCapabilities responses. Defaults to
 *     {@link CatalogMode#HIDE}.
 * @since 1.0
 * @see Rule
 * @see GrantType#LIMIT
 * @see SpatialFilterType
 * @see LayerDetails
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record RuleLimits(
        @Nullable MultiPolygon<?> allowedArea, SpatialFilterType spatialFilterType, CatalogMode catalogMode) {

    public RuleLimits {
        if (spatialFilterType == null) spatialFilterType = DEFAULT_SPATIAL_FILTERTYPE;
        if (catalogMode == null) catalogMode = DEFAULT_CATALOG_MODE;
    }

    /** Default spatial filter type for rules with spatial constraints. */
    public static final SpatialFilterType DEFAULT_SPATIAL_FILTERTYPE = SpatialFilterType.INTERSECT;

    /** Default catalog mode for layer visibility in GetCapabilities. */
    public static final CatalogMode DEFAULT_CATALOG_MODE = CatalogMode.HIDE;

    public static RuleLimits clip() {
        return RuleLimits.builder().spatialFilterType(SpatialFilterType.CLIP).build();
    }

    public static RuleLimits intersect() {
        return RuleLimits.builder()
                .spatialFilterType(SpatialFilterType.INTERSECT)
                .build();
    }
}
