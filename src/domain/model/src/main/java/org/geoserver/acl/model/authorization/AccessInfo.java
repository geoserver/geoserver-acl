/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.model.authorization;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.With;

import org.geolatte.geom.Geometry;
import org.geoserver.acl.model.rules.CatalogMode;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.LayerAttribute;

import java.util.Set;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AccessInfo {

    /** Default "allow everything" AccessInfo */
    public static final AccessInfo ALLOW_ALL = AccessInfo.builder().grant(GrantType.ALLOW).build();

    /** Default "deny everything" AccessInfo */
    public static final AccessInfo DENY_ALL = AccessInfo.builder().grant(GrantType.DENY).build();

    /** The resulting grant: allow or deny. */
    @Default private GrantType grant = GrantType.DENY;

    @Default private boolean adminRights = false;

    private Geometry<?> area;

    private Geometry<?> clipArea;

    private CatalogMode catalogMode;

    private String defaultStyle;

    private String cqlFilterRead;

    private String cqlFilterWrite;

    private Set<LayerAttribute> attributes;

    private Set<String> allowedStyles;

    public static class Builder {
        // explicitly implement only mutators that need to ensure immutability
        private Set<LayerAttribute> attributes;
        private Set<String> allowedStyles;

        public Builder attributes(Set<LayerAttribute> value) {
            this.attributes = value == null ? null : Set.copyOf(value);
            return this;
        }

        public Builder allowedStyles(Set<String> value) {
            this.allowedStyles = value == null ? null : Set.copyOf(value);
            return this;
        }
    }
}
