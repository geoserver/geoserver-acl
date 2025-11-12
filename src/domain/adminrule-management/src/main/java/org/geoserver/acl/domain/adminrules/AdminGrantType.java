/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.adminrules;

/**
 * Workspace administration privilege levels.
 *
 * <p>Admin privileges allow creating, editing, and deleting layers, modifying workspace settings,
 * managing styles, and configuring layer properties (CRS, bounding boxes, etc.).
 *
 * <p>Example:
 * <pre>{@code
 * // Grant admin privileges
 * AdminRule adminAccess = AdminRule.builder()
 *     .access(AdminGrantType.ADMIN)
 *     .identifier(AdminRuleIdentifier.builder()
 *         .username("workspace_manager")
 *         .workspace("topp")
 *         .build())
 *     .build();
 *
 * // Regular user access (no admin)
 * AdminRule userAccess = AdminRule.builder()
 *     .access(AdminGrantType.USER)
 *     .identifier(AdminRuleIdentifier.builder()
 *         .username("data_viewer")
 *         .workspace("public")
 *         .build())
 *     .build();
 * }</pre>
 *
 * @since 1.0
 * @see AdminRule
 * @see org.geoserver.acl.domain.rules.GrantType
 */
public enum AdminGrantType {
    /**
     * Full workspace administration - can create, modify, and delete workspace resources
     * (layers, styles, settings).
     */
    ADMIN,

    /**
     * Regular user - no administrative privileges. Data access controlled by
     * {@link org.geoserver.acl.domain.rules.Rule}.
     */
    USER
}
