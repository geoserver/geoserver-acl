/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.filter.predicate;

import lombok.EqualsAndHashCode;

import org.geoserver.acl.model.filter.RuleFilter;

import java.io.Serializable;
import java.util.Set;

/** Contains a fixed text OR a special filtering condition (i.e. ANY, DEFAULT). */
@EqualsAndHashCode(callSuper = true)
public class InSetPredicate<V> extends RulePredicate<Set<V>> implements Serializable, Cloneable {

    private static final long serialVersionUID = 6565336016075974626L;
    private Set<V> values = Set.of();

    public InSetPredicate(FilterType type) {
        super(type);
    }

    public InSetPredicate(FilterType type, boolean includeDefault) {
        super(type, includeDefault);
    }

    public InSetPredicate(Set<V> values, boolean includeDefault) {
        this(values);
        setIncludeDefault(includeDefault);
    }

    public InSetPredicate(Set<V> values) {
        super(FilterType.NAMEVALUE);
        this.values = values == null ? Set.of() : Set.copyOf(values);
    }

    @SuppressWarnings("unchecked")
    public void setHeuristically(String text) {
        if (text == null) {
            this.type = FilterType.DEFAULT;
        } else if (text.equals("*")) {
            this.type = FilterType.ANY;
        } else {
            setValues((Set<V>) RuleFilter.asCollectionValue(text));
        }
    }

    public void setHeuristically(Set<V> roles) {
        if (roles == null || roles.isEmpty()) {
            this.values = Set.of();
            this.type = FilterType.DEFAULT;
        } else if (roles.contains("*")) {
            this.values = Set.of();
            this.type = FilterType.ANY;
        } else {
            this.setValues(roles);
        }
    }

    @SuppressWarnings("unchecked")
    public void setText(String name) {
        setValues(name == null ? null : (Set<V>) RuleFilter.asCollectionValue(name));
    }

    public void setValues(Set<V> values) {
        this.type = FilterType.NAMEVALUE;
        this.values = values == null ? Set.of() : Set.copyOf(values);
    }

    public Set<V> getValues() {
        return values;
    }

    @Override
    public String toString() {
        switch (type) {
            case ANY:
            case DEFAULT:
                return type.toString();
            case NAMEVALUE:
                return values + (includeDefault ? "+" : "");
            case IDVALUE:
            default:
                throw new AssertionError();
        }
    }

    @Override
    public InSetPredicate<V> clone() throws CloneNotSupportedException {
        return (InSetPredicate<V>) super.clone();
    }

    @SuppressWarnings("unchecked")
    public boolean test(String value) {
        return test((Set<V>) RuleFilter.asCollectionValue(value));
    }

    @Override
    public boolean test(Set<V> values) {
        switch (type) {
            case ANY:
                return true;
            case DEFAULT:
                return values == null || values.isEmpty();
            case NAMEVALUE:
                Set<V> filter = getValues() == null ? Set.of() : getValues();
                values = values == null ? Set.of() : values;
                if (this.isIncludeDefault()) {
                    return values.isEmpty() || values.stream().anyMatch(filter::contains);
                }
                return values.stream().anyMatch(filter::contains);
            case IDVALUE:
            default:
                throw new IllegalArgumentException();
        }
    }
}
