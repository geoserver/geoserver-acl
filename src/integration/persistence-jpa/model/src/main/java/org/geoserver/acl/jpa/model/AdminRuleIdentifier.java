/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.jpa.model;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * Uniquely identifies an {@link AdminRule}, all properties are mandatory in order for the {@link
 * AdminRule}'s unique constraint to be enforced by the database, which otherwise will consider
 * {@literal NULL != NULL}. The {@code *} literal is used as default value.
 *
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@Embeddable
public class AdminRuleIdentifier implements Cloneable {
    public static final String ANY = "*";

    @NonNull
    @Column(nullable = false)
    private String username = ANY;

    @NonNull
    @Column(nullable = false)
    private String rolename = ANY;

    @NonNull
    @Column(nullable = false)
    private String workspace = ANY;

    @NonNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "low", column = @Column(name = "ip_low")),
        @AttributeOverride(name = "high", column = @Column(name = "ip_high")),
        @AttributeOverride(name = "size", column = @Column(name = "ip_size"))
    })
    private IPAddressRange addressRange = new IPAddressRange();

    public @Override AdminRuleIdentifier clone() {
        AdminRuleIdentifier clone;
        try {
            clone = (AdminRuleIdentifier) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.addressRange = addressRange.clone();
        return clone;
    }

    public String username() {
        return ANY.equals(username) ? null : username;
    }

    public String rolename() {
        return ANY.equals(rolename) ? null : rolename;
    }

    public String workspace() {
        return ANY.equals(workspace) ? null : workspace;
    }
}
