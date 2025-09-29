/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.authorization;

import java.util.List;
import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.domain.rules.Rule;
import org.geoserver.acl.domain.rules.RuleAdminService;

/**
 * The {@code AuthorizationService} implements the business logic to grant or deny access to layers
 * by processing the {@link Rule}s in the {@link RuleAdminService}, and to determine the admin
 * access level to workspaces based on the {@link AdminRule}s in the {@link AdminRuleAdminService}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence's
 *     AdminRuleService)
 * @author Gabriel Roldan adapt from {@code RuleFilter} to immutable parameters and return types
 */
public interface AuthorizationService {

    /**
     * Return info on resource accessibility.
     *
     * @throws IllegalArgumentException if {@link AccessRequest#validate() request} is invalid
     */
    AccessInfo getAccessInfo(AccessRequest request);

    /**
     * info about admin authorization on a given workspace.
     *
     * <p>Returned AdminAccessInfo will always be ALLOW, with the computed adminRights.
     *
     * @throws IllegalArgumentException if {@link AdminAccessRequest#validate() request} is invalid
     */
    AdminAccessInfo getAdminAuthorization(AdminAccessRequest request);

    /**
     * Returns a summary of workspace names and the layers a user denoted by the {@code request} can
     * somehow see, and in the case of workspaces, whether it's an administrator of.
     */
    AccessSummary getUserAccessSummary(AccessSummaryRequest request);

    /**
     * Return the unprocessed {@link Rule} list matching a given request, sorted by priority.
     *
     * <p>Use {@link #getAccessInfo(AccessRequest)} and {@link
     * #getAdminAuthorization(AdminAccessRequest)} if you need the resulting coalesced access info.
     *
     * @throws IllegalArgumentException if {@link AccessRequest#validate() request} is invalid
     */
    List<Rule> getMatchingRules(AccessRequest request);
}
