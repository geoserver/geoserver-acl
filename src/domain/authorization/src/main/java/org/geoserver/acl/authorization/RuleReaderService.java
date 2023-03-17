/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.authorization;

import org.geoserver.acl.model.rules.Rule;

import java.util.List;

/**
 * Operations on
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
public interface RuleReaderService {

    /** Return info on resource accessibility. */
    AccessInfo getAccessInfo(AccessRequest request);

    /**
     * info about admin authorization on a given workspace.
     *
     * <p>Returned AccessInfo will always be ALLOW, with the computed adminRights.
     */
    AccessInfo getAdminAuthorization(AccessRequest request);

    /**
     * Return the unprocessed {@link Rule} list matching a given filter, sorted by priority.
     *
     * <p>Use {@link getAccessInfo(RuleFilter) getAccessInfo(RuleFilter)} if you need the resulting
     * coalesced access info.
     */
    List<Rule> getMatchingRules(AccessRequest request);

    // ==========================================================================

}
