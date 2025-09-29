/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.accessmanager;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;

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
                new AccessRequestUserResolver(config).withUser(user).resolve();
        AdminAccessRequest.Builder builder = AdminAccessRequest.builder();

        builder.user(userResolver.getUsername());
        builder.roles(userResolver.getUnfilteredRoles());

        builder.workspace(workspace);
        String sourceAddress = resolveSourceAddress();
        builder.sourceAddress(sourceAddress);

        AdminAccessRequest accessRequest = builder.build();
        LOGGER.log(Level.FINEST, "AdminAccessRequest: {0}", accessRequest);

        return accessRequest;
    }

    private String resolveSourceAddress() {
        String sourceAddress = ipAddress;
        if (sourceAddress == null) {
            Optional<Request> req = Optional.ofNullable(Dispatcher.REQUEST.get());
            sourceAddress = AccessRequestBuilder.retrieveCallerIpAddress(req);
        }
        if (sourceAddress == null) {
            LOGGER.warning("No source IP address found");
        }
        return sourceAddress;
    }
}
