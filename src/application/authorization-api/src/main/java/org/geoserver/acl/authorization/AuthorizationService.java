/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import java.util.List;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.rules.Rule;

/**
 * Primary application port for GeoServer access control authorization.
 *
 * <p>The {@code AuthorizationService} is the central interface for determining access control
 * decisions in GeoServer. It evaluates authorization requests against configured access control
 * rules and returns authorization decisions with applicable restrictions.
 *
 * <h2>Rule-Based Access Control</h2>
 *
 * <p>The service evaluates two types of rules to make authorization decisions:
 *
 * <ul>
 *   <li><strong>Data Access {@linkplain Rule}s</strong>: Control access to GeoServer data (layers,
 *       workspaces). Can specify spatial filters, attribute filters, CQL filters, and catalog
 *       visibility modes.
 *   <li><strong>Workspace {@linkplain AdminRule}s</strong>: Control administrative access to workspaces,
 *       determining whether a user can modify workspace configuration.
 * </ul>
 *
 * <p>Rules are matched against request context (user, roles, IP address, service, workspace, layer,
 * etc.) and evaluated to produce an authorization decision. When multiple rules apply, the service
 * resolves them to determine the effective access rights and restrictions.
 *
 * <h2>Authorization Decisions</h2>
 *
 * <p>Authorization decisions include:
 *
 * <ul>
 *   <li><strong>Grant/Deny</strong>: Whether access is allowed or denied
 *   <li><strong>Spatial Restrictions</strong>: Geographic boundaries for data access
 *   <li><strong>Attribute Access</strong>: Which feature attributes can be read or written
 *   <li><strong>CQL Filters</strong>: Row-level security filters
 *   <li><strong>Style Restrictions</strong>: Which visualization styles are allowed
 *   <li><strong>Catalog Mode</strong>: The security mode in which the GeoServer Catalog should respond to requests for a specific resource (layer/feature type/coverage)
 * </ul>
 *
 * <h2>Use Cases</h2>
 *
 * <ul>
 *   <li><strong>{@link #getAccessInfo(AccessRequest)}</strong>: Evaluate a specific access request
 *       and return detailed authorization decision
 *   <li><strong>{@link #getAdminAuthorization(AdminAccessRequest)}</strong>: Determine workspace
 *       administrative rights
 *   <li><strong>{@link #getUserAccessSummary(AccessSummaryRequest)}</strong>: Get overview of all
 *       accessible resources for building catalogs and UIs
 *   <li><strong>{@link #getMatchingRules(AccessRequest)}</strong>: Retrieve raw matching rules for
 *       debugging and auditing
 * </ul>
 *
 * <h2>Clean Architecture Role</h2>
 *
 * <p>This interface is a <strong>Primary Port</strong> in the Application Layer. It:
 *
 * <ul>
 *   <li>Defines authorization use cases independent of delivery mechanism
 *   <li>Depends only on domain layer types (rules, filters, decisions)
 *   <li>Can be invoked locally or adapted for remote access (REST API, etc.)
 * </ul>
 *
 * @see Rule
 * @see AdminRule
 * @see AccessInfo
 * @see AdminAccessInfo
 * @see AccessSummary
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence's
 *     RuleReaderService)
 * @author Gabriel Roldan - Camptocamp
 */
public interface AuthorizationService {

    /**
     * Evaluates an access request and returns the authorization decision with applicable
     * restrictions.
     *
     * <p>This is the main authorization method. It evaluates applicable {@link Rule}s for the given
     * request context and returns an {@link AccessInfo} object describing:
     *
     * <ul>
     *   <li><strong>Grant Decision</strong>: Whether access is allowed or denied
     *   <li><strong>Spatial Restrictions</strong>: Geographic boundaries for allowed data access
     *   <li><strong>Attribute Access</strong>: Which feature attributes can be read or written
     *   <li><strong>CQL Filters</strong>: Row-level security filters for read and write operations
     *   <li><strong>Style Restrictions</strong>: Allowed visualization styles and default style
     *   <li><strong>Catalog Mode</strong>: How the resource appears in catalogs (HIDE, CHALLENGE, MIXED)
     *   <li><strong>Matching Rules</strong>: IDs of rules that contributed to the decision
     * </ul>
     *
     * <p>When multiple rules apply to the request, they are evaluated to determine the effective
     * access rights. Rules are matched against request parameters including user, roles, IP address,
     * service, workspace, and layer.
     *
     * @param request the access request containing user, roles, IP address, service, workspace,
     *     layer, and other authorization context
     * @return authorization decision with applicable restrictions, never {@code null}. Returns {@link
     *     AccessInfo#DENY_ALL} if access is denied
     * @throws IllegalArgumentException if {@link AccessRequest#validate() request} is invalid
     * @see #getMatchingRules(AccessRequest) to retrieve raw matching rules for debugging
     */
    AccessInfo getAccessInfo(AccessRequest request);

    /**
     * Evaluates administrative access rights for a workspace.
     *
     * <p>Determines whether the user has administrative privileges on the specified workspace by
     * evaluating applicable {@link AdminRule}s. Admin rules control configuration-level access
     * (creating/modifying layers, changing workspace settings) as opposed to data-level access.
     *
     * <p>Rules are matched against the request context including user, roles, IP address, and target
     * workspace.
     *
     * @param request the admin access request containing user, roles, IP address, and target
     *     workspace
     * @return admin access information with {@code admin} boolean flag indicating whether the user
     *     has administrative privileges. Never {@code null}; defaults to non-admin access when no
     *     applicable admin rules grant privileges
     * @throws IllegalArgumentException if {@link AdminAccessRequest#validate() request} is invalid
     */
    AdminAccessInfo getAdminAuthorization(AdminAccessRequest request);

    /**
     * Computes an access summary across all workspaces for a given user.
     *
     * <p>This method provides an efficient overview of which workspaces and layers a user can
     * access, without requiring individual authorization checks for each resource. It's useful for:
     *
     * <ul>
     *   <li>Building catalog and layer tree listings in user interfaces
     *   <li>Filtering capabilities documents for WMS/WFS/WCS services
     *   <li>Pre-computing accessible resources for dashboards and reporting
     * </ul>
     *
     * <h3>Returned Information</h3>
     *
     * <p>For each workspace, the summary includes:
     *
     * <ul>
     *   <li><strong>Admin Access</strong>: Whether the user has administrative rights on the workspace
     *   <li><strong>Allowed Layers</strong>: Layers the user can access
     *   <li><strong>Forbidden Layers</strong>: Layers explicitly denied to the user
     * </ul>
     *
     * <p><strong>Note</strong>: This is a high-level summary that does not include detailed
     * restrictions such as spatial filters, CQL filters, or attribute-level access. Use {@link
     * #getAccessInfo(AccessRequest)} for detailed authorization evaluation of specific resources.
     *
     * @param request the access summary request containing user and roles
     * @return access summary with per-workspace visibility information, never {@code null}
     */
    AccessSummary getUserAccessSummary(AccessSummaryRequest request);

    /**
     * Retrieves the list of {@link Rule}s that match a given access request.
     *
     * <p>This method returns the raw, unprocessed rules that apply to the request context. Unlike
     * {@link #getAccessInfo(AccessRequest)}, it does <strong>not</strong> evaluate or resolve the
     * rules into an authorization decision.
     *
     * <p>This method is useful for:
     *
     * <ul>
     *   <li>Debugging authorization decisions
     *   <li>Auditing which rules apply to specific requests
     *   <li>Understanding rule coverage without performing authorization
     *   <li>Building custom authorization analysis tools
     * </ul>
     *
     * @param request the access request to match rules against
     * @return list of matching rules, may be empty but never {@code null}
     * @throws IllegalArgumentException if {@link AccessRequest#validate() request} is invalid
     * @see #getAccessInfo(AccessRequest) for evaluated authorization decisions
     */
    List<Rule> getMatchingRules(AccessRequest request);
}
