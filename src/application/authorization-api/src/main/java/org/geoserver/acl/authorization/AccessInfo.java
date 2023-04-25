/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import org.geolatte.geom.Geometry;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;

import java.util.List;
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

    private Geometry<?> area;

    private Geometry<?> clipArea;

    private CatalogMode catalogMode;

    private String defaultStyle;

    private String cqlFilterRead;

    private String cqlFilterWrite;

    private Set<LayerAttribute> attributes;

    private Set<String> allowedStyles;

    @Default @NonNull private List<String> matchingRules = List.of();

    public String toShortString() {
        return String.format(
                "AccessInfo[grant: %s, catalogMode: %s, area: %s, clip: %s, styles[def: %s, allowed: %s], cql[r: %s, w: %s], atts: %s",
                grant,
                catalogMode,
                area == null ? null : "present",
                clipArea == null ? null : "present",
                defaultStyle,
                allowedStyles == null ? null : allowedStyles.size(),
                cqlFilterRead == null ? null : "present",
                cqlFilterWrite == null ? null : "present",
                attributes == null ? null : attributes.size());
    }

    public static class Builder {
        // explicitly implement only mutators that need to ensure immutability
        private Set<LayerAttribute> attributes = Set.of();
        private Set<String> allowedStyles = Set.of();

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
