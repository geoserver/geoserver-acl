/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.authorization;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.util.Set;

@Value
@With
@Builder(toBuilder = true, builderClassName = "Builder")
public class AdminAccessRequest {

    private String user;
    @Default @NonNull private Set<String> roles = Set.of();
    private String instance;
    private String workspace;
    private String sourceAddress;
}
