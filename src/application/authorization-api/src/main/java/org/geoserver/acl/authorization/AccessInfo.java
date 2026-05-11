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
import lombok.With;
import org.geolatte.geom.Geometry;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.GrantType;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.domain.rules.SpatialFilterType;
import org.jspecify.annotations.Nullable;

/**
 * Resolved data access decision produced by
 * {@link AuthorizationService#getAccessInfo(AccessRequest)}.
 *
 * <p>Carries the effective grant for an {@link AccessRequest} together with the restrictions that
 * apply to it: spatial intersect and clip areas, catalog mode, default and allowed styles, read
 * and write CQL filters, attribute-level permissions, and the identifiers of the rules that
 * contributed to the decision. Two well-known constants, {@link #ALLOW_ALL} and {@link #DENY_ALL},
 * represent unrestricted access and full denial.
 *
 * @param grant the effective {@link GrantType} for the request; {@link GrantType#ALLOW} permits
 *     access subject to the other restrictions, {@link GrantType#DENY} forbids it.
 * @param intersectArea the allowed area applied with {@link SpatialFilterType#INTERSECT} semantics,
 *     returning whole features that intersect this geometry. {@code null} when no intersect
 *     spatial restriction applies.
 * @param clipArea the allowed area applied with {@link SpatialFilterType#CLIP} semantics, clipping
 *     features to this geometry's boundary. {@code null} when no clip spatial restriction
 *     applies.
 * @param catalogMode the {@link CatalogMode} controlling how the catalog responds for the targeted
 *     resource (e.g., HIDE, CHALLENGE, MIXED). {@code null} when no catalog mode is enforced.
 * @param defaultStyle the name of the default style to render the layer with. {@code null} when
 *     no default style is imposed.
 * @param cqlFilterRead the CQL filter to apply to read operations for row-level security.
 *     {@code null} when no read filter applies.
 * @param cqlFilterWrite the CQL filter to apply to write operations for row-level security.
 *     {@code null} when no write filter applies.
 * @param attributes the per-attribute read/write permissions ({@link LayerAttribute}) that
 *     restrict feature attribute visibility; empty when no attribute-level restriction applies.
 * @param allowedStyles the set of style names the user is allowed to use for the layer; empty
 *     when all styles are allowed.
 * @param matchingRules the identifiers of the {@link org.geoserver.acl.domain.rules.Rule}s that
 *     contributed to this decision, in evaluation order.
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record AccessInfo(
        GrantType grant,
        @Nullable Geometry<?> intersectArea,
        @Nullable Geometry<?> clipArea,
        @Nullable CatalogMode catalogMode,
        @Nullable String defaultStyle,
        @Nullable String cqlFilterRead,
        @Nullable String cqlFilterWrite,
        Set<LayerAttribute> attributes,
        Set<String> allowedStyles,
        List<String> matchingRules) {

    /** Default "allow everything" AccessInfo */
    public static final AccessInfo ALLOW_ALL =
            AccessInfo.builder().grant(GrantType.ALLOW).build();

    /** Default "deny everything" AccessInfo */
    public static final AccessInfo DENY_ALL =
            AccessInfo.builder().grant(GrantType.DENY).build();

    public AccessInfo {
        if (grant == null) {
            grant = GrantType.DENY;
        }
        attributes = attributes == null ? Set.of() : Set.copyOf(attributes);
        allowedStyles = allowedStyles == null ? Set.of() : Set.copyOf(allowedStyles);
        matchingRules = matchingRules == null ? List.of() : List.copyOf(matchingRules);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccessInfo[grant: ").append(grant);
        if (catalogMode != null) sb.append(", catalogMode: ").append(catalogMode);
        if (intersectArea != null) sb.append(", intersectArea: yes");
        if (clipArea != null) sb.append(", clipArea: yes");
        if (defaultStyle != null) sb.append(", def style: ").append(defaultStyle);
        if (!allowedStyles.isEmpty()) sb.append("allowed styles: ").append(allowedStyles);
        if (cqlFilterRead != null) sb.append(", cql read filter: present");
        if (cqlFilterWrite != null) sb.append(", cql write filter: present");
        if (!attributes.isEmpty()) sb.append(", attributes: ").append(attributes.size());
        sb.append(", matchingRules: ").append(matchingRules);
        return sb.append("]").toString();
    }
}
