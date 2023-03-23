/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Data
@Accessors(chain = true)
@Embeddable
public class RuleLimits implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    @Column(name = "limits_area")
    private org.geolatte.geom.MultiPolygon<?> allowedArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "limits_spatial_filter_type", nullable = true)
    private SpatialFilterType spatialFilterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "limits_catalog_mode", nullable = true)
    private CatalogMode catalogMode;

    boolean isEmpty() {
        return allowedArea == null && spatialFilterType == null && catalogMode == null;
    }

    @SneakyThrows(CloneNotSupportedException.class)
    public @Override RuleLimits clone() {
        return (RuleLimits) super.clone();
    }
}
