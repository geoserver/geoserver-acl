/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * An AdminRule expresses if a given combination of request access is allowed or not.
 *
 * <p>It's used for setting admin privileges on workspaces.
 *
 * <p>AdminRule filtering and selection is almost identical to {@see Rule}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity(name = "AdminRule")
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "acl_adminrule",
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {
                        "instance",
                        "username",
                        "rolename",
                        "workspace",
                        "ip_low",
                        "ip_high",
                        "ip_size",
                    })
        },
        indexes = {
            @Index(name = "idx_adminrule_priority", columnList = "priority"),
            @Index(name = "idx_adminrule_username", columnList = "username"),
            @Index(name = "idx_adminrule_rolename", columnList = "rolename"),
            @Index(name = "idx_adminrule_workspace", columnList = "workspace"),
            @Index(name = "idx_adminrule_grant_type", columnList = "grant_type")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "Rule")
public class AdminRule extends Auditable implements Cloneable {
    private static final long serialVersionUID = 422357467611162461L;

    @Id
    @GeneratedValue(
            generator = "acl_adminrules_sequence_generator",
            strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
            name = "acl_adminrules_sequence_generator",
            sequenceName = "acl_adminrule_sequence",
            allocationSize = 1)
    private Long id;

    /**
     * External Id. An ID used in an external systems. This field should simplify Authorization
     * integration in complex systems.
     */
    @Column(nullable = true, updatable = false, unique = true)
    private String extId;

    private String name;

    @Column(length = 4096)
    private String description;

    private long priority;

    @Embedded private AdminRuleIdentifier identifier = new AdminRuleIdentifier();

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", nullable = false)
    private AdminGrantType access = AdminGrantType.USER;

    // visible for testing
    public @Override AdminRule clone() {
        AdminRule clone;
        try {
            clone = (AdminRule) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.identifier = identifier.clone();
        return clone;
    }
}
