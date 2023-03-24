/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import org.geolatte.geom.MultiPolygon;

import java.util.Set;

/**
 * @since 1.0
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class LayerDetails {

    public enum LayerType {
        VECTOR,
        RASTER,
        LAYERGROUP
    }

    private LayerType type;

    private String defaultStyle;

    private String cqlFilterRead;

    private String cqlFilterWrite;

    private MultiPolygon<?> area;

    @Default @NonNull private SpatialFilterType spatialFilterType = SpatialFilterType.INTERSECT;

    @Default @NonNull private CatalogMode catalogMode = CatalogMode.HIDE;

    @NonNull private Set<String> allowedStyles;

    @NonNull private Set<LayerAttribute> attributes;

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
