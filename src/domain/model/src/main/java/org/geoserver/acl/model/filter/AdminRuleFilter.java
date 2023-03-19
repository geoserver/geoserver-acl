/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.filter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.geoserver.acl.model.adminrules.AdminGrantType;
import org.geoserver.acl.model.adminrules.AdminRule;
import org.geoserver.acl.model.adminrules.AdminRuleIdentifier;
import org.geoserver.acl.model.filter.predicate.FilterType;
import org.geoserver.acl.model.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.model.filter.predicate.InSetPredicate;
import org.geoserver.acl.model.filter.predicate.SpecialFilterType;
import org.geoserver.acl.model.filter.predicate.TextFilter;

import java.util.Set;
import java.util.function.Predicate;

// REVISIT: shouldn't extend RuleFilter, it has only a subset of its properties
public class AdminRuleFilter extends Filter implements Cloneable, Predicate<AdminRule> {

    private @Getter @Setter AdminGrantType grantType;

    private final @Getter TextFilter user;
    private final @Getter InSetPredicate<String> role;
    private final @Getter TextFilter instance;
    private final @Getter IPAddressRangeFilter sourceAddress;
    private final @Getter TextFilter workspace;

    public AdminRuleFilter() {
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
    public AdminRuleFilter(SpecialFilterType type) {
        FilterType ft = type.getRelatedType();

        user = new TextFilter(ft);
        role = new InSetPredicate<String>(ft);
        instance = new TextFilter(ft);
        sourceAddress = new IPAddressRangeFilter(ft);
        workspace = new TextFilter(ft);
    }

    public AdminRuleFilter setIncludeDefault(boolean includeDefault) {
        user.setIncludeDefault(includeDefault);
        role.setIncludeDefault(includeDefault);
        instance.setIncludeDefault(includeDefault);
        sourceAddress.setIncludeDefault(includeDefault);
        workspace.setIncludeDefault(includeDefault);
        return this;
    }

    public AdminRuleFilter(AdminRuleFilter source) {
        grantType = source.getGrantType();
        try {
            user = source.user.clone();
            role = source.role.clone();
            instance = source.instance.clone();
            sourceAddress = source.sourceAddress.clone();
            workspace = source.workspace.clone();
        } catch (CloneNotSupportedException ex) {
            // Should not happen
            throw new UnknownError("Clone error - should not happen");
        }
    }

    private AdminRuleFilter(RuleFilter source) {
        try {
            user = source.getUser().clone();
            role = source.getRole().clone();
            instance = source.getInstance().clone();
            sourceAddress = source.getSourceAddress().clone();
            workspace = source.getWorkspace().clone();
        } catch (CloneNotSupportedException ex) {
            // Should not happen
            throw new UnknownError("Clone error - should not happen");
        }
    }

    @Override
    public AdminRuleFilter clone() {
        return new AdminRuleFilter(this);
    }

    public static AdminRuleFilter of(RuleFilter ruleFilter) {
        return new AdminRuleFilter(ruleFilter);
    }

    public static AdminRuleFilter any() {
        return AdminRuleFilter.of(RuleFilter.any());
    }

    public AdminRuleFilter setUser(@NonNull String name) {
        user.setText(name);
        return this;
    }

    public AdminRuleFilter setUser(SpecialFilterType type) {
        user.setType(type);
        return this;
    }

    public AdminRuleFilter setRole(@NonNull Set<String> roles) {
        role.setValues(roles);
        return this;
    }

    public AdminRuleFilter setRole(String name) {
        if (name == null) throw new NullPointerException();
        role.setText(name);
        return this;
    }

    public AdminRuleFilter setRole(SpecialFilterType type) {
        role.setType(type);
        return this;
    }

    public AdminRuleFilter setInstance(@NonNull String name) {
        instance.setText(name);
        return this;
    }

    public AdminRuleFilter setInstance(SpecialFilterType type) {
        instance.setType(type);
        return this;
    }

    public AdminRuleFilter setSourceAddress(String dotted) {
        sourceAddress.setText(dotted);
        return this;
    }

    public AdminRuleFilter setSourceAddress(SpecialFilterType type) {
        sourceAddress.setType(type);
        return this;
    }

    public AdminRuleFilter setWorkspace(String name) {
        workspace.setText(name);
        return this;
    }

    public AdminRuleFilter setWorkspace(SpecialFilterType type) {
        workspace.setType(type);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AdminRuleFilter other = (AdminRuleFilter) obj;
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
        if (this.workspace != other.workspace
                && (this.workspace == null || !this.workspace.equals(other.workspace))) {
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
        hash = 37 * hash + (this.workspace != null ? this.workspace.hashCode() : 0);
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
        sb.append(" ws:").append(workspace);
        sb.append(']');

        return sb.toString();
    }

    @Override
    public boolean test(@NonNull AdminRule rule) {
        AdminRuleIdentifier idf = rule.getIdentifier();

        return getInstance().test(idf.getInstanceName())
                && getRole().test(idf.getRolename())
                && getSourceAddress().test(idf.getAddressRange())
                && getUser().test(idf.getUsername())
                && getWorkspace().test(idf.getWorkspace())
                && (grantType == null || grantType.equals(rule.getAccess()));
    }
}
