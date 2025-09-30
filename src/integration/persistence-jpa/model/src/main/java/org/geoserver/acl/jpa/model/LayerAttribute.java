/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import java.io.Serial;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Data
@Accessors(chain = true)
@Embeddable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "LayerAttribute")
public class LayerAttribute implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum AccessType {

        /** No access to the resource. */
        NONE,

        /** Read only access. */
        READONLY,

        /** Full access. */
        READWRITE
    }

    @Column(nullable = false)
    private String name;

    @Column(name = "data_type")
    private String dataType; // should be an enum?

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = true /* false */)
    private AccessType access;

    public @Override LayerAttribute clone() {
        try {
            return (LayerAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
