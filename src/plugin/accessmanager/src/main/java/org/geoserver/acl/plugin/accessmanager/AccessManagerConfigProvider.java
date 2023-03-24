/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.accessmanager;

import java.util.function.Supplier;

@FunctionalInterface
public interface AccessManagerConfigProvider extends Supplier<AccessManagerConfig> {}
