/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter;

import static java.util.Optional.ofNullable;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Optional;

@Data
@Accessors(chain = true)
public class RuleQuery<F extends Filter<?>> {

    private F filter;

    private Integer limit;
    private String nextId;

    public static <RF extends Filter<?>> RuleQuery<RF> of() {
        return new RuleQuery<RF>();
    }

    public static <RF extends Filter<?>> RuleQuery<RF> of(Integer limit, String nextId) {
        return new RuleQuery<RF>().setLimit(limit).setNextId(nextId);
    }

    public static <RF extends Filter<?>> RuleQuery<RF> of(RF filter) {
        return new RuleQuery<RF>().setFilter(filter);
    }

    public static <RF extends Filter<?>> RuleQuery<RF> of(RF filter, Integer limit, String nextId) {
        return new RuleQuery<RF>().setLimit(limit).setNextId(nextId).setFilter(filter);
    }

    public Optional<F> getFilter() {
        return ofNullable(filter);
    }
}
