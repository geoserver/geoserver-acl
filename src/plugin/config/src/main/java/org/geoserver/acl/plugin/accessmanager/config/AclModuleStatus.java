/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license
 */
package org.geoserver.acl.plugin.accessmanager.config;

import java.io.Serial;
import org.geoserver.platform.ModuleStatusImpl;

public final class AclModuleStatus extends ModuleStatusImpl {

    @Serial
    private static final long serialVersionUID = 1L;

    public AclModuleStatus(String module, String name) {
        super(module, name);
    }
}
