/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

/**
 * Used in DAOs and Services
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
public enum InsertPosition {

    /** priority is a fixed value */
    FIXED,
    /** priority is the position from start (0 is the first one) */
    FROM_START,
    /** * priority is the position from end (0 is the last one) */
    FROM_END
}
