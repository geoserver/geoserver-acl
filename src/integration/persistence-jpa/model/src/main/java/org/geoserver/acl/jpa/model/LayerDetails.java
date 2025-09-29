/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import static java.util.Objects.isNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.UniqueConstraint;
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
public class LayerDetails implements Serializable, Cloneable {

    private static final long serialVersionUID = 1;

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
    private SpatialFilterType spatialFilterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ld_catalog_mode", nullable = true)
    private CatalogMode catalogMode;

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
    private Set<LayerAttribute> attributes;

    public @Override LayerDetails clone() {
        LayerDetails clone;

        try {
            clone = (LayerDetails) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (allowedStyles != null) {
            clone.allowedStyles = new HashSet<>(allowedStyles);
        }
        if (attributes != null) {
            clone.attributes =
                    attributes.stream().map(LayerAttribute::clone).collect(Collectors.toCollection(HashSet::new));
        }
        return clone;
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
