/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.model.authorization;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AdminAccessInfo {

    private String workspace;

    private boolean admin;

    private String matchingAdminRule;
}
