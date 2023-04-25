/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Refactored, original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import org.apache.commons.lang.StringUtils;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class AccessRequestUserResolver {

    private static final Logger LOGGER = Logging.getLogger(AccessRequestUserResolver.class);

    private AccessManagerConfig config;

    String authorizationName;
    Collection<? extends GrantedAuthority> authorities;

    private String username;
    private Set<String> userRoles;

    AccessRequestUserResolver(AccessManagerConfig configuration) {
        this.config = configuration;
    }

    AccessRequestUserResolver user(String username) {
        this.authorizationName = username;
        return this;
    }

    AccessRequestUserResolver user(Authentication user) {
        if (user == null) {
            authorities = null;
        } else {
            authorities = user.getAuthorities();
            authorizationName = user.getName();
        }
        return this;
    }

    AccessRequestUserResolver authorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        return this;
    }

    public AccessRequestUserResolver resolve() {
        if (authorizationName == null) {
            LOGGER.log(Level.FINEST, "No user given");
            this.userRoles = Set.of();
            this.username = null;
        } else {
            this.userRoles = resolveRoleNames();
            this.username = authorizationName;
            if (StringUtils.isEmpty(username)) {
                LOGGER.log(Level.WARNING, "Username is null");
                this.username = null;
            }
        }
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getUserRoles() {
        return userRoles;
    }

    private Set<String> resolveRoleNames() {
        // just some loggings here
        if (config.isUseRolesToFilter() && config.getAcceptedRoles().isEmpty()) {
            LOGGER.log(
                    Level.WARNING,
                    "Role filtering requested, but no roles provided. Will only use user authorizations");
        }

        if (config.isUseRolesToFilter() && !config.getAcceptedRoles().isEmpty()) {

            Set<String> roles = getFilteredRoles();
            //            if (roles.isEmpty()) {
            //                roles.add("UNKNOWN");
            //            }
            LOGGER.log(Level.FINE, "Setting role for filter: {0}", new Object[] {roles});
            return roles;
        }
        return Set.of();
    }

    public Set<String> getFilteredRoles() {
        boolean getAllRoles = config.getAcceptedRoles().contains("*");
        Set<String> excluded =
                config.getAcceptedRoles().stream()
                        .filter(r -> r.startsWith("-"))
                        .map(r -> r.substring(1))
                        .collect(Collectors.toSet());

        return getFilteredRoles(getAllRoles, excluded);
    }

    private Set<String> getFilteredRoles(boolean getAllRoles, Set<String> excluded) {
        if (authorities == null || authorities.isEmpty()) {
            return Set.of();
        }
        Set<String> roles = new HashSet<>();
        for (String authRole : getUnfilteredRoles()) {
            if (addRole(authRole, excluded, getAllRoles)) roles.add(authRole);
        }
        return roles;
    }

    public Set<String> getUnfilteredRoles() {
        if (authorities == null || authorities.isEmpty()) {
            return Set.of();
        }
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    private boolean addRole(String role, Set<String> excluded, boolean getAllRoles) {
        boolean addRole = getAllRoles || config.getAcceptedRoles().contains(role);
        return addRole && !(excluded.contains(role));
    }
}
