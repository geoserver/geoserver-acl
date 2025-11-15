/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

/**
 * Specifies where to insert a new rule in the priority order.
 *
 * <p>Rules are evaluated in priority order, where lower priority values take precedence. This enum
 * controls how a new rule's priority is determined during insertion.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan (adapted to GeoServer ACL)
 */
public enum InsertPosition {

    /**
     * Uses the rule's existing priority value.
     *
     * <p>The priority must be explicitly set on the rule being inserted. Other rules with equal or
     * higher priorities are shifted down to make room.
     */
    FIXED,

    /**
     * Inserts at a position counting from the highest priority (lowest value).
     *
     * <p>Position 0 inserts at the very beginning (highest priority), position 1 inserts as the
     * second rule, and so on. The actual priority value is calculated based on existing rules.
     */
    FROM_START,

    /**
     * Inserts at a position counting from the lowest priority (highest value).
     *
     * <p>Position 0 inserts at the very end (lowest priority), position 1 inserts as the
     * second-to-last rule, and so on. The actual priority value is calculated based on existing
     * rules.
     */
    FROM_END
}
