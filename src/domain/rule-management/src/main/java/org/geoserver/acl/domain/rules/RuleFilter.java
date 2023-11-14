/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

import org.geoserver.acl.domain.filter.Filter;
import org.geoserver.acl.domain.filter.predicate.FilterType;
import org.geoserver.acl.domain.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.domain.filter.predicate.InSetPredicate;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.filter.predicate.TextFilter;

import java.util.Set;

/**
 * A Filter for selecting {@link Rule}s.
 *
 * <p>For every given field, you may choose to select
 *
 * <UL>
 *   <LI>a given value
 *   <LI>any values (no filtering)
 *   <LI>only default rules (null value in a field)
 * </UL>
 *
 * For instances (i.e., classes represented by DB entities) you may specify either the ID or the
 * name.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@EqualsAndHashCode
public class RuleFilter implements Filter<Rule>, Cloneable {

    private final TextFilter user;
    private final InSetPredicate<String> role;
    private final IPAddressRangeFilter sourceAddress;
    private final TextFilter service;
    private final TextFilter request;
    private final TextFilter subfield;
    private final TextFilter workspace;
    private final TextFilter layer;

    public RuleFilter() {
        this(SpecialFilterType.DEFAULT);
    }

    /**
     * Creates a RuleFilter by setting all fields filtering either to ANY or DEFAULT. <br>
     * If no other field is set, you will get
     *
     * <UL>
     *   <LI>with <B>ANY</B>, all Rules will be returned
     *   <LI>with <B>DEFAULT</B>, only the default Rule will be returned
     * </UL>
     */
    public RuleFilter(SpecialFilterType type) {
        FilterType ft = type.getRelatedType();

        user = new TextFilter(ft);
        role = new InSetPredicate<>(ft);
        sourceAddress = new IPAddressRangeFilter(ft);
        service = new TextFilter(ft, true);
        request = new TextFilter(ft, true);
        subfield = new TextFilter(ft, true);
        workspace = new TextFilter(ft);
        layer = new TextFilter(ft);
    }

    public RuleFilter(SpecialFilterType type, boolean includeDefault) {
        FilterType ft = type.getRelatedType();

        user = new TextFilter(ft);
        user.setIncludeDefault(includeDefault);
        role = new InSetPredicate<>(ft);
        role.setIncludeDefault(includeDefault);
        sourceAddress = new IPAddressRangeFilter(ft);
        sourceAddress.setIncludeDefault(includeDefault);
        service = new TextFilter(ft, true);
        service.setIncludeDefault(includeDefault);
        request = new TextFilter(ft, true);
        request.setIncludeDefault(includeDefault);
        subfield = new TextFilter(ft, true);
        subfield.setIncludeDefault(includeDefault);
        workspace = new TextFilter(ft);
        workspace.setIncludeDefault(includeDefault);
        layer = new TextFilter(ft);
        layer.setIncludeDefault(includeDefault);
    }

    public static RuleFilter any() {
        return new RuleFilter(SpecialFilterType.ANY);
    }

    /**
     * Creates a RuleFilter by heuristically converting special string values into Fitler behaviour:
     *
     * <UL>
     *   <LI>a null value will match only with null
     *   <LI>a '*' value will match everything (no filter condition on that given field)
     *   <LI>any other string will match literally
     * </UL>
     */
    public RuleFilter(
            String userName,
            String groupName,
            String sourceAddress,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer) {
        this(SpecialFilterType.DEFAULT);

        this.user.setHeuristically(userName);
        this.role.setHeuristically(groupName);
        this.sourceAddress.setHeuristically(sourceAddress);

        this.service.setHeuristically(service);
        this.request.setHeuristically(request);
        this.subfield.setHeuristically(subfield);
        this.workspace.setHeuristically(workspace);
        this.layer.setHeuristically(layer);
    }

    public RuleFilter(RuleFilter source) {
        try {
            user = source.user.clone();
            role = source.role.clone();
            sourceAddress = source.sourceAddress.clone();
            service = source.service.clone();
            request = source.request.clone();
            subfield = source.subfield.clone();
            workspace = source.workspace.clone();
            layer = source.layer.clone();
        } catch (CloneNotSupportedException ex) {
            // Should not happen
            throw new UnknownError("Clone error - should not happen");
        }
    }

    public RuleFilter setUser(String name) {
        if (name == null) throw new NullPointerException();
        user.setText(name);
        return this;
    }

    public RuleFilter setUser(SpecialFilterType type) {
        user.setType(type);
        return this;
    }

    public RuleFilter setRole(Set<String> roles) {
        //        if (roles == null) throw new NullPointerException();
        role.setValues(roles);
        return this;
    }

    public RuleFilter setRole(String name) {
        //        if (name == null) throw new NullPointerException();
        role.setText(name);
        return this;
    }

    public RuleFilter setRole(SpecialFilterType type) {
        role.setType(type);
        return this;
    }

    public RuleFilter setSourceAddress(String dotted) {
        sourceAddress.setAddress(dotted);
        return this;
    }

    public RuleFilter setSourceAddress(SpecialFilterType type) {
        sourceAddress.setType(type);
        return this;
    }

    public RuleFilter setService(String name) {
        service.setText(name);
        return this;
    }

    public RuleFilter setService(SpecialFilterType type) {
        service.setType(type);
        return this;
    }

    public RuleFilter setRequest(String name) {
        request.setText(name);
        return this;
    }

    public RuleFilter setRequest(SpecialFilterType type) {
        request.setType(type);
        return this;
    }

    public RuleFilter setSubfield(String name) {
        subfield.setText(name);
        return this;
    }

    public RuleFilter setSubfield(SpecialFilterType type) {
        subfield.setType(type);
        return this;
    }

    public RuleFilter setWorkspace(String name) {
        workspace.setText(name);
        return this;
    }

    public RuleFilter setWorkspace(SpecialFilterType type) {
        workspace.setType(type);
        return this;
    }

    public RuleFilter setLayer(String name) {
        layer.setText(name);
        return this;
    }

    public RuleFilter setLayer(SpecialFilterType type) {
        layer.setType(type);
        return this;
    }

    public IPAddressRangeFilter getSourceAddress() {
        return sourceAddress;
    }

    public TextFilter getLayer() {
        return layer;
    }

    public InSetPredicate<String> getRole() {
        return role;
    }

    public TextFilter getRequest() {
        return request;
    }

    public TextFilter getSubfield() {
        return subfield;
    }

    public TextFilter getService() {
        return service;
    }

    public TextFilter getUser() {
        return user;
    }

    public TextFilter getWorkspace() {
        return workspace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("user:").append(user);
        sb.append(" role:").append(role);
        sb.append(" ip:").append(sourceAddress);
        sb.append(" serv:").append(service);
        sb.append(" req:").append(request);
        if (subfield != null) sb.append(" sub:").append(subfield);
        sb.append(" ws:").append(workspace);
        sb.append(" layer:").append(layer);
        sb.append(']');

        return sb.toString();
    }

    @Override
    public RuleFilter clone() {
        return new RuleFilter(this);
    }

    @Override
    public boolean test(@NonNull Rule rule) {
        RuleIdentifier idf = rule.getIdentifier();
        return getLayer().test(idf.getLayer())
                && getRequest().test(idf.getRequest())
                && getRole().test(idf.getRolename())
                && getService().test(idf.getService())
                && getSourceAddress().test(idf.getAddressRange())
                && getSubfield().test(idf.getSubfield())
                && getUser().test(idf.getUsername())
                && getWorkspace().test(idf.getWorkspace());
    }
}
