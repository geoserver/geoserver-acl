/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.model.filter;

import lombok.NonNull;

import org.geoserver.acl.model.filter.predicate.FilterType;
import org.geoserver.acl.model.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.model.filter.predicate.InSetPredicate;
import org.geoserver.acl.model.filter.predicate.SpecialFilterType;
import org.geoserver.acl.model.filter.predicate.TextFilter;
import org.geoserver.acl.model.rules.Rule;
import org.geoserver.acl.model.rules.RuleIdentifier;

import java.util.Set;
import java.util.function.Predicate;

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
public class RuleFilter extends Filter implements Cloneable, Predicate<Rule> {

    private final TextFilter user;
    private final InSetPredicate<String> role;
    private final TextFilter instance;
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
        instance = new TextFilter(ft);
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
        instance = new TextFilter(ft, includeDefault);
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
            String instanceName,
            String sourceAddress,
            String service,
            String request,
            String subfield,
            String workspace,
            String layer) {
        this(SpecialFilterType.DEFAULT);

        this.user.setHeuristically(userName);
        this.role.setHeuristically(groupName);
        this.instance.setHeuristically(instanceName);
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
            instance = source.instance.clone();
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

    public RuleFilter setInstance(String name) {
        instance.setText(name);
        return this;
    }

    public RuleFilter setInstance(SpecialFilterType type) {
        instance.setType(type);
        return this;
    }

    public RuleFilter setSourceAddress(String dotted) {
        sourceAddress.setText(dotted);
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

    public TextFilter getInstance() {
        return instance;
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleFilter other = (RuleFilter) obj;
        if (this.user != other.user && (this.user == null || !this.user.equals(other.user))) {
            return false;
        }
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
            return false;
        }
        if (this.instance != other.instance
                && (this.instance == null || !this.instance.equals(other.instance))) {
            return false;
        }
        if (this.service != other.service
                && (this.service == null || !this.service.equals(other.service))) {
            return false;
        }
        if (this.request != other.request
                && (this.request == null || !this.request.equals(other.request))) {
            return false;
        }
        if (this.subfield != other.subfield
                && (this.subfield == null || !this.subfield.equals(other.subfield))) {
            return false;
        }
        if (this.workspace != other.workspace
                && (this.workspace == null || !this.workspace.equals(other.workspace))) {
            return false;
        }
        if (this.layer != other.layer && (this.layer == null || !this.layer.equals(other.layer))) {
            return false;
        }
        // NOTE: ipaddress not in equals() bc it is not used for caching
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 37 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 37 * hash + (this.instance != null ? this.instance.hashCode() : 0);
        hash = 37 * hash + (this.sourceAddress != null ? this.sourceAddress.hashCode() : 0);
        hash = 37 * hash + (this.service != null ? this.service.hashCode() : 0);
        hash = 37 * hash + (this.request != null ? this.request.hashCode() : 0);
        hash = 37 * hash + (this.subfield != null ? this.subfield.hashCode() : 0);
        hash = 37 * hash + (this.workspace != null ? this.workspace.hashCode() : 0);
        hash = 37 * hash + (this.layer != null ? this.layer.hashCode() : 0);
        // NOTE: ipaddress not in hashcode bc it is not used for caching
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("user:").append(user);
        sb.append(" role:").append(role);
        sb.append(" inst:").append(instance);
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
        return getInstance().test(idf.getInstanceName())
                && getLayer().test(idf.getLayer())
                && getRequest().test(idf.getRequest())
                && getRole().test(idf.getRolename())
                && getService().test(idf.getService())
                && getSourceAddress().test(idf.getAddressRange())
                && getSubfield().test(idf.getSubfield())
                && getUser().test(idf.getUsername())
                && getWorkspace().test(idf.getWorkspace());
    }
}
