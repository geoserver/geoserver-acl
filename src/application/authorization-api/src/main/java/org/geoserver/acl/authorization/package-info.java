/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/**
 * Authorization service contract: evaluate access rules to make access decisions.
 *
 * <p>Given a request context (user, roles, IP, service / request / workspace / layer), the
 * {@link org.geoserver.acl.authorization.AuthorizationService} consults the data access
 * {@link org.geoserver.acl.domain.rules.Rule rules} and workspace
 * {@link org.geoserver.acl.domain.adminrules.AdminRule admin rules} to produce structured
 * decisions: an {@link org.geoserver.acl.authorization.AccessInfo}, an
 * {@link org.geoserver.acl.authorization.AdminAccessInfo}, or an
 * {@link org.geoserver.acl.authorization.AccessSummary} of the workspaces and layers a user can
 * see.
 *
 * <h2>Requests</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.authorization.AccessRequest} - data access query (user / roles /
 *       IP / service / request / subfield / workspace / layer).
 *   <li>{@link org.geoserver.acl.authorization.AdminAccessRequest} - workspace admin query.
 *   <li>{@link org.geoserver.acl.authorization.AccessSummaryRequest} - per-user summary of
 *       visible workspaces and layers.
 * </ul>
 *
 * <h2>Responses</h2>
 * <ul>
 *   <li>{@link org.geoserver.acl.authorization.AccessInfo} - resolved data access decision: grant
 *       type, spatial filters, CQL filters, allowed styles, attribute permissions, catalog mode,
 *       matching rule IDs.
 *   <li>{@link org.geoserver.acl.authorization.AdminAccessInfo} - whether the user has admin on
 *       the requested workspace.
 *   <li>{@link org.geoserver.acl.authorization.AccessSummary} - per-workspace
 *       {@link org.geoserver.acl.authorization.WorkspaceAccessSummary summary} of layers a user
 *       can or cannot see.
 * </ul>
 *
 * <h2>Composition</h2>
 *
 * <p>{@link org.geoserver.acl.authorization.ForwardingAuthorizationService} is a delegating base
 * for decorators (caching, instrumentation) layered over a concrete service implementation.
 *
 * @see org.geoserver.acl.domain.rules
 * @see org.geoserver.acl.domain.adminrules
 */
@NullMarked
package org.geoserver.acl.authorization;

import org.jspecify.annotations.NullMarked;
