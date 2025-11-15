/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.filter.predicate;

/**
 * Filter matching strategy for rule predicates.
 *
 * <p>Determines how predicates match against rule values, with special handling for wildcards
 * and null values.
 *
 * @since 1.0
 * @see RulePredicate
 * @see SpecialFilterType
 */
public enum FilterType {
    /** Match a specific value (e.g., username="admin"). */
    NAMEVALUE,

    /** Match by ID (used in specialized scenarios). */
    IDVALUE,

    /**
     * Match all rules (wildcard, equivalent to "*").
     * @see SpecialFilterType#ANY
     */
    ANY,

    /**
     * Match only rules where the field is null (catch-all rules).
     * <p>Default rules act as fallbacks. For example, a rule with username=null matches
     * any user not explicitly mentioned in other rules.
     * @see SpecialFilterType#DEFAULT
     */
    DEFAULT;
}
