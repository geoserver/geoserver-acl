/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import org.geoserver.acl.model.adminrules.AdminGrantType;
import org.geoserver.acl.model.rules.CatalogMode;
import org.geoserver.acl.model.rules.GrantType;
import org.geoserver.acl.model.rules.LayerAttribute;
import org.geoserver.acl.model.rules.LayerDetails;
import org.geoserver.acl.model.rules.SpatialFilterType;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EnumsApiMapper {

    LayerDetails.LayerType map(org.geoserver.acl.api.model.LayerDetails.TypeEnum value);

    org.geoserver.acl.api.model.LayerDetails.TypeEnum map(LayerDetails.LayerType value);

    CatalogMode map(org.geoserver.acl.api.model.CatalogMode value);

    org.geoserver.acl.api.model.CatalogMode map(CatalogMode value);

    SpatialFilterType map(org.geoserver.acl.api.model.SpatialFilterType value);

    org.geoserver.acl.api.model.SpatialFilterType map(SpatialFilterType value);

    org.geoserver.acl.api.model.InsertPosition map(
            org.geoserver.acl.model.rules.InsertPosition pos);

    org.geoserver.acl.model.rules.InsertPosition map(
            org.geoserver.acl.api.model.InsertPosition pos);

    org.geoserver.acl.api.model.LayerAttribute.AccessEnum accessType(
            LayerAttribute.AccessType value);

    LayerAttribute.AccessType accessType(
            org.geoserver.acl.api.model.LayerAttribute.AccessEnum value);

    GrantType grantType(org.geoserver.acl.api.model.GrantType value);

    org.geoserver.acl.api.model.GrantType grantType(GrantType value);

    AdminGrantType adminGrantType(org.geoserver.acl.api.model.AdminGrantType value);

    org.geoserver.acl.api.model.AdminGrantType adminGrantType(AdminGrantType value);
}
