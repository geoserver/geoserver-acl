/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.components;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfig;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfigProvider;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.web.GeoServerApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Slf4j
@SuppressWarnings("serial")
public abstract class AbstractRuleEditModel<R extends Serializable> implements Serializable {

    /**
     * Maximum number of items to show on {@link #getWorkspaceChoices(}, {@link #getUserChoices},
     * {@link #getRoleChoices}
     */
    protected static final int MAX_SUGGESTIONS = 100;

    private @Getter CompoundPropertyModel<R> model;

    private @Getter @Setter boolean includeInstanceName;

    public AbstractRuleEditModel(@NonNull R rule) {
        model = new CompoundPropertyModel<>(rule);
        includeInstanceName = hasInstanceOrDefault(rule);
    }

    private boolean hasInstanceOrDefault(@NonNull R rule) {
        String ruleInstanceName = getInstanceName(rule);
        boolean hasInstance = StringUtils.hasText(ruleInstanceName);
        return hasInstance || isDefaultIncludeInstanceName();
    }

    protected boolean isDefaultIncludeInstanceName() {
        AccessManagerConfig config = config();
        boolean defaultIncludeInstance = config.isCreateWithInstanceName();
        return defaultIncludeInstance;
    }

    public abstract void save();

    /**
     * Returns the {@link AccessManagerConfig#getInstanceName()}, required for the "use instance
     * name" checkbox label, and to set the rule's instance name
     */
    public String getInstanceName() {
        return config().getInstanceName();
    }

    protected abstract String getInstanceName(R rule);

    /**
     * @see #getUserChoices(String)
     */
    protected abstract String getRoleName(R rule);

    public Iterator<String> getUserChoices(String input) {
        final Pattern test = caseInsensitiveStartsWith(input);
        final String roleName = getRoleName(getModel().getObject());
        return getUserNamesByRole(roleName)
                .filter(user -> inputMatches(test, user))
                .sorted()
                .distinct()
                .limit(MAX_SUGGESTIONS)
                .iterator();
    }

    public Iterator<String> getRoleChoices(@Nullable String input) {
        final Pattern test = caseInsensitiveStartsWith(input);
        return getRoleNames()
                .filter(role -> inputMatches(test, role))
                .limit(MAX_SUGGESTIONS)
                .iterator();
    }

    public Iterator<String> getWorkspaceChoices(@Nullable String input) {
        final Pattern test = caseInsensitiveStartsWith(input);
        return getWorkspaceNames()
                .filter(workspace -> inputMatches(test, workspace))
                .limit(MAX_SUGGESTIONS)
                .iterator();
    }

    protected Pattern caseInsensitiveStartsWith(@Nullable String input) {
        return Pattern.compile(Pattern.quote(nonNull(input)) + ".*", Pattern.CASE_INSENSITIVE);
    }

    protected Pattern caseInsensitiveContains(@Nullable String input) {
        return Pattern.compile(
                ".*" + Pattern.quote(nonNull(input)) + ".*", Pattern.CASE_INSENSITIVE);
    }

    protected Pattern startsWith(@NonNull String input) {
        return Pattern.compile(Pattern.quote(nonNull(input)) + ".*");
    }

    protected String nonNull(String input) {
        return StringUtils.hasText(input) ? input.trim() : "";
    }

    protected boolean inputMatches(@NonNull Pattern input, @NonNull String choice) {
        return input.matcher(choice).matches();
        // return
        // choice.toLowerCase(Locale.ROOT).toLowerCase(Locale.ROOT).startsWith(input);
    }

    public Stream<String> getRoleNames() {
        try {
            GeoServerSecurityManager securityManager = securityManager();
            return securityManager.getRolesForAccessControl().stream()
                    .map(GeoServerRole::getAuthority);
        } catch (IOException e) {
            log.warn("Error getting available role names", e);
            return Stream.empty();
        }
    }

    protected Stream<String> getWorkspaceNames() {
        return rawCatalog().getWorkspaces().stream()
                .parallel()
                .map(WorkspaceInfo::getName)
                .sorted();
    }

    protected Set<GeoServerRole> getAvailableRoles() {
        try {
            return securityManager().getRolesForAccessControl();
        } catch (IOException e) {
            log.warn("Error obtaining available roles", e);
            return Set.of();
        }
    }

    protected Stream<String> getUserNamesByRole(String roleName) {

        GeoServerSecurityManager securityManager = securityManager();
        try {
            if (StringUtils.hasText(roleName)) {
                SortedSet<String> ret = new TreeSet<>();
                for (String serviceName : securityManager.listRoleServices()) {
                    GeoServerRoleService roleService = securityManager.loadRoleService(serviceName);
                    GeoServerRole role = roleService.getRoleByName(roleName);
                    if (role != null) {
                        SortedSet<String> usernames = roleService.getUserNamesForRole(role);
                        ret.addAll(usernames);
                    }
                }
                return ret.stream();
            }

            return securityManager.loadUserGroupServices().stream()
                    .map(
                            t -> {
                                try {
                                    return t.getUsers();
                                } catch (IOException e) {
                                    log.warn(
                                            "Error getting users from group service " + t.getName(),
                                            e);
                                    return Set.<GeoServerUser>of();
                                }
                            })
                    .flatMap(Set::stream)
                    .map(GeoServerUser::getUsername)
                    .sorted()
                    .distinct();
        } catch (IOException e) {
            log.warn("Error getting users for role " + roleName, e);
            return Stream.empty();
        }
    }

    protected Map<String, Set<String>> loadUsersByRole() {

        Map<String, Set<String>> usersByRole = new TreeMap<>();
        usersByRole.put(GeoServerRole.AUTHENTICATED_ROLE.getAuthority(), Set.of());
        usersByRole.put(GeoServerRole.ANONYMOUS_ROLE.getAuthority(), Set.of());
        usersByRole.put(GeoServerRole.ADMIN_ROLE.getAuthority(), new TreeSet<>());

        GeoServerSecurityManager securityManager = securityManager();
        SortedSet<String> serviceNames;
        try {
            serviceNames = securityManager.listUserGroupServices();
        } catch (IOException e) {
            log.warn(e.getLocalizedMessage(), e);
            return usersByRole;
        }

        for (String serviceName : serviceNames) {
            try {
                GeoServerUserGroupService service =
                        securityManager.loadUserGroupService(serviceName);
                SortedSet<GeoServerUser> users = service.getUsers();
                for (GeoServerUser user : users) {
                    user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .forEach(
                                    role -> {
                                        usersByRole
                                                .computeIfAbsent(role, rname -> new TreeSet<>())
                                                .add(user.getUsername());
                                    });
                }
            } catch (IOException e) {
                log.warn(e.getLocalizedMessage(), e);
                return usersByRole;
            }
        }
        return usersByRole;
    }

    public Catalog rawCatalog() {
        return (Catalog) GeoServerExtensions.bean("rawCatalog");
    }

    protected GeoServerSecurityManager securityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    protected AccessManagerConfig config() {
        return configProvider().get();
    }

    protected static AccessManagerConfigProvider configProvider() {
        // can't use GeoServerApplication.getBeanOfType cause it delegates to
        // GeoServerExtensions
        // which throws an exception if there are multiple beans of the same type,
        // disregarding the
        // @Primary config
        ApplicationContext context = GeoServerApplication.get().getApplicationContext();
        return context.getBean(AccessManagerConfigProvider.class);
    }
}
