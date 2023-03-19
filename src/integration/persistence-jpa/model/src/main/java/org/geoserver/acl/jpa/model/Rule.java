/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */

package org.geoserver.acl.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
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
        //                        "instance",
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

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column private Long id;

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

    @Embedded private RuleIdentifier identifier = new RuleIdentifier();

    @Embedded private LayerDetails layerDetails;

    @Embedded private RuleLimits ruleLimits;

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
