/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.webapi.v1.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.geoserver.acl.authorization.AccessSummary;
import org.geoserver.acl.authorization.WorkspaceAccessSummary;
import org.geoserver.acl.webapi.v1.model.AccessInfo;
import org.geoserver.acl.webapi.v1.model.AccessRequest;
import org.geoserver.acl.webapi.v1.model.AccessSummaryRequest;
import org.geoserver.acl.webapi.v1.model.AdminAccessInfo;
import org.geoserver.acl.webapi.v1.model.AdminAccessRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {GeometryApiMapper.class, RuleFilterApiMapper.class})
public interface AuthorizationModelApiMapper {

    AccessRequest toApi(org.geoserver.acl.authorization.AccessRequest request);

    org.geoserver.acl.authorization.AccessRequest toModel(AccessRequest request);

    @Mapping(target = "area", source = "intersectArea") // for API backwards compat with the "area" property name
    AccessInfo toApi(org.geoserver.acl.authorization.AccessInfo grant);

    @Mapping(source = "area", target = "intersectArea") // for API backwards compat with the "area" property name
    org.geoserver.acl.authorization.AccessInfo toModel(AccessInfo grant);

    AdminAccessRequest toApi(org.geoserver.acl.authorization.AdminAccessRequest request);

    org.geoserver.acl.authorization.AdminAccessRequest toModel(AdminAccessRequest request);

    AdminAccessInfo toApi(org.geoserver.acl.authorization.AdminAccessInfo grant);

    org.geoserver.acl.authorization.AdminAccessInfo toModel(AdminAccessInfo grant);

    AccessSummaryRequest toApi(org.geoserver.acl.authorization.AccessSummaryRequest request);

    org.geoserver.acl.authorization.AccessSummaryRequest toModel(AccessSummaryRequest request);

    org.geoserver.acl.webapi.v1.model.AccessSummary toApi(AccessSummary apiResponse);

    default AccessSummary toModel(org.geoserver.acl.webapi.v1.model.AccessSummary apiResponse) {
        if (apiResponse == null) {
            return null;
        }
        List<WorkspaceAccessSummary> workspaces =
                Optional.ofNullable(apiResponse.getWorkspaces()).orElse(List.of()).stream()
                        .map(this::workspaceAccessSummary)
                        .collect(Collectors.toList());
        return AccessSummary.of(workspaces);
    }

    WorkspaceAccessSummary workspaceAccessSummary(
            org.geoserver.acl.webapi.v1.model.WorkspaceAccessSummary workspaceAccessSummary);
}
