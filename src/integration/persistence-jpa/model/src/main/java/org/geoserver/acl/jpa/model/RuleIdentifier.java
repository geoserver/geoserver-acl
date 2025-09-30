/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.acl.jpa.model;

import java.io.Serial;
import java.io.Serializable;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Uniquely identifies a {@link Rule}, all properties are mandatory in order for the {@link Rule}'s
 * unique constraint to be enforced by the database, which otherwise will consider {@literal NULL !=
 * NULL}.
 *
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@Embeddable
public class RuleIdentifier implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ANY = "*";

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", nullable = false)
    private GrantType access = GrantType.DENY;

    @NonNull
    @Column(name = "username", nullable = false)
    private String username = ANY;

    @NonNull
    @Column(name = "rolename", nullable = false)
    private String rolename = ANY;

    @NonNull
    @Column(nullable = false)
    private String service = ANY;

    @NonNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "low", column = @Column(name = "ip_low")),
        @AttributeOverride(name = "high", column = @Column(name = "ip_high")),
        @AttributeOverride(name = "size", column = @Column(name = "ip_size"))
    })
    private IPAddressRange addressRange = new IPAddressRange();

    @NonNull
    @Column(nullable = false)
    private String request = ANY;

    @NonNull
    @Column(nullable = false)
    private String subfield = ANY;

    @NonNull
    @Column(nullable = false)
    private String workspace = ANY;

    @NonNull
    @Column(nullable = false)
    private String layer = ANY;

    public @Override RuleIdentifier clone() {
        RuleIdentifier clone;
        try {
            clone = (RuleIdentifier) super.clone();
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

    public String service() {
        return ANY.equals(service) ? null : service;
    }

    public String request() {
        return ANY.equals(request) ? null : request;
    }

    public String subfield() {
        return ANY.equals(subfield) ? null : subfield;
    }

    public String workspace() {
        return ANY.equals(workspace) ? null : workspace;
    }

    public String layer() {
        return ANY.equals(layer) ? null : layer;
    }
}
