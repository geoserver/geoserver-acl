/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.adminrules;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.geoserver.acl.domain.filter.Filter;
import org.geoserver.acl.domain.filter.predicate.FilterType;
import org.geoserver.acl.domain.filter.predicate.IPAddressRangeFilter;
import org.geoserver.acl.domain.filter.predicate.InSetPredicate;
import org.geoserver.acl.domain.filter.predicate.SpecialFilterType;
import org.geoserver.acl.domain.filter.predicate.TextFilter;

import java.util.Set;

@EqualsAndHashCode
public class AdminRuleFilter implements Filter<AdminRule>, Cloneable {

    private @Getter @Setter AdminGrantType grantType;

    private final @Getter TextFilter user;
    private final @Getter InSetPredicate<String> role;
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
        sourceAddress = new IPAddressRangeFilter(ft);
        workspace = new TextFilter(ft);
    }

    public AdminRuleFilter setIncludeDefault(boolean includeDefault) {
        user.setIncludeDefault(includeDefault);
        role.setIncludeDefault(includeDefault);
        sourceAddress.setIncludeDefault(includeDefault);
        workspace.setIncludeDefault(includeDefault);
        return this;
    }

    public AdminRuleFilter(AdminRuleFilter source) {
        grantType = source.getGrantType();
        try {
            user = source.user.clone();
            role = source.role.clone();
            sourceAddress = source.sourceAddress.clone();
            workspace = source.workspace.clone();
        } catch (CloneNotSupportedException ex) {
            // Should not happen
            throw new UnknownError("Clone error - should not happen");
        }
    }

    @Override
    public AdminRuleFilter clone() {
        return new AdminRuleFilter(this);
    }

    public static AdminRuleFilter any() {
        return new AdminRuleFilter(SpecialFilterType.ANY);
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

    public AdminRuleFilter setSourceAddress(String dotted) {
        sourceAddress.setAddress(dotted);
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
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append('[');
        sb.append("user:").append(user);
        sb.append(" role:").append(role);
        sb.append(" ip:").append(sourceAddress);
        sb.append(" ws:").append(workspace);
        sb.append(']');

        return sb.toString();
    }

    @Override
    public boolean test(@NonNull AdminRule rule) {
        AdminRuleIdentifier idf = rule.getIdentifier();

        return getRole().test(idf.getRolename())
                && getSourceAddress().test(idf.getAddressRange())
                && getUser().test(idf.getUsername())
                && getWorkspace().test(idf.getWorkspace())
                && (grantType == null || grantType.equals(rule.getAccess()));
    }
}
