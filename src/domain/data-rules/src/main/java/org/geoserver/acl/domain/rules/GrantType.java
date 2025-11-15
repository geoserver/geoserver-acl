/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.rules;

/**
 * Access grant types for data access rules.
 *
 * <p>Determines the level of access a rule provides. Rules are evaluated in priority order
 * (lower numbers first), and the first matching rule determines the access level.
 *
 * @since 1.0
 * @see Rule
 * @see LayerDetails
 * @see RuleLimits
 */
public enum GrantType {
    /**
     * Grant access, optionally with detailed restrictions.
     * <p>Can include {@link LayerDetails} specifying allowed styles, CQL filters (read/write),
     * spatial constraints, attribute permissions, and catalog mode.
     */
    ALLOW,

    /**
     * Deny access.
     * <p>No details or limits - denial is absolute. Use to block access that would otherwise
     * be granted by lower-priority rules.
     */
    DENY,

    /**
     * Grant limited access with restrictions.
     * <p>Applies {@link RuleLimits} constraining spatial extent and catalog mode.
     * Simpler than ALLOW with LayerDetails - focuses on spatial restrictions without
     * attribute-level control or CQL filters.
     */
    LIMIT
}
