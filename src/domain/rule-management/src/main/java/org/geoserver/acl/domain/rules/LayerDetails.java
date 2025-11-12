/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import java.util.Set;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.geolatte.geom.MultiPolygon;

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
 * @since 1.0
 * @see Rule
 * @see GrantType#ALLOW
 * @see LayerAttribute
 * @see SpatialFilterType
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class LayerDetails {

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

    /**
     * The type of layer these details apply to.
     *
     * <p>Optional. Can be used for layer-type-specific validation or processing.
     */
    private LayerType type;

    /**
     * Default style to use when no style is explicitly requested.
     *
     * <p>If specified, this style is used when the client doesn't specify a style in their
     * request. Must be one of the {@link #allowedStyles} if that set is non-empty.
     */
    private String defaultStyle;

    /**
     * CQL filter applied to read operations (GetMap, GetFeature, GetFeatureInfo).
     *
     * <p>Only features matching this filter are visible to the user. CQL syntax supports attribute
     * comparisons, spatial predicates, and placeholder substitution (e.g., {@code owner =
     * '${userName}'}).
     *
     * <p><b>Example:</b> {@code "status IN ('public', 'published') AND region = 'north'"}
     */
    private String cqlFilterRead;

    /**
     * CQL filter applied to write operations (Transaction: Insert, Update, Delete).
     *
     * <p>Only features matching this filter can be modified by the user. Typically more
     * restrictive than {@link #cqlFilterRead}.
     *
     * <p><b>Example:</b> {@code "owner = '${userName}' AND locked = false"}
     */
    private String cqlFilterWrite;

    /**
     * Geographic area constraint (MultiPolygon geometry).
     *
     * <p>If specified, only features within or intersecting this area are accessible, depending on
     * {@link #spatialFilterType}. Uses geolatte-geom library for infrastructure-agnostic geometry
     * representation.
     *
     * <p>Can be {@code null} for no spatial restriction.
     */
    private MultiPolygon<?> area;

    /**
     * How to apply the spatial area constraint.
     *
     * <p>Defaults to {@link SpatialFilterType#INTERSECT}.
     *
     * @see SpatialFilterType
     */
    @Default
    @NonNull
    private SpatialFilterType spatialFilterType = SpatialFilterType.INTERSECT;

    /**
     * How the layer appears in GetCapabilities responses.
     *
     * <p>Defaults to {@link CatalogMode#HIDE}.
     *
     * @see CatalogMode
     */
    @Default
    @NonNull
    private CatalogMode catalogMode = CatalogMode.HIDE;

    /**
     * Set of style names the user is permitted to use.
     *
     * <p>If empty, all styles are allowed. If non-empty, only listed styles can be requested.
     * Useful for preventing information disclosure through specialized styles or for enforcing
     * consistent visualization.
     *
     * <p>Immutable set. Never {@code null}.
     */
    @NonNull
    private Set<String> allowedStyles;

    /**
     * Set of layer attributes with their access permissions.
     *
     * <p>Defines column-level access control. Each {@link LayerAttribute} specifies whether an
     * attribute is hidden, read-only, or read-write.
     *
     * <p>If empty, all attributes are accessible according to the CQL filters. If non-empty, only
     * listed attributes are visible with their specified permissions.
     *
     * <p>Immutable set. Never {@code null}.
     *
     * @see LayerAttribute
     */
    @NonNull
    private Set<LayerAttribute> attributes;

    public static class Builder {
        // define (effectively overriding lombok's generated ones) only the builder methods for the
        // collection attributes we want to ensure are immutable
        private Set<String> allowedStyles = Set.of();
        private Set<LayerAttribute> attributes = Set.of();

        public Builder allowedStyles(Set<String> allowedStyles) {
            if (allowedStyles == null) {
                this.allowedStyles = Set.of();
            } else {
                this.allowedStyles = Set.copyOf(allowedStyles);
            }
            return this;
        }

        public Builder attributes(Set<LayerAttribute> attributes) {
            if (attributes == null) {
                this.attributes = Set.of();
            } else {
                this.attributes = Set.copyOf(attributes);
            }
            return this;
        }
    }
}
