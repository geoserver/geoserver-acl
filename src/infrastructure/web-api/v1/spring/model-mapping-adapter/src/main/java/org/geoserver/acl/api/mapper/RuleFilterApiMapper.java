/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.mapper;

import java.util.Set;
import org.geoserver.acl.domain.adminrules.AdminRuleFilter;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.rules.RuleFilter;
import org.geoserver.acl.webapi.v1.model.AddressRangeFilter;
import org.geoserver.acl.webapi.v1.model.AdminGrantType;
import org.geoserver.acl.webapi.v1.model.SetFilter;
import org.geoserver.acl.webapi.v1.model.TextFilter;
import org.springframework.stereotype.Component;

@Component
public class RuleFilterApiMapper {

    public org.geoserver.acl.webapi.v1.model.AdminRuleFilter map(AdminRuleFilter filter) {
        if (filter == null) return null;
        org.geoserver.acl.webapi.v1.model.AdminRuleFilter api = new org.geoserver.acl.webapi.v1.model.AdminRuleFilter();

        api.setGrantType(map(filter.getGrantType()));
        api.setRoles(setFilterToApi(filter.getRole()));
        api.setSourceAddress(addressRangeToApi(filter.getSourceAddress()));
        api.setUser(textFilterToApi(filter.getUser()));
        api.setWorkspace(textFilterToApi(filter.getWorkspace()));
        return api;
    }

    public AdminRuleFilter map(org.geoserver.acl.webapi.v1.model.AdminRuleFilter filter) {
        if (filter == null) return null;
        AdminRuleFilter model = new AdminRuleFilter();
        model.setGrantType(map(filter.getGrantType()));
        setFilterToModel(model.getRole(), filter.getRoles());
        addressRangeToModel(model.getSourceAddress(), filter.getSourceAddress());
        textFilterToModel(model.getUser(), filter.getUser());
        textFilterToModel(model.getWorkspace(), filter.getWorkspace());
        return model;
    }

    private org.geoserver.acl.domain.adminrules.AdminGrantType map(AdminGrantType grantType) {

        if (grantType != null) {
            switch (grantType) {
                case ADMIN:
                    return org.geoserver.acl.domain.adminrules.AdminGrantType.ADMIN;
                case USER:
                    return org.geoserver.acl.domain.adminrules.AdminGrantType.USER;
                default:
                    throw new IllegalArgumentException("Unknown AdminGrantType: " + grantType);
            }
        }
        return null;
    }

    private AdminGrantType map(org.geoserver.acl.domain.adminrules.AdminGrantType grantType) {
        if (grantType == null) return null;
        switch (grantType) {
            case ADMIN:
                return AdminGrantType.ADMIN;
            case USER:
                return AdminGrantType.USER;
            default:
                throw new IllegalArgumentException("Unknown AdminGrantType: " + grantType);
        }
    }

    public org.geoserver.acl.webapi.v1.model.RuleFilter toApi(RuleFilter filter) {
        if (filter == null) return null;
        org.geoserver.acl.webapi.v1.model.RuleFilter api = new org.geoserver.acl.webapi.v1.model.RuleFilter();

        api.setLayer(textFilterToApi(filter.getLayer()));
        api.setRoles(setFilterToApi(filter.getRole()));
        api.setRequest(textFilterToApi(filter.getRequest()));
        api.setService(textFilterToApi(filter.getService()));
        api.setSourceAddress(addressRangeToApi(filter.getSourceAddress()));
        api.setSubfield(textFilterToApi(filter.getSubfield()));
        api.setUser(textFilterToApi(filter.getUser()));
        api.setWorkspace(textFilterToApi(filter.getWorkspace()));
        return api;
    }

    public RuleFilter toModel(org.geoserver.acl.webapi.v1.model.RuleFilter filter) {
        if (filter == null) return null;
        RuleFilter model = new RuleFilter();
        textFilterToModel(model.getLayer(), filter.getLayer());
        textFilterToModel(model.getRequest(), filter.getRequest());
        setFilterToModel(model.getRole(), filter.getRoles());
        textFilterToModel(model.getService(), filter.getService());
        addressRangeToModel(model.getSourceAddress(), filter.getSourceAddress());
        textFilterToModel(model.getSubfield(), filter.getSubfield());
        textFilterToModel(model.getUser(), filter.getUser());
        textFilterToModel(model.getWorkspace(), filter.getWorkspace());
        return model;
    }

    private org.geoserver.acl.webapi.v1.model.SetFilter setFilterToApi(
            org.geoserver.acl.domain.filter.predicate.InSetPredicate<String> filter) {

        switch (filter.getType()) {
            case DEFAULT:
                return null;
            case ANY:
                return new SetFilter().values(Set.of("*"));
            case NAMEVALUE:
                SetFilter value = new SetFilter().values(filter.getValues());
                if (!filter.isIncludeDefault()) {
                    value.includeDefault(filter.isIncludeDefault());
                }
                return value;
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Unexpected value type for TextFilter: " + filter.getType());
        }
    }

    private void setFilterToModel(
            org.geoserver.acl.domain.filter.predicate.InSetPredicate<String> target,
            org.geoserver.acl.webapi.v1.model.SetFilter source) {

        if (source == null) return;

        if (null != source.getIncludeDefault()) {
            target.setIncludeDefault(source.getIncludeDefault().booleanValue());
        }
        if (null != source.getValues()) {
            Set<String> values = source.getValues();
            if (values.contains("*")) {
                target.setType(SpecialFilterType.ANY);
            } else if (!values.isEmpty()) {
                target.setValues(values);
            }
        }
    }

    private org.geoserver.acl.webapi.v1.model.TextFilter textFilterToApi(
            org.geoserver.acl.domain.filter.predicate.TextFilter filter) {

        switch (filter.getType()) {
            case DEFAULT:
                return null;
            case ANY:
                return new TextFilter().value("*");
            case NAMEVALUE:
                TextFilter value = new TextFilter().value(filter.getText());
                if (!filter.isIncludeDefault()) {
                    value.includeDefault(filter.isIncludeDefault());
                }
                return value;
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Unexpected value type for TextFilter: " + filter.getType());
        }
    }

    private void textFilterToModel(
            org.geoserver.acl.domain.filter.predicate.TextFilter target,
            org.geoserver.acl.webapi.v1.model.TextFilter source) {

        if (source != null) {
            Boolean includeDefault = source.getIncludeDefault();
            String value = source.getValue();

            if (value != null) {
                target.setHeuristically(value);
                if (includeDefault != null) target.setIncludeDefault(includeDefault);
            } else {
                if (includeDefault != null && includeDefault.booleanValue()) target.setType(SpecialFilterType.DEFAULT);
                else target.setType(SpecialFilterType.ANY);
            }
        }
    }

    private org.geoserver.acl.webapi.v1.model.AddressRangeFilter addressRangeToApi(
            org.geoserver.acl.domain.filter.predicate.IPAddressRangeFilter filter) {

        switch (filter.getType()) {
            case DEFAULT:
                return null;
            case ANY:
                return new AddressRangeFilter().value("*");
            case NAMEVALUE:
                AddressRangeFilter value = new AddressRangeFilter().value(filter.getAddress());
                if (!filter.isIncludeDefault()) {
                    value.includeDefault(filter.isIncludeDefault());
                }
                return value;
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Unexpected value type for TextFilter: " + filter.getType());
        }
    }

    private void addressRangeToModel(
            org.geoserver.acl.domain.filter.predicate.IPAddressRangeFilter target,
            org.geoserver.acl.webapi.v1.model.AddressRangeFilter source) {

        if (source != null) {
            Boolean includeDefault = source.getIncludeDefault();
            String value = source.getValue();

            if (value != null) {
                target.setHeuristically(value);
                if (includeDefault != null) target.setIncludeDefault(includeDefault);
            } else {
                if (includeDefault != null && includeDefault.booleanValue()) target.setType(SpecialFilterType.DEFAULT);
                else target.setType(SpecialFilterType.ANY);
            }
        }
    }
}
