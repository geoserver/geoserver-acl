/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.geolatte.geom.Geometry;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AccessInfo {

    /** Default "allow everything" AccessInfo */
    public static final AccessInfo ALLOW_ALL =
            AccessInfo.builder().grant(GrantType.ALLOW).build();

    /** Default "deny everything" AccessInfo */
    public static final AccessInfo DENY_ALL =
            AccessInfo.builder().grant(GrantType.DENY).build();

    /** The resulting grant: allow or deny. */
    @Default
    private GrantType grant = GrantType.DENY;

    private Geometry<?> area;

    private Geometry<?> clipArea;

    private CatalogMode catalogMode;

    private String defaultStyle;

    private String cqlFilterRead;

    private String cqlFilterWrite;

    private Set<LayerAttribute> attributes;

    private Set<String> allowedStyles;

    @Default
    @NonNull
    private List<String> matchingRules = List.of();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccessInfo[grant: ").append(grant);
        if (catalogMode != null) sb.append(", catalogMode: ").append(catalogMode);
        if (area != null) sb.append(", area: yes");
        if (clipArea != null) sb.append(", clipArea: yes");
        if (defaultStyle != null) sb.append(", def style: ").append(defaultStyle);
        if (null != allowedStyles && !allowedStyles.isEmpty())
            sb.append("allowed styles: ").append(allowedStyles);
        if (cqlFilterRead != null) sb.append(", cql read filter: present");
        if (cqlFilterWrite != null) sb.append(", cql write filter: present");
        if (null != attributes && !attributes.isEmpty())
            sb.append(", attributes: ").append(attributes.size());
        sb.append(", matchingRules: ").append(matchingRules);
        return sb.append("]").toString();
    }

    public static class Builder {
        // explicitly implement only mutators that need to ensure immutability
        // Ignore squid:S1068, private field required for the lombok-generated build() method
        @SuppressWarnings("squid:S1068")
        private Set<LayerAttribute> attributes = Set.of();

        @SuppressWarnings("squid:S1068")
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
