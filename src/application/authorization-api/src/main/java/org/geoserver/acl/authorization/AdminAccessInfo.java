/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.authorization;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

/**
 * Resolved workspace administration decision produced by
 * {@link AuthorizationService#getAdminAuthorization(AdminAccessRequest)}.
 *
 * <p>Indicates whether the requesting user holds administrative privileges on the targeted
 * workspace and, when applicable, the identifier of the admin rule that produced the decision.
 *
 * @param workspace the name of the workspace the decision applies to. {@code null} when the
 *     evaluated request did not target a specific workspace.
 * @param admin {@code true} if the user holds administrative privileges on {@code workspace},
 *     {@code false} otherwise.
 * @param matchingAdminRule the identifier of the
 *     {@link org.geoserver.acl.domain.adminrules.AdminRule} that produced this decision.
 *     {@code null} when no admin rule matched.
 */
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public record AdminAccessInfo(@Nullable String workspace, boolean admin, @Nullable String matchingAdminRule) {}
