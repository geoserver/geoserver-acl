/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.adminrules.model;

import lombok.NonNull;

import org.geoserver.acl.domain.adminrules.AdminRule;
import org.geoserver.acl.domain.adminrules.AdminRuleAdminService;
import org.geoserver.acl.plugin.web.components.AbstractRuleEditModel;
import org.geoserver.web.GeoServerApplication;

@SuppressWarnings("serial")
public class AdminRuleEditModel extends AbstractRuleEditModel<MutableAdminRule> {

    public AdminRuleEditModel() {
        this(new MutableAdminRule());
    }

    public AdminRuleEditModel(@NonNull MutableAdminRule rule) {
        super(rule);
    }

    @Override
    protected String getRoleName(MutableAdminRule rule) {
        return rule.getRoleName();
    }

    @Override
    public void save() {
        MutableAdminRule modelRule = getModel().getObject();
        if (isIncludeInstanceName()) {
            String instanceName = getInstanceName();
            modelRule.setInstanceName(instanceName);
        } else {
            modelRule.setInstanceName(null);
        }

        AdminRuleAdminService service = adminService();
        if (null == modelRule.getId()) {
            AdminRule newRule = modelRule.toRule();
            AdminRule created = service.insert(newRule);
            modelRule.from(created);
        } else {
            AdminRule current = loadDomainRule();
            AdminRule toUpdate = modelRule.toRule(current);
            AdminRule updated = service.update(toUpdate);
            modelRule.from(updated);
        }
    }

    public AdminRule loadDomainRule() {
        MutableAdminRule modelRule = getModel().getObject();
        AdminRuleAdminService service = adminService();
        AdminRule current =
                service.get(modelRule.getId())
                        .orElseThrow(() -> new IllegalStateException("The rule no longer exists"));
        return current;
    }

    private static AdminRuleAdminService adminService() {
        return GeoServerApplication.get().getBeanOfType(AdminRuleAdminService.class);
    }

    @Override
    protected String getInstanceName(MutableAdminRule rule) {
        return rule.getInstanceName();
    }
}
