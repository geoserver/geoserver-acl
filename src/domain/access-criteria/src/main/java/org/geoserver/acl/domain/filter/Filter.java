/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter;

import java.util.function.Predicate;

/**
 * Predicate for filtering rules in ACL queries.
 *
 * <p>Filters can be combined using standard {@code Predicate} methods:
 * <pre>{@code
 * Filter<Rule> userFilter = rule -> rule.getUsername().equals("admin");
 * Filter<Rule> roleFilter = rule -> rule.getRolename().equals("ROLE_ADMIN");
 * Filter<Rule> combined = userFilter.and(roleFilter);
 * }</pre>
 *
 * @param <R> the type of rule being filtered (e.g., {@code Rule}, {@code AdminRule})
 * @since 1.0
 * @see RuleQuery
 * @see Predicate
 */
@FunctionalInterface
public interface Filter<R> extends Predicate<R> {}
