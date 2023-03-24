/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.accessmanager;

import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;

import java.util.logging.Level;
import java.util.logging.Logger;

class AdminAccessRequestBuilder {

    private String ipAddress;
    private String workspace;
    private Authentication user;
    private AccessManagerConfig config;

    private static final Logger LOGGER = Logging.getLogger(AdminAccessRequestBuilder.class);

    AdminAccessRequestBuilder(AccessManagerConfig configuration) {
        this.config = configuration;
    }

    AdminAccessRequestBuilder ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    AdminAccessRequestBuilder workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    AdminAccessRequestBuilder user(Authentication authentication) {
        this.user = authentication;
        return this;
    }

    /**
     * Builds an {@link AdminAccessRequest} using the values set through the various builder's
     * method.
     */
    public AdminAccessRequest build() {
        AccessRequestUserResolver userResolver =
                new AccessRequestUserResolver(config).user(user).resolve();
        AdminAccessRequest.Builder builder = AdminAccessRequest.builder();

        builder.user(userResolver.getUsername());
        builder.roles(userResolver.getUnfilteredRoles());

        builder.instance(config.getInstanceName());
        builder.workspace(workspace);
        String sourceAddress = ipAddress;
        if (sourceAddress != null) {
            builder.sourceAddress(sourceAddress);
        } else {
            LOGGER.log(Level.WARNING, "No source IP address found");
            builder.sourceAddress(null);
        }

        AdminAccessRequest accessRequest = builder.build();
        LOGGER.log(Level.FINE, "AdminAccessRequest: {0}", accessRequest);

        return accessRequest;
    }
}
