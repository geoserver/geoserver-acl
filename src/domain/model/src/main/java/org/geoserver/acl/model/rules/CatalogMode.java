/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.model.rules;

/**
 * CatalogMode is the mode used in geoserver for a given layer.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
public enum CatalogMode {
    HIDE,
    CHALLENGE,
    MIXED;
}
