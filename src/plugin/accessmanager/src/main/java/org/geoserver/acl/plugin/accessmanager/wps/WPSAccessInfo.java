/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.wps;

import org.geoserver.acl.authorization.AccessInfo;
import org.locationtech.jts.geom.Geometry;

public class WPSAccessInfo {
    AccessInfo accessInfo;
    Geometry area;
    Geometry clip;

    public WPSAccessInfo(AccessInfo accessInfo) {
        this.accessInfo = accessInfo;
        this.area = null;
        this.clip = null;
    }

    public WPSAccessInfo(AccessInfo accessInfo, Geometry area, Geometry clip) {
        this.accessInfo = accessInfo;
        this.area = area;
        this.clip = clip;
    }

    public AccessInfo getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(AccessInfo accessInfo) {
        this.accessInfo = accessInfo;
    }

    public Geometry getArea() {
        return area;
    }

    public void setArea(Geometry area) {
        this.area = area;
    }

    public Geometry getClip() {
        return clip;
    }

    public void setClip(Geometry clip) {
        this.clip = clip;
    }
}
