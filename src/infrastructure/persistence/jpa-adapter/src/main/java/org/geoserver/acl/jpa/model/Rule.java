/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "Rule")
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "acl_rule",
        // NOTE unique constraints don't work with null values, so all RuleIdentifier attributes
        // have default values
        //        uniqueConstraints = {
        //            @UniqueConstraint(
        //                   name = "acl_rule_identifier",
        //                    columnNames = {
        //                        "username",
        //                        "rolename",
        //                        "service",
        //                        "ip_low",
        //                        "ip_high",
        //                        "ip_size",
        //                        "request",
        //                        "subfield",
        //                        "workspace",
        //                        "layer",
        //                        "grant_type"
        //                    })
        //        },
        indexes = {
            @Index(name = "idx_rule_priority", columnList = "priority"),
            @Index(name = "idx_rule_service", columnList = "service"),
            @Index(name = "idx_rule_request", columnList = "request"),
            @Index(name = "idx_rule_workspace", columnList = "workspace"),
            @Index(name = "idx_rule_layer", columnList = "layer")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "Rule")
public class Rule extends Auditable implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "acl_rules_sequence_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "acl_rules_sequence_generator", sequenceName = "acl_rule_sequence", allocationSize = 1)
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

    /** Unique, prevent overlapping priorities and force the upper layers to be careful */
    @Column(nullable = false)
    private long priority;

    @Embedded
    private RuleIdentifier identifier = new RuleIdentifier();

    @Embedded
    private LayerDetails layerDetails;

    @Embedded
    private RuleLimits ruleLimits;

    public @Override Rule clone() {
        Rule clone;
        try {
            clone = (Rule) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.identifier = identifier.clone();
        clone.layerDetails = layerDetails == null ? null : layerDetails.clone();
        clone.ruleLimits = ruleLimits == null ? null : ruleLimits.clone();
        return clone;
    }
}
