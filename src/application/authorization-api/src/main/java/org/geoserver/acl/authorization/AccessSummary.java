/* (c) 2024  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.authorization;

import static java.lang.String.format;
import static org.geoserver.acl.authorization.WorkspaceAccessSummary.ANY;
import static org.geoserver.acl.authorization.WorkspaceAccessSummary.NO_WORKSPACE;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.geoserver.acl.domain.adminrules.AdminGrantType;

/**
 * Represents the converged set of visible layer names of a specific workspace for for a {@link
 * AccessSummaryRequest}
 *
 * @since 2.3
 * @see WorkspaceAccessSummary
 */
@EqualsAndHashCode
public class AccessSummary {

    /** Immutable mapping of workspace name to summary */
    private Map<String, WorkspaceAccessSummary> workspaceSummaries;

    private static final WorkspaceAccessSummary HIDE_ALL = WorkspaceAccessSummary.builder()
            .workspace("*")
            .adminAccess(null)
            .addForbidden("*")
            .build();

    private AccessSummary(Map<String, WorkspaceAccessSummary> workspaceSummaries) {
        this.workspaceSummaries = workspaceSummaries;
    }

    public static AccessSummary of(WorkspaceAccessSummary... workspaces) {
        return AccessSummary.of(Arrays.asList(workspaces));
    }

    public static AccessSummary of(List<WorkspaceAccessSummary> workspaces) {
        Map<String, WorkspaceAccessSummary> summaries = new LinkedHashMap<>();
        workspaces.forEach(ws -> summaries.put(ws.getWorkspace(), ws));
        return new AccessSummary(summaries);
    }

    public List<WorkspaceAccessSummary> getWorkspaces() {
        return List.copyOf(workspaceSummaries.values());
    }

    public WorkspaceAccessSummary workspace(String workspace) {
        return workspaceSummaries.get(workspace);
    }

    public boolean hasAdminReadAccess(@NonNull String workspaceName) {
        boolean user = workspaceSummaries.getOrDefault(ANY, HIDE_ALL).isUser();
        return user
                ? user
                : workspaceSummaries.getOrDefault(workspaceName, HIDE_ALL).isUser();
    }

    public boolean hasAdminWriteAccess(@NonNull String workspaceName) {
        boolean admin = workspaceSummaries.getOrDefault(ANY, HIDE_ALL).isAdmin();
        return admin
                ? admin
                : workspaceSummaries.getOrDefault(workspaceName, HIDE_ALL).isAdmin();
    }

    public boolean canSeeLayer(String workspaceName, @NonNull String layerName) {
        if (null == workspaceName) workspaceName = WorkspaceAccessSummary.NO_WORKSPACE;
        WorkspaceAccessSummary summary = summary(workspaceName);
        return summary.canSeeLayer(layerName);
    }

    private WorkspaceAccessSummary summary(@NonNull String workspaceName) {
        var summary = workspaceSummaries.get(workspaceName);
        if (null == summary) summary = workspaceSummaries.getOrDefault(ANY, HIDE_ALL);
        return summary;
    }

    public Set<String> visibleWorkspaces() {
        return workspaceSummaries.values().stream()
                .filter(WorkspaceAccessSummary::visible)
                .map(WorkspaceAccessSummary::getWorkspace)
                .filter(name -> !NO_WORKSPACE.equals(name))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> adminableWorkspaces() {
        return workspaceSummaries.keySet().stream()
                .filter(this::hasAdminWriteAccess)
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        var values = new TreeMap<>(workspaceSummaries).values();
        return format("%s(%s)", getClass().getSimpleName(), values);
    }

    public boolean hasAdminRightsToAnyWorkspace() {
        return workspaceSummaries.values().stream()
                .map(WorkspaceAccessSummary::getAdminAccess)
                .anyMatch(AdminGrantType.ADMIN::equals);
    }
}
