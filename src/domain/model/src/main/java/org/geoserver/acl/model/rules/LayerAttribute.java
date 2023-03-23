/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.model.rules;

import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class LayerAttribute {

    public enum AccessType {

        /** No access to the resource. */
        NONE,

        /** Read only access. */
        READONLY,

        /** Full access. */
        READWRITE
    }

    private String name;

    private String dataType; // should be an enum?

    private AccessType access;

    public static LayerAttribute none() {
        return LayerAttribute.builder().access(AccessType.NONE).build();
    }

    public static LayerAttribute read() {
        return LayerAttribute.builder().access(AccessType.READONLY).build();
    }

    public static LayerAttribute write() {
        return LayerAttribute.builder().access(AccessType.READWRITE).build();
    }
}
