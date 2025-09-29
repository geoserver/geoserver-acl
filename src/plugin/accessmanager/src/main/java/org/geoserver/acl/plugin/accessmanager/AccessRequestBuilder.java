/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import static java.util.logging.Level.WARNING;

import java.net.InetAddress;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
                new AccessRequestUserResolver(config).withUser(user).resolve();

        Set<String> roles = userResolver.getUserRoles();

        AccessRequest.Builder builder = AccessRequest.builder();
        builder.user(userResolver.getUsername());
        builder.roles(roles);

        // get info from the current request
        Optional<Request> owsReq = resolveOwsRequest();
        String requestedService = resoleService(owsReq);
        String requestedServiceRequest = resolveServiceRequest(owsReq);

        builder.service(requestedService);
        builder.request(requestedServiceRequest);
        builder.workspace(workspace);
        builder.layer(layer);
        String sourceAddress = resolveSourceAddress();
        builder.sourceAddress(sourceAddress);

        AccessRequest accessRequest = builder.build();
        LOGGER.log(Level.FINEST, "AccessRequest: {0}", accessRequest);

        return accessRequest;
    }

    private String resolveServiceRequest(Optional<Request> owsReq) {
        String requestedServiceRequest = this.request;
        if (requestedServiceRequest == null && owsReq.isPresent()) {
            requestedServiceRequest = owsReq.orElseThrow().getRequest();
        }
        if ("*".equals(requestedServiceRequest)) {
            requestedServiceRequest = null;
        }
        return requestedServiceRequest;
    }

    private String resoleService(Optional<Request> owsReq) {
        String requestedService = this.service;
        if (requestedService == null && owsReq.isPresent()) {
            requestedService = owsReq.orElseThrow().getService();
        }
        if ("*".equals(requestedService)) {
            requestedService = null;
        }
        return requestedService;
    }

    private Optional<Request> resolveOwsRequest() {
        return Optional.ofNullable(this.owsRequest).or(() -> Optional.ofNullable(Dispatcher.REQUEST.get()));
    }

    private String resolveSourceAddress() {
        String sourceAddress = ipAddress;
        if (sourceAddress == null) {
            sourceAddress = retrieveCallerIpAddress(resolveOwsRequest());
        }
        if (sourceAddress == null) {
            LOGGER.warning("No source IP address found");
        }
        return sourceAddress;
    }

    static String retrieveCallerIpAddress(Optional<Request> owsRequest) {

        String reqSource = "Dispatcher.REQUEST";
        final HttpServletRequest request;

        // is this an OWS request
        if (owsRequest.isPresent()) {
            request = owsRequest.orElseThrow().getHttpRequest();
        } else {
            reqSource = "Spring Request";
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            request = requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        try {
            String sourceAddress = getSourceAddress(request);
            if (sourceAddress == null) {
                LOGGER.log(WARNING, "Could not retrieve source address from {0}", reqSource);
            }
            return sourceAddress;
        } catch (RuntimeException ex) {
            LOGGER.log(
                    WARNING, "Error retrieving source address with {0}: {1}", new Object[] {reqSource, ex.getMessage()
                    });
            return null;
        }
    }

    static String getSourceAddress(HttpServletRequest http) {
        if (http == null) {
            LOGGER.warning("No HTTP request available.");
            return null;
        }

        String sourceAddress = null;
        try {
            final String forwardedFor = http.getHeader("X-Forwarded-For");
            final String remoteAddr = http.getRemoteAddr();
            if (forwardedFor != null) {
                String[] ips = forwardedFor.split(", ");
                sourceAddress = InetAddress.getByName(ips[0]).getHostAddress();
            } else if (remoteAddr != null) {
                // Returns an IP address, removes surrounding brackets present in case of IPV6
                // addresses
                sourceAddress = remoteAddr.replaceAll("[\\[\\]]", "");
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Failed to get remote address", e);
        }
        return sourceAddress;
    }
}
