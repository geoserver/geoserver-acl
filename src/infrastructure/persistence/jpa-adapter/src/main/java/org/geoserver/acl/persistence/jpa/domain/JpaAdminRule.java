/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.persistence.jpa.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * An AdminRule expresses if a given combination of request access is allowed or not.
 *
 * <p>It's used for setting admin privileges on workspaces.
 *
 * <p>AdminRule filtering and selection is almost identical to {@see Rule}.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
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
                        "username",
                        "rolename",
                        "workspace",
                        "ip_low",
                        "ip_high",
                        "ip_size",
                    })
        },
        indexes = {
            @Index(name = "idx_adminrule_username", columnList = "username"),
            @Index(name = "idx_adminrule_rolename", columnList = "rolename"),
            @Index(name = "idx_adminrule_workspace", columnList = "workspace"),
            @Index(name = "idx_adminrule_grant_type", columnList = "grant_type")
        })
@SecondaryTable(
        name = "acl_adminrule_priority",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "id"),
        foreignKey = @ForeignKey(name = "fk_adminrule_priority_adminrule"),
        indexes = {@Index(name = "idx_adminrule_priority", columnList = "priority")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "Rule")
public class JpaAdminRule extends Auditable {

    @Id
    @GeneratedValue(generator = "acl_adminrules_sequence_generator", strategy = GenerationType.SEQUENCE)
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

    @Column(table = "acl_adminrule_priority", nullable = false)
    private long priority;

    @Embedded
    private JpaAdminRuleIdentifier identifier = new JpaAdminRuleIdentifier();

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", nullable = false)
    private JpaAdminGrantType access = JpaAdminGrantType.USER;

    public JpaAdminRule() {}

    public JpaAdminRule(JpaAdminRule other) {
        super(other);
        this.id = other.id;
        this.extId = other.extId;
        this.name = other.name;
        this.description = other.description;
        this.priority = other.priority;
        this.identifier = new JpaAdminRuleIdentifier(other.identifier);
        this.access = other.access;
    }
}
