/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import java.util.Set;
import lombok.Builder;
import lombok.With;
import org.geolatte.geom.MultiPolygon;
import org.jspecify.annotations.Nullable;

/**
 * Detailed access constraints for ALLOW rules on layers.
 *
 * <p>Provides fine-grained control when a {@link Rule} grants permission ({@link GrantType#ALLOW}):
 * <ul>
 *   <li>Which styles can be used
 *   <li>What data is visible (CQL filters for read/write separately)
 *   <li>Which geographic areas are accessible (spatial filtering)
 *   <li>Which attributes are visible or editable (column-level permissions)
 *   <li>How the layer appears in GetCapabilities (catalog mode)
 * </ul>
 *
 * <p>LayerDetails is stored separately from {@link Rule} and retrieved via
 * {@link RuleRepository#findLayerDetailsByRuleId(String)}. This allows efficient rule queries
 * without loading potentially large spatial geometries or attribute lists.
 *
 * <p>Example:
 * <pre>{@code
 * LayerDetails details = LayerDetails.builder()
 *     .type(LayerType.VECTOR)
 *     .cqlFilterRead("status = 'public'")
 *     .cqlFilterWrite("status = 'public' AND owner = '${userName}'")
 *     .area(cityBoundaryPolygon)
 *     .spatialFilterType(SpatialFilterType.CLIP)
 *     .allowedStyles(Set.of("default", "simple"))
 *     .attributes(Set.of(
 *         LayerAttribute.read("name"),
 *         LayerAttribute.write("status")
 *     ))
 *     .catalogMode(CatalogMode.HIDE)
 *     .build();
 * }</pre>
 *
 * <p>Immutable. Use {@code with*()} methods or {@code toBuilder()} for modifications.
 *
 * @param type the type of layer these details apply to. Optional; can be used for
 *     layer-type-specific validation or processing.
 * @param defaultStyle default style to use when no style is explicitly requested. Must be one of
 *     the {@code allowedStyles} if that set is non-empty.
 * @param cqlFilterRead CQL filter applied to read operations (GetMap, GetFeature, GetFeatureInfo).
 *     Only features matching this filter are visible to the user. Supports attribute comparisons,
 *     spatial predicates, and placeholder substitution (e.g., {@code owner = '${userName}'}).
 * @param cqlFilterWrite CQL filter applied to write operations (Transaction: Insert, Update,
 *     Delete). Only features matching this filter can be modified by the user. Typically more
 *     restrictive than {@code cqlFilterRead}.
 * @param area geographic area constraint (MultiPolygon geometry). If specified, only features
 *     within or intersecting this area are accessible, depending on {@code spatialFilterType}.
 *     Uses geolatte-geom for infrastructure-agnostic geometry representation. Can be {@code null}
 *     for no spatial restriction.
 * @param spatialFilterType how to apply the spatial area constraint. Defaults to
 *     {@link SpatialFilterType#INTERSECT}.
 * @param catalogMode how the layer appears in GetCapabilities responses. Defaults to
 *     {@link CatalogMode#HIDE}.
 * @param allowedStyles set of style names the user is permitted to use. If empty, all styles are
 *     allowed; if non-empty, only listed styles can be requested. Useful for preventing
 *     information disclosure through specialized styles or for enforcing consistent
 *     visualization. Immutable, never {@code null}.
 * @param attributes set of layer attributes with their access permissions. Defines column-level
 *     access control: each {@link LayerAttribute} specifies whether an attribute is hidden,
 *     read-only, or read-write. If empty, all attributes are accessible according to the CQL
 *     filters; if non-empty, only listed attributes are visible with their specified permissions.
 *     Immutable, never {@code null}.
 * @since 1.0
 * @see Rule
 * @see GrantType#ALLOW
 * @see LayerAttribute
 * @see SpatialFilterType
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record LayerDetails(
        @Nullable LayerType type,
        @Nullable String defaultStyle,
        @Nullable String cqlFilterRead,
        @Nullable String cqlFilterWrite,
        @Nullable MultiPolygon<?> area,
        SpatialFilterType spatialFilterType,
        CatalogMode catalogMode,
        Set<String> allowedStyles,
        Set<LayerAttribute> attributes) {

    public LayerDetails {
        if (spatialFilterType == null) spatialFilterType = SpatialFilterType.INTERSECT;
        if (catalogMode == null) catalogMode = CatalogMode.HIDE;
        allowedStyles = allowedStyles == null ? Set.of() : Set.copyOf(allowedStyles);
        attributes = attributes == null ? Set.of() : Set.copyOf(attributes);
    }

    /**
     * Enumeration of GeoServer layer types.
     *
     * <p>Used to identify the kind of layer being restricted. Different layer types may have
     * different applicable constraints.
     */
    public enum LayerType {
        /** Vector data (features with geometry and attributes). */
        VECTOR,

        /** Raster data (gridded coverage data). */
        RASTER,

        /** Layer group (collection of multiple layers). */
        LAYERGROUP
    }
}
