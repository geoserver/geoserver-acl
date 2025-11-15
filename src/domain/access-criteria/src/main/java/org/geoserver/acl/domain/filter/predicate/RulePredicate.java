/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;

/**
 * Base class for rule filtering predicates.
 *
 * <p>Each predicate has a {@link FilterType} that determines its matching strategy:
 * <ul>
 *   <li><b>ANY:</b> Match all rules
 *   <li><b>DEFAULT:</b> Match only rules with null field value (catch-all rules)
 *   <li><b>NAMEVALUE:</b> Match rules with a specific field value
 * </ul>
 *
 * <p>The {@code includeDefault} flag controls whether NAMEVALUE predicates also match DEFAULT
 * rules. When true, a predicate matching "admin" will also match rules with null username.
 *
 * <p>Implementations: {@link TextFilter} for strings, {@link InSetPredicate} for sets,
 * {@link IPAddressRangeFilter} for IP ranges.
 *
 * @param <T> the type of value being tested (e.g., String, Set)
 * @since 1.0
 * @see FilterType
 */
@EqualsAndHashCode
public abstract class RulePredicate<T> implements Predicate<T>, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 6565336016075974626L;

    /** Filter matching strategy (ANY, DEFAULT, or NAMEVALUE). */
    protected FilterType type;

    /**
     * Whether to include default (null) rules when matching specific values.
     * <p>Only applies to NAMEVALUE. When true, matching "admin" also matches rules with null
     * in that field. When false, only exact matches are returned.
     */
    protected boolean includeDefault = true;

    public RulePredicate(FilterType type) {
        this.type = type;
    }

    public RulePredicate(FilterType type, boolean includeDefault) {
        this.type = type;
        this.includeDefault = includeDefault;
    }

    public void setType(SpecialFilterType type) {
        this.type = type.getRelatedType();
    }

    public FilterType getType() {
        return type;
    }

    public boolean isIncludeDefault() {
        return includeDefault;
    }

    public void setIncludeDefault(boolean includeDefault) {
        this.includeDefault = includeDefault;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RulePredicate<T> clone() throws CloneNotSupportedException {
        return (RulePredicate<T>) super.clone();
    }
}
