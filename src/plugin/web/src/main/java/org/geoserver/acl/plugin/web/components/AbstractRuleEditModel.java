/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.components;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfig;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AbstractRuleEditModel<R extends Serializable> extends AbstractRulesModel {

    private @Getter CompoundPropertyModel<R> model;

    private @Getter @Setter boolean includeInstanceName;

    public AbstractRuleEditModel(@NonNull R rule) {
        model = new CompoundPropertyModel<>(rule);
        includeInstanceName = hasInstanceOrDefault(rule);
    }

    private boolean hasInstanceOrDefault(@NonNull R rule) {
        String ruleInstanceName = getInstanceName(rule);
        boolean hasInstance = StringUtils.hasText(ruleInstanceName);
        return hasInstance || isDefaultIncludeInstanceName();
    }

    protected boolean isDefaultIncludeInstanceName() {
        AccessManagerConfig config = config();
        boolean defaultIncludeInstance = config.isCreateWithInstanceName();
        return defaultIncludeInstance;
    }

    public abstract void save();

    protected abstract String getInstanceName(R rule);

    public @Override String getSelectedRoleName() {
        return getRoleName(getModel().getObject());
    }

    /**
     * @see #getUserChoices(String)
     */
    protected abstract String getRoleName(R rule);
}
