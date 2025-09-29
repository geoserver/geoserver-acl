/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.geoserver.acl.domain.rules.CatalogMode;
import org.geoserver.acl.domain.rules.LayerAttribute;
import org.geoserver.acl.domain.rules.LayerAttribute.AccessType;

/**
 * @author "etj (Emanuele Tajariol @ GeoSolutions)" - Originally as part of GeoFence's GeoServer
 *     extension
 */
public class AccessInfoUtils {

    public static String intersectCQL(String c1, String c2) {
        if (c1 == null) {
            return c2;
        }
        if (c2 == null) {
            return c1;
        }

        return "(" + c1 + ") AND (" + c2 + ")";
    }

    public static Set<LayerAttribute> intersectAttributes(Set<LayerAttribute> s1, Set<LayerAttribute> s2) {
        if (s1 == null) {
            return s2;
        }
        if (s2 == null) {
            return s1;
        }

        Map<String, LayerAttribute[]> map = new HashMap<>();
        for (LayerAttribute la : s1) {
            map.put(la.getName(), new LayerAttribute[] {la, null});
        }
        for (LayerAttribute la : s2) {
            LayerAttribute[] arr = map.computeIfAbsent(la.getName(), k -> new LayerAttribute[] {null, la});
            arr[1] = la;
        }

        Set<LayerAttribute> ret = new HashSet<>();
        for (LayerAttribute[] arr : map.values()) {
            if (arr[0] == null) {
                ret.add(arr[1]);
            }
            if (arr[1] == null) {
                ret.add(arr[0]);
            }

            LayerAttribute la = LayerAttribute.builder()
                    .name(arr[0].getName())
                    .dataType(arr[0].getDataType())
                    .access(getStricter(arr[0].getAccess(), arr[1].getAccess()))
                    .build();

            ret.add(la);
        }
        return ret;
    }

    public static AccessType getStricter(AccessType a1, AccessType a2) {
        if (a1 == null || a2 == null) return AccessType.NONE; // should not happen
        if (a1 == AccessType.NONE || a2 == AccessType.NONE) return AccessType.NONE;
        if (a1 == AccessType.READONLY || a2 == AccessType.READONLY) return AccessType.READONLY;
        return AccessType.READWRITE;
    }

    public static CatalogMode getStricter(CatalogMode m1, CatalogMode m2) {
        if (m1 == null) {
            return m2;
        }
        if (m2 == null) {
            return m1;
        }
        if (CatalogMode.HIDE == m1 || CatalogMode.HIDE == m2) {
            return CatalogMode.HIDE;
        }
        if (CatalogMode.MIXED == m1 || CatalogMode.MIXED == m2) {
            return CatalogMode.MIXED;
        }
        return CatalogMode.CHALLENGE;
    }

    public static CatalogMode getLarger(CatalogMode m1, CatalogMode m2) {
        if (m1 == null) {
            return m2;
        }
        if (m2 == null) {
            return m1;
        }
        if (CatalogMode.CHALLENGE == m1 || CatalogMode.CHALLENGE == m2) {
            return CatalogMode.CHALLENGE;
        }
        if (CatalogMode.MIXED == m1 || CatalogMode.MIXED == m2) {
            return CatalogMode.MIXED;
        }
        return CatalogMode.HIDE;
    }
}
