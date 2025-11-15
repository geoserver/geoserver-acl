/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class IPAddressRange implements Cloneable {

    public static final long NULL = -1L;

    @Column(nullable = false)
    private long low = NULL;

    @Column(nullable = false)
    private long high = NULL;

    @Column(nullable = false)
    private int size = (int) NULL;

    public @Override IPAddressRange clone() {
        return new IPAddressRange(low, high, size);
    }

    /**
     * @return a new no_data value instance with all fields set to {@code -1}
     */
    public static IPAddressRange noData() {
        return new IPAddressRange();
    }

    public boolean isEmpty() {
        return NULL == low && NULL == high && NULL == size;
    }
}
