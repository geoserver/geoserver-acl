/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 * Domain model and services for workspace administration rules.
 *
 * <p>An {@link org.geoserver.acl.domain.adminrules.AdminRule admin rule} controls who can manage
 * GeoServer workspaces. Unlike data access {@link org.geoserver.acl.domain.rules.Rule rules},
 * admin rules grant or withhold administrative privilege ({@link
 * org.geoserver.acl.domain.adminrules.AdminGrantType ADMIN or USER}) on a per-workspace basis,
 * and use a smaller match surface (no service / request / layer fields).
 *
 * <h2>Core types</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRule} - the rule: priority, identifier,
 *       grant type.
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRuleIdentifier} - matching criteria
 *       (username, role, IP range, workspace).
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminGrantType} - ADMIN or USER.
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRuleFilter} - query criteria.
 * </ul>
 *
 * <h2>Services and persistence</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRuleAdminService} - CRUD plus priority
 *       management.
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRuleRepository} - storage abstraction
 *       implemented by infrastructure modules.
 *   <li>{@link org.geoserver.acl.domain.adminrules.AdminRuleEvent} - domain event for
 *       create/update/delete.
 * </ul>
 *
 * <p>Value types are immutable records exposing component accessors; use {@code with*()} methods
 * or {@code toBuilder()} for modifications.
 *
 * @see org.geoserver.acl.domain.rules
 * @see org.geoserver.acl.authorization
 */
@NullMarked
package org.geoserver.acl.domain.adminrules;

import org.jspecify.annotations.NullMarked;
