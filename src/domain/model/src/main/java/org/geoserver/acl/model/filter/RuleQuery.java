/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.filter;

import static java.util.Optional.ofNullable;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.OptionalInt;

@Data
@Accessors(chain = true)
public class RuleQuery<F extends Filter> {

    private F filter;

    private Integer pageNumber;
    private Integer pageSize;

    public static <RF extends Filter> RuleQuery<RF> of(RF filter) {
        return new RuleQuery<RF>().setFilter(filter);
    }

    public static <RF extends Filter> RuleQuery<RF> of() {
        return new RuleQuery<RF>();
    }

    public static <RF extends Filter> RuleQuery<RF> of(Integer pageNumber, Integer pageSize) {
        return new RuleQuery<RF>().setPageNumber(pageNumber).setPageSize(pageSize);
    }

    public Optional<F> getFilter() {
        return ofNullable(filter);
    }

    public OptionalInt pageNumber() {
        return pageNumber == null ? OptionalInt.empty() : OptionalInt.of(pageNumber);
    }

    public OptionalInt pageSize() {
        return pageSize == null ? OptionalInt.empty() : OptionalInt.of(pageSize);
    }
}
