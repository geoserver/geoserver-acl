/* (c) 2024 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager;

import static org.geoserver.acl.authorization.WorkspaceAccessSummary.ANY;
import static org.geoserver.acl.authorization.WorkspaceAccessSummary.NO_WORKSPACE;
import static org.geoserver.catalog.Predicates.acceptAll;
import static org.geoserver.catalog.Predicates.acceptNone;
import static org.geoserver.catalog.Predicates.and;
import static org.geoserver.catalog.Predicates.equal;
import static org.geoserver.catalog.Predicates.in;
import static org.geoserver.catalog.Predicates.isInstanceOf;
import static org.geoserver.catalog.Predicates.isNull;
import static org.geoserver.catalog.Predicates.not;
import static org.geoserver.catalog.Predicates.notEqual;
import static org.geoserver.catalog.Predicates.or;
import static org.geotools.api.filter.Filter.EXCLUDE;
import static org.geotools.api.filter.Filter.INCLUDE;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.WorkspaceAccessSummary;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geotools.api.filter.Filter;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author Gabriel Roldan - Camptocamp
 */
public class CatalogSecurityFilterBuilder {

    private final AccessSummary viewables;

    public CatalogSecurityFilterBuilder(AccessSummary viewables) {
        this.viewables = Objects.requireNonNull(viewables);
    }

    public static Filter buildSecurityFilter(AccessSummary viewables, Class<? extends CatalogInfo> infoType) {
        return new CatalogSecurityFilterBuilder(viewables).build(infoType);
    }

    @SuppressWarnings("unchecked")
    public Filter build(Class<? extends CatalogInfo> clazz) {
        Objects.requireNonNull(clazz);
        if (viewables.getWorkspaces().isEmpty()) {
            return EXCLUDE;
        }
        if (WorkspaceInfo.class.isAssignableFrom(clazz)) {
            return workspaceNameFilter("name", false);
        }
        if (NamespaceInfo.class.isAssignableFrom(clazz)) {
            return workspaceNameFilter("prefix", false);
        }
        if (StoreInfo.class.isAssignableFrom(clazz)) {
            return workspaceNameFilter("workspace.name", false);
        }
        if (ResourceInfo.class.isAssignableFrom(clazz)) {
            return layerFilter("store.workspace.name", "name", ResourceInfo.class);
        }
        if (PublishedInfo.class.isAssignableFrom(clazz)) {
            return publishedInfoFilter((Class<? extends PublishedInfo>) clazz);
        }
        if (StyleInfo.class.isAssignableFrom(clazz)) {
            return styleFilter();
        }
        throw new UnsupportedOperationException("Unknown CatalogInfo type: " + clazz.getCanonicalName());
    }

    private Filter styleFilter() {
        return workspaceNameFilter("workspace.name", true);
    }

    private Filter publishedInfoFilter(Class<? extends PublishedInfo> clazz) {
        if (LayerInfo.class.isAssignableFrom(clazz)) {
            return layerFilter("resource.store.workspace.name", "name", LayerInfo.class);
        }
        if (LayerGroupInfo.class.isAssignableFrom(clazz)) {
            return layerFilter("workspace.name", "name", LayerGroupInfo.class);
        }
        Filter layerInfoFilter = build(LayerInfo.class);
        Filter layerGroupInfoFilter = build(LayerGroupInfo.class);
        if (INCLUDE.equals(layerInfoFilter) && INCLUDE.equals(layerGroupInfoFilter)) {
            return INCLUDE;
        }
        Filter layerFilter = instanceOfAnd(LayerInfo.class, layerInfoFilter);
        Filter groupFilter = instanceOfAnd(LayerGroupInfo.class, layerGroupInfoFilter);

        if (EXCLUDE.equals(layerInfoFilter)) {
            return groupFilter;
        } else if (EXCLUDE.equals(layerGroupInfoFilter)) {
            return layerFilter;
        }

        return or(layerFilter, groupFilter);
    }

    private Filter instanceOfAnd(Class<?> typeOf, Filter andThen) {
        if (EXCLUDE.equals(andThen)) return andThen;
        return and(isInstanceOf(typeOf), andThen);
    }

    private Filter layerFilter(String workspaceProperty, String nameProperty, Class<? extends CatalogInfo> type) {
        List<WorkspaceAccessSummary> summaries = viewables.getWorkspaces();

        Filter filter = acceptNone();
        Set<String> hideAllWorkspaceNames = new TreeSet<>();
        for (WorkspaceAccessSummary wsSummary : summaries) {
            String workspace = wsSummary.getWorkspace();
            if (wsSummary.hideAll()) {
                if (!ANY.equals(workspace)) {
                    hideAllWorkspaceNames.add(workspace);
                }
            } else {
                boolean isNullWorkspace = NO_WORKSPACE.equals(workspace);
                boolean supportsNullWorkspace = LayerGroupInfo.class.equals(type);
                // ignore if workspace is null and type is LayerInfo or ResourceInfo
                if (!isNullWorkspace || supportsNullWorkspace) {
                    Filter wsLayersFitler =
                            filterLayersOnWorkspace(wsSummary, workspaceProperty, supportsNullWorkspace, nameProperty);

                    if (EXCLUDE.equals(filter)) {
                        filter = wsLayersFitler;
                    } else {
                        filter = or(filter, wsLayersFitler);
                    }
                }
            }
        }
        filter = prependHideAllWorkspaces(filter, workspaceProperty, hideAllWorkspaceNames);
        return SimplifyingFilterVisitor.simplify(filter);
    }

    private Filter prependHideAllWorkspaces(
            Filter filter, String workspaceProperty, Set<String> hideAllWorkspaceNames) {
        if (hideAllWorkspaceNames.isEmpty()) {
            return filter;
        }
        Filter hiddenWorkspaces = denyWorkspacesFilter(workspaceProperty, hideAllWorkspaceNames);
        if (INCLUDE.equals(filter)) {
            return hiddenWorkspaces;
        }
        return and(hiddenWorkspaces, filter);
    }

    private Filter denyWorkspacesFilter(String workspaceProperty, Set<String> hideAllWorkspaceNames) {
        Assert.isTrue(!hideAllWorkspaceNames.isEmpty(), "hidden workspace names can't be empty");
        return notEqualOrIn(workspaceProperty, hideAllWorkspaceNames, EXCLUDE);
    }

    @NonNull
    private Filter filterLayersOnWorkspace(
            WorkspaceAccessSummary vl, String workspaceProperty, boolean includeNullWorkspace, String nameProperty) {

        final String workspace = vl.getWorkspace();
        final Set<String> allowed = vl.getAllowed();
        final Set<String> forbidden = vl.getForbidden();

        Filter workspaceFilter = workspaceNameFilter(workspaceProperty, includeNullWorkspace, Set.of(workspace));
        Filter filter;
        if (allowed.isEmpty() && forbidden.isEmpty()) {
            filter = workspaceFilter;
        } else {
            Filter layerFilter = mergeLayers(nameProperty, allowed, forbidden);
            filter = and(workspaceFilter, layerFilter);
        }
        return filter;
    }

    private Filter mergeLayers(String nameProperty, Set<String> allowed, Set<String> forbidden) {
        Filter allowFilter = equalOrIn(nameProperty, allowed, INCLUDE);
        Filter hideFilter = notEqualOrIn(nameProperty, forbidden, EXCLUDE);
        if (INCLUDE.equals(hideFilter)) {
            return allowFilter;
        }
        if (INCLUDE.equals(allowFilter)) {
            return hideFilter;
        }
        // neither is include, conflates to the allow filter
        return allowFilter;
    }

    /**
     * @return {@link Filter#INCLUDE} if {@code names} is empty, {@code not(equalOrIn(nameProperty,
     *     names)} otherwise
     */
    private Filter notEqualOrIn(String nameProperty, Set<String> names, Filter defaultIfAny) {
        if (names.isEmpty()) return INCLUDE;
        if (names.contains(ANY)) return defaultIfAny;

        if (names.size() == 1) {
            return notEqual(nameProperty, names.iterator().next());
        }
        return not(equalOrIn(nameProperty, names, /* has no effect */ defaultIfAny));
    }

    /**
     * @return {@link Filter#INCLUDE} if {@code names} is empty, {@code defaultIfAny} if {@code
     *     names} contains {@code *}, a "{@code name = names.get(0)}" filter if {@code names} has a
     *     single element, a "{@code name IN(names)}" filter if {@code names} has multiple elements.
     */
    private Filter equalOrIn(String nameProperty, Set<String> names, Filter defaultIfAny) {
        if (names.isEmpty()) return INCLUDE;
        if (names.contains(ANY)) return defaultIfAny;
        if (names.size() == 1) {
            return equal(nameProperty, names.iterator().next());
        }
        return in(nameProperty, List.copyOf(names));
    }

    private Set<String> getVisibleWorkspaces() {
        return viewables.visibleWorkspaces();
    }

    private Filter workspaceNameFilter(String workspaceProperty, boolean includeNullWorkspace) {
        Set<String> visibleWorkspaces = getVisibleWorkspaces();
        if (includeNullWorkspace && viewables.workspace(NO_WORKSPACE) != null) {
            visibleWorkspaces = new TreeSet<>(visibleWorkspaces);
            visibleWorkspaces.add(NO_WORKSPACE);
        }
        return workspaceNameFilter(workspaceProperty, includeNullWorkspace, visibleWorkspaces);
    }

    private Filter workspaceNameFilter(
            String workspaceProperty, boolean includeNullWorkspace, Set<String> visibleWorkspaces) {
        if (visibleWorkspaces.contains(ANY)) {
            return acceptAll();
        }
        Filter filter = acceptNone();
        if (visibleWorkspaces.contains(NO_WORKSPACE)) {
            if (includeNullWorkspace) {
                filter = isNull(workspaceProperty);
            }
            visibleWorkspaces = new TreeSet<>(visibleWorkspaces);
            visibleWorkspaces.remove(NO_WORKSPACE);
        }
        if (!visibleWorkspaces.isEmpty()) {
            List<String> workspaces = List.copyOf(visibleWorkspaces);
            Filter namesFilter;
            if (workspaces.size() == 1) {
                namesFilter = equal(workspaceProperty, workspaces.get(0));
            } else {
                namesFilter = in(workspaceProperty, workspaces);
            }
            if (EXCLUDE.equals(filter)) {
                filter = namesFilter;
            } else {
                filter = or(filter, namesFilter);
            }
        }
        return filter;
    }
}
