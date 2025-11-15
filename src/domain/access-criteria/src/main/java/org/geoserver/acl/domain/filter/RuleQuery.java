/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Query for filtering and paginating rules.
 *
 * <p>Supports keyset pagination (cursor-based) using rule IDs as continuation tokens.
 *
 * <p>Examples:
 * <pre>{@code
 * // All rules
 * RuleQuery<RuleFilter> query = RuleQuery.of();
 *
 * // With filter
 * RuleFilter filter = new RuleFilter(SpecialFilterType.ANY);
 * filter.setUser("admin");
 * RuleQuery<RuleFilter> query = RuleQuery.of(filter);
 *
 * // First page (10 results)
 * RuleQuery<RuleFilter> firstPage = RuleQuery.of(filter, 10, null);
 *
 * // Next page
 * RuleQuery<RuleFilter> nextPage = RuleQuery.of(filter, 10, "last-rule-id");
 * }</pre>
 *
 * @param <F> the type of filter to use (must extend {@link Filter})
 * @since 1.0
 * @see Filter
 */
@Data
@Accessors(chain = true)
public class RuleQuery<F extends Filter<?>> {

    /** Filter to apply (null matches all rules). */
    private F filter;

    /** Maximum number of results to return (null means no limit). */
    private Integer limit;

    /** ID of last rule from previous page for keyset pagination (null starts from beginning). */
    private String nextId;

    /**
     * Creates an empty query.
     *
     * @param <RF> the filter type
     * @return new RuleQuery with no filter or pagination
     */
    public static <RF extends Filter<?>> RuleQuery<RF> of() {
        return new RuleQuery<RF>();
    }

    /**
     * Creates a query with pagination.
     *
     * @param <RF> the filter type
     * @param limit max results
     * @param nextId continuation token from previous page
     * @return new RuleQuery
     */
    public static <RF extends Filter<?>> RuleQuery<RF> of(Integer limit, String nextId) {
        return new RuleQuery<RF>().setLimit(limit).setNextId(nextId);
    }

    /**
     * Creates a query with a filter.
     *
     * @param <RF> the filter type
     * @param filter the filter
     * @return new RuleQuery
     */
    public static <RF extends Filter<?>> RuleQuery<RF> of(RF filter) {
        return new RuleQuery<RF>().setFilter(filter);
    }

    /**
     * Creates a query with filter and pagination.
     *
     * @param <RF> the filter type
     * @param filter the filter
     * @param limit max results
     * @param nextId continuation token from previous page
     * @return new RuleQuery
     */
    public static <RF extends Filter<?>> RuleQuery<RF> of(RF filter, Integer limit, String nextId) {
        return new RuleQuery<RF>().setLimit(limit).setNextId(nextId).setFilter(filter);
    }

    /**
     * @return Optional containing the filter, or empty if none set
     */
    public Optional<F> getFilter() {
        return ofNullable(filter);
    }
}
