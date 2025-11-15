/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.rules;

/**
 * The security mode in which the GeoServer Catalog should respond to requests for a specific resource (layer/feature type/coverage).
 * <p>
 * Matches the GeoServer catalog mode options.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
public enum CatalogMode {
    /**
     * If the user does not have enough authorities, just pretend the layers that cannot be read are not there, and
     * those that cannot be written are read only (never ask for authentication, which should be performed prior to data
     * access)
     */
    HIDE,
    /**
     * Always list of all the layers and allow access to each layer metadata. If a user tries to access the data and she
     * cannot read, or to write data and she cannot write, challenge her with an authentication request. This mode does
     * not hide the existence of layers, and should work fine with most applications requiring authentication.
     */
    CHALLENGE,
    /**
     * A mixed approach. The methods that do list the contents of the catalog do not report the layers the current user
     * cannot access to, but trying to access the layer directly generates a {@code SpringSecurityException} that will
     * challenge the user for authentication. This approach assumes the capabilities requests are using the listing
     * methods, whilst any access by name is performed using the direct access methods. This is reasonable, but cannot
     * be guaranteed, so this approach is bound to be more fragile than the other two, given it's based on a programming
     * convention that cannot be enforced.
     */
    MIXED;

    public static CatalogMode stricter(CatalogMode m1, CatalogMode m2) {

        if (m1 == null) return m2;
        if (m2 == null) return m1;

        if (HIDE == m1 || HIDE == m2) return HIDE;

        if (MIXED == m1 || MIXED == m2) return MIXED;

        return CHALLENGE;
    }

    public static CatalogMode lenient(CatalogMode m1, CatalogMode m2) {

        if (m1 == null) return m2;
        if (m2 == null) return m1;

        if (CHALLENGE == m1 || CHALLENGE == m2) return CHALLENGE;

        if (MIXED == m1 || MIXED == m2) return MIXED;

        return HIDE;
    }
}
