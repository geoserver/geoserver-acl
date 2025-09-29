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

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class RuleLimits {

    public static final SpatialFilterType DEFAULT_SPATIAL_FILTERTYPE = SpatialFilterType.INTERSECT;
    public static final CatalogMode DEFAULT_CATALOG_MODE = CatalogMode.HIDE;

    private MultiPolygon<?> allowedArea;

    @Default
    @NonNull
    private SpatialFilterType spatialFilterType = DEFAULT_SPATIAL_FILTERTYPE;

    @Default
    @NonNull
    private CatalogMode catalogMode = DEFAULT_CATALOG_MODE;

    public static RuleLimits clip() {
        return RuleLimits.builder().spatialFilterType(SpatialFilterType.CLIP).build();
    }

    public static RuleLimits intersect() {
        return RuleLimits.builder()
                .spatialFilterType(SpatialFilterType.INTERSECT)
                .build();
    }
}
