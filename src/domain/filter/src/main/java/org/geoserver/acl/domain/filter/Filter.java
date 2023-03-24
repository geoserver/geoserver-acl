/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter;

import java.util.function.Predicate;

@FunctionalInterface
public interface Filter<R> extends Predicate<R> {}
