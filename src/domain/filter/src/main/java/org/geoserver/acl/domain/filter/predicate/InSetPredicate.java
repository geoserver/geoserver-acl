/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

/** Contains a fixed text OR a special filtering condition (i.e. ANY, DEFAULT). */
@EqualsAndHashCode(callSuper = true)
public class InSetPredicate<V> extends RulePredicate<Set<V>> implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 6565336016075974626L;

    private static final String COLLECTION_VALUE_SEPARATOR = ",";

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
            setValues((Set<V>) asCollectionValue(text));
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
        setValues(name == null ? null : (Set<V>) asCollectionValue(name));
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
        return test((Set<V>) asCollectionValue(value));
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

    public static String asTextValue(Collection<String> values) {
        return values.stream().collect(Collectors.joining(COLLECTION_VALUE_SEPARATOR));
    }

    public static SortedSet<String> asCollectionValue(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySortedSet();
        }
        if (value.contains(COLLECTION_VALUE_SEPARATOR)) {
            return Arrays.stream(value.split(COLLECTION_VALUE_SEPARATOR))
                    .map(n -> n.trim())
                    .filter(n -> !n.isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        return new TreeSet<>(Set.of(value));
    }
}
