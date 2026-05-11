/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.persistence.jpa.domain;

import static java.util.Objects.isNull;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.geolatte.geom.MultiPolygon;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@Embeddable
public class JpaLayerDetails {

    public enum LayerType {
        VECTOR,
        RASTER,
        LAYERGROUP
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "ld_type", nullable = true /* false */)
    private LayerType type;

    @Column(name = "ld_default_style")
    private String defaultStyle;

    @Column(name = "ld_cql_filter_read", length = 65535)
    private String cqlFilterRead;

    @Column(name = "ld_cql_filter_write", length = 65535)
    private String cqlFilterWrite;

    @Column(name = "ld_area")
    private MultiPolygon<?> area;

    @Enumerated(EnumType.STRING)
    @Column(name = "ld_spatial_filter_type", nullable = true)
    private JpaSpatialFilterType spatialFilterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ld_catalog_mode", nullable = true)
    private JpaCatalogMode catalogMode;

    /** Styles allowed for this layer */
    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(
            name = "acl_layer_styles",
            joinColumns = @JoinColumn(name = "details_id", foreignKey = @ForeignKey(name = "fk_styles_layer")))
    @Column(name = "ld_styleName")
    private Set<String> allowedStyles;

    @ElementCollection(fetch = FetchType.LAZY)
    @JoinTable(
            name = "acl_layer_attributes",
            joinColumns = @JoinColumn(name = "details_id"),
            uniqueConstraints =
                    @UniqueConstraint(
                            name = "acl_layer_attributes_name",
                            columnNames = {"details_id", "name"}),
            foreignKey = @ForeignKey(name = "fk_attribute_layer"))
    @Fetch(FetchMode.SELECT)
    private Set<JpaLayerAttribute> attributes;

    public JpaLayerDetails() {}

    public JpaLayerDetails(JpaLayerDetails other) {
        this.type = other.type;
        this.defaultStyle = other.defaultStyle;
        this.cqlFilterRead = other.cqlFilterRead;
        this.cqlFilterWrite = other.cqlFilterWrite;
        this.area = other.area;
        this.spatialFilterType = other.spatialFilterType;
        this.catalogMode = other.catalogMode;
        this.allowedStyles = other.allowedStyles == null ? null : new HashSet<>(other.allowedStyles);
        this.attributes = other.attributes == null
                ? null
                : other.attributes.stream().map(JpaLayerAttribute::new).collect(Collectors.toCollection(HashSet::new));
    }

    public boolean isEmpty() {
        return isNull(type)
                && isNull(defaultStyle)
                && isNull(cqlFilterRead)
                && isNull(cqlFilterWrite)
                && isNull(area)
                && isNull(spatialFilterType)
                && isNull(catalogMode)
                && (isNull(allowedStyles) || allowedStyles.isEmpty())
                && (isNull(attributes) || attributes.isEmpty());
    }
}
