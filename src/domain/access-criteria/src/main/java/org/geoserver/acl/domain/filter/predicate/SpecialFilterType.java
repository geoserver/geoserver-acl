/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.filter.predicate;

/**
 * Shorthand for configuring a {@link RulePredicate} to either match any value or only default
 * (null) values on its target field.
 *
 * <p>Used by rule filter constructors and by {@code setHeuristically} parsing as a high-level
 * alternative to setting a {@link FilterType} directly: each value maps to the corresponding
 * {@link FilterType} via {@link #getRelatedType()}.
 *
 * @see FilterType
 * @see RulePredicate
 */
public enum SpecialFilterType {

    /**
     * ANY will not add any constraints on the field. This means that all values on that field will
     * be allowed. Pay attention when using this type, since it may allow rules you do not intended
     * to retrieve. You may mean to use the ANY value.
     */
    ANY(FilterType.ANY),
    /**
     * DEFAULT will create an IS NULL filter on the field. This means only the default rules for a
     * given field will be returned. This is probably the value you want to use.
     */
    DEFAULT(FilterType.DEFAULT);

    private FilterType relatedType;

    private SpecialFilterType(FilterType relatedType) {
        this.relatedType = relatedType;
    }

    public FilterType getRelatedType() {
        return relatedType;
    }
}
