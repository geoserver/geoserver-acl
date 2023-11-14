/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import org.geoserver.acl.authorization.AccessRequest;
import org.geoserver.acl.authorization.AdminAccessRequest;
import org.geoserver.acl.authorization.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration object for {@link ACLResourceAccessManager}.
 *
 * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it" - Originally as part of
 *     GeoFence's GeoServer extension
 */
public class AccessManagerConfig implements Serializable, Cloneable {

    public static final String URL_INTERNAL = "internal:/";

    private static final long serialVersionUID = 3L;

    private boolean allowRemoteAndInlineLayers;
    private boolean grantWriteToWorkspacesToAuthenticatedUsers;
    private boolean useRolesToFilter = true;
    private List<String> roles;

    private String serviceUrl = URL_INTERNAL;

    public AccessManagerConfig() {
        initDefaults();
    }

    public void initDefaults() {
        allowRemoteAndInlineLayers = true;
        grantWriteToWorkspacesToAuthenticatedUsers = false;
        useRolesToFilter = true;
        roles = new ArrayList<>(List.of("*"));
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /** Flag to allow usage of remote and inline layers in SLDs. */
    public void setAllowRemoteAndInlineLayers(boolean allowRemoteAndInlineLayers) {
        this.allowRemoteAndInlineLayers = allowRemoteAndInlineLayers;
    }

    /** Flag to allow usage of remote and inline layers in SLDs. */
    public boolean isAllowRemoteAndInlineLayers() {
        return allowRemoteAndInlineLayers;
    }

    /**
     * Whether to allow write access to resources to authenticated users ({@code true}, if {@code
     * false}, only admins (users with {@literal ROLE_ADMINISTRATOR}) have write access.
     */
    public boolean isGrantWriteToWorkspacesToAuthenticatedUsers() {
        return grantWriteToWorkspacesToAuthenticatedUsers;
    }

    /**
     * Whether to allow write access to resources to authenticated users, if false only admins
     * (users with {@literal ROLE_ADMINISTRATOR}) have write access.
     */
    public void setGrantWriteToWorkspacesToAuthenticatedUsers(
            boolean grantWriteToWorkspacesToAuthenticatedUsers) {
        this.grantWriteToWorkspacesToAuthenticatedUsers =
                grantWriteToWorkspacesToAuthenticatedUsers;
    }

    /**
     * Use authenticated users roles to match rules, in addition to username (defaults to {@code
     * true})
     *
     * @return {@code true} if {@link Authentication} role names are included in {@link
     *     AccessRequest} and {@link AdminAccessRequest} for the {@link AuthorizationService}
     *     (default behavior), {@code false} if only authenticated user names shall be used.
     */
    public boolean isUseRolesToFilter() {
        return useRolesToFilter;
    }

    /**
     * Use authenticated users roles to match rules, in addition to username (defaults to {@code
     * true}).
     *
     * @param {@code true} if {@link Authentication} role names are to be included in {@link
     *     AccessRequest} and {@link AdminAccessRequest} for the {@link AuthorizationService}
     *     (default behavior), {@code false} if only authenticated user names shall be used.
     */
    public void setUseRolesToFilter(boolean useRolesToFilter) {
        this.useRolesToFilter = useRolesToFilter;
    }

    /**
     * List of mutually exclusive roles used for rule matching when useRolesToFilter is true.
     *
     * @return the acceptedRoles
     */
    public List<String> getAcceptedRoles() {
        return roles;
    }

    /**
     * List of mutually exclusive roles used for rule matching when {@link #setUseRolesToFilter
     * useRolesToFilter} is {@code true}
     *
     * <p>By default all roles are accepted and this property equals to the {@code *} wildcard
     * character.
     *
     * @param acceptedRoles the acceptedRoles to set
     */
    public void setAcceptedRoles(List<String> acceptedRoles) {
        if (acceptedRoles == null || acceptedRoles.isEmpty()) {
            this.roles = new ArrayList<>(List.of("*"));
        } else {
            this.roles =
                    acceptedRoles.stream()
                            .filter(StringUtils::hasText)
                            .collect(Collectors.toList());
        }
    }

    /** Creates a copy of the configuration object. */
    @Override
    public AccessManagerConfig clone() {
        try {
            AccessManagerConfig clone = (AccessManagerConfig) super.clone();
            clone.setAcceptedRoles(this.roles);
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new UnknownError("Unexpected exception: " + ex.getMessage());
        }
    }
}
