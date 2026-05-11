/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.persistence.jpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@Data
@Accessors(chain = true)
@Embeddable
public class JpaRuleLimits {

    @Column(name = "limits_area")
    private org.geolatte.geom.MultiPolygon<?> allowedArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "limits_spatial_filter_type", nullable = true)
    private JpaSpatialFilterType spatialFilterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "limits_catalog_mode", nullable = true)
    private JpaCatalogMode catalogMode;

    public JpaRuleLimits() {}

    public JpaRuleLimits(JpaRuleLimits other) {
        this.allowedArea = other.allowedArea;
        this.spatialFilterType = other.spatialFilterType;
        this.catalogMode = other.catalogMode;
    }

    boolean isEmpty() {
        return allowedArea == null && spatialFilterType == null && catalogMode == null;
    }
}
