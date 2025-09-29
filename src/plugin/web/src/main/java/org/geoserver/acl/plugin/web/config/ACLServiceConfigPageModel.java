/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.config;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfig;
import org.geoserver.acl.plugin.accessmanager.config.AclConfigurationManager;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.wicket.model.ExtPropertyModel;

public class ACLServiceConfigPageModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private @Getter CompoundPropertyModel<AccessManagerConfig> configModel;

    private @Getter ExtPropertyModel<String> serviceUrl;

    private @Getter IModel<Boolean> allowRemoteAndInlineLayers;
    private @Getter IModel<Boolean> grantWriteToWorkspacesToAuthenticatedUsers;

    private @Getter IModel<Boolean> useRolesToFilter;

    public static ACLServiceConfigPageModel newInstance() {
        return new ACLServiceConfigPageModel();
    }

    ACLServiceConfigPageModel() {
        AccessManagerConfig config = getConfigManager().getConfiguration().clone();
        configModel = new CompoundPropertyModel<>(config);
        serviceUrl = new ExtPropertyModel<String>(configModel, "serviceUrl");
        allowRemoteAndInlineLayers = new PropertyModel<>(configModel, "allowRemoteAndInlineLayers");
        grantWriteToWorkspacesToAuthenticatedUsers =
                new PropertyModel<>(configModel, "grantWriteToWorkspacesToAuthenticatedUsers");
        useRolesToFilter = new PropertyModel<>(configModel, "useRolesToFilter");
    }

    public AclConfigurationManager getConfigManager() {
        AclConfigurationManager manager = GeoServerExtensions.bean(AclConfigurationManager.class);
        Objects.requireNonNull(manager, AclConfigurationManager.class.getSimpleName() + " bean not found");
        return manager;
    }

    /**
     * @return {@code true} if the ACL service runs in-process, {@code false} if it hits a remote
     *     service
     */
    public boolean isInternal() {
        return false;
    }

    public void testConnection() throws Exception {
        AccessManagerConfig newConfig = configModel.getObject();
        getConfigManager().testConfig(newConfig);
    }

    public void applyAndSaveConfiguration() throws Exception {
        AclConfigurationManager manager = getConfigManager();
        final AccessManagerConfig currentConfig = manager.getConfiguration();
        AccessManagerConfig config = configModel.getObject();

        try {
            manager.setConfiguration(config);
        } catch (Exception e) {
            manager.setConfiguration(currentConfig);
            throw e;
        }

        try {
            manager.storeConfiguration();
        } catch (Exception e) {
            manager.setConfiguration(currentConfig);
            throw e;
        }
    }
}
