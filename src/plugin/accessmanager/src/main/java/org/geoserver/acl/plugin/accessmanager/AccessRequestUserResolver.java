/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Refactored, original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;
import org.geotools.util.logging.Logging;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

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

    AccessRequestUserResolver withUserName(String username) {
        this.authorizationName = username;
        return this;
    }

    AccessRequestUserResolver withUser(Authentication user) {
        if (user == null) {
            authorities = null;
        } else {
            authorities = user.getAuthorities();
            authorizationName = user.getName();
        }
        return this;
    }

    AccessRequestUserResolver withAuthorities(Collection<? extends GrantedAuthority> authorities) {
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
        Set<String> roles = Set.of();
        if (config.isUseRolesToFilter()) {
            final List<String> acceptedRoles = config.getAcceptedRoles();
            if (acceptedRoles.isEmpty()) {
                LOGGER.warning("Role filtering requested, but no roles provided. Will only use user authorizations");
            } else {
                roles = getFilteredRoles();
                LOGGER.log(Level.FINEST, "Setting role for filter: {0}", new Object[] {roles});
            }
        }
        return roles;
    }

    public Set<String> getFilteredRoles() {
        final boolean getAllRoles = config.getAcceptedRoles().contains("*");
        Set<String> excluded = config.getAcceptedRoles().stream()
                .filter(r -> r.startsWith("-"))
                .map(r -> r.substring(1))
                .collect(Collectors.toSet());

        return getFilteredRoles(getAllRoles, excluded);
    }

    private Set<String> getFilteredRoles(boolean getAllRoles, Set<String> excluded) {
        return getUnfilteredRoleNames()
                .filter(authRole -> addRole(authRole, excluded, getAllRoles))
                .collect(Collectors.toSet());
    }

    public Set<String> getUnfilteredRoles() {
        return getUnfilteredRoleNames().collect(Collectors.toSet());
    }

    private Stream<String> getUnfilteredRoleNames() {
        if (authorities == null || authorities.isEmpty()) {
            return Stream.empty();
        }
        return authorities.stream().map(GrantedAuthority::getAuthority);
    }

    private boolean addRole(String role, Set<String> excluded, boolean getAllRoles) {
        boolean addRole = getAllRoles || config.getAcceptedRoles().contains(role);
        return addRole && !(excluded.contains(role));
    }
}
