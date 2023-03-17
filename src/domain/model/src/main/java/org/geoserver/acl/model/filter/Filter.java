/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class Filter {
    public static final String COLLECTION_VALUE_SEPARATOR = ",";

    protected String sortNames(String s) {
        if (s.contains(COLLECTION_VALUE_SEPARATOR)) {
            s =
                    asCollectionValue(s).stream()
                            .collect(Collectors.joining(COLLECTION_VALUE_SEPARATOR));
        }
        return s;
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
