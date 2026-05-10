/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 * Domain model and services for data access rules.
 *
 * <p>A {@link org.geoserver.acl.domain.rules.Rule rule} controls access to GeoServer data
 * resources (workspaces, layers, services). Rules are evaluated in strict priority order; the
 * first rule whose {@link org.geoserver.acl.domain.rules.RuleIdentifier identifier} matches a
 * request determines the {@link org.geoserver.acl.domain.rules.GrantType decision} (ALLOW, DENY,
 * or LIMIT).
 *
 * <h2>Core types</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.domain.rules.Rule} - the rule itself: priority, identifier,
 *       optional {@link org.geoserver.acl.domain.rules.RuleLimits limits}.
 *   <li>{@link org.geoserver.acl.domain.rules.RuleIdentifier} - matching criteria (user, role,
 *       IP range, service, request, subfield, workspace, layer) and grant type.
 *   <li>{@link org.geoserver.acl.domain.rules.RuleLimits} - constraints attached to LIMIT rules:
 *       spatial restrictions and catalog visibility.
 *   <li>{@link org.geoserver.acl.domain.rules.LayerDetails} - per-layer ALLOW-rule details: CQL
 *       filters, allowed styles, attribute permissions, spatial constraints.
 *   <li>{@link org.geoserver.acl.domain.rules.LayerAttribute} - column-level access type
 *       (NONE, READONLY, READWRITE).
 *   <li>{@link org.geoserver.acl.domain.rules.RuleFilter} - query criteria for selecting rules.
 * </ul>
 *
 * <h2>Services and persistence</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.domain.rules.RuleAdminService} - CRUD plus priority management.
 *   <li>{@link org.geoserver.acl.domain.rules.RuleRepository} - storage abstraction; the domain
 *       defines the interface, infrastructure modules supply implementations (JPA, in-memory,
 *       REST client, caching).
 *   <li>{@link org.geoserver.acl.domain.rules.RuleEvent} - domain event published on rule
 *       create/update/delete for cache invalidation and clustered propagation.
 * </ul>
 *
 * <p>All value types in this package are immutable records exposing component accessors
 * ({@code rule.priority()}, {@code identifier.access()}); use {@code with*()} methods or
 * {@code toBuilder()} to derive modified copies.
 *
 * @see org.geoserver.acl.domain.adminrules
 * @see org.geoserver.acl.authorization
 */
@NullMarked
package org.geoserver.acl.domain.rules;

import org.jspecify.annotations.NullMarked;
