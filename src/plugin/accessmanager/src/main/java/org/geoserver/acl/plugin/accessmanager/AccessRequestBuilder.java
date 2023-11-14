/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.ows.Request;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Builder class for a {@link RuleFilter}. */
class AccessRequestBuilder {

    private String service;
    private String request;
    private Request owsRequest;

    private String ipAddress;
    private String workspace;
    private String layer;
    private Authentication user;
    private AccessManagerConfig config;

    private static final Logger LOGGER = Logging.getLogger(AccessRequestBuilder.class);

    AccessRequestBuilder(AccessManagerConfig configuration) {
        this.config = configuration;
    }

    AccessRequestBuilder request(Request request) {
        this.owsRequest = request;
        return this;
    }

    public AccessRequestBuilder service(String service) {
        this.service = service;
        return this;
    }

    public AccessRequestBuilder request(String request) {
        this.request = request;
        return this;
    }

    AccessRequestBuilder ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    AccessRequestBuilder workspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    AccessRequestBuilder layer(String layer) {
        this.layer = layer;
        return this;
    }

    AccessRequestBuilder user(Authentication authentication) {
        this.user = authentication;
        return this;
    }

    /**
     * Builds an {@link AccessRequest} using the values set through the various builder's method.
     */
    public AccessRequest build() {
        AccessRequestUserResolver userResolver =
                new AccessRequestUserResolver(config).user(user).resolve();

        Set<String> roles = userResolver.getUserRoles();

        AccessRequest.Builder builder = AccessRequest.builder();
        builder.user(userResolver.getUsername());
        builder.roles(roles);

        // get info from the current request
        String service = this.service;
        String request = this.request;
        if (owsRequest != null) {
            service = owsRequest.getService();
            request = owsRequest.getRequest();
        }
        if ("*".equals(service)) {
            builder.service(null);
        } else {
            builder.service(service);
        }
        if ("*".equals(request)) {
            builder.request(null);
        } else {
            builder.request(request);
        }

        builder.workspace(workspace);
        builder.layer(layer);
        String sourceAddress = ipAddress;
        if (sourceAddress == null) {
            LOGGER.log(Level.WARNING, "No source IP address found");
        }
        builder.sourceAddress(sourceAddress);

        AccessRequest accessRequest = builder.build();
        LOGGER.log(Level.FINEST, "AccessRequest: {0}", accessRequest);

        return accessRequest;
    }
}
