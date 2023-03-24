/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license (org.geoserver.geofence.server.web.GeofenceServerPage)
 */
package org.geoserver.acl.plugin.web.accessrules;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.geoserver.acl.plugin.web.accessrules.model.DataAccessRuleEditModel;
import org.geoserver.acl.plugin.web.accessrules.model.DataAccessRulesDataProvider;
import org.geoserver.acl.plugin.web.accessrules.model.MutableRule;
import org.geoserver.acl.plugin.web.components.RulesTablePanel;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.GeoServerTablePanel;

/**
 * @author Niels Charlier - Originally as part of GeoFence's GeoServer extension
 */
@SuppressWarnings("serial")
public class AccessRulesACLPage extends GeoServerSecuredPage {

    private DataAccessRulesDataProvider dataProvider;

    private GeoServerTablePanel<MutableRule> rulesPanel;

    private AjaxLink<Object> removeLink;

    public AccessRulesACLPage() {
        dataProvider = new DataAccessRulesDataProvider();

        add(rulesPanel = rulesTablePanel());
        setHeaderPanel(headerPanel());
    }

    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);
        header.add(addNewLink());
        header.add(removeLink = removeLink());
        return header;
    }

    private AjaxLink<Object> removeLink() {
        AjaxLink<Object> link =
                new AjaxLink<Object>("removeSelected") {
                    public @Override void onClick(AjaxRequestTarget target) {
                        dataProvider.remove(rulesPanel.getSelection());
                        rulesPanel.clearSelection();
                        target.add(rulesPanel);
                    }
                };
        link.setOutputMarkupId(true);
        link.setEnabled(false);
        return link;
    }

    private AjaxLink<Object> addNewLink() {
        return new AjaxLink<Object>("addNew") {
            public @Override void onClick(AjaxRequestTarget target) {
                setResponsePage(new DataAccessRuleEditPage(new DataAccessRuleEditModel()));
            }
        };
    }

    private RulesTablePanel<MutableRule> rulesTablePanel() {
        RulesTablePanel<MutableRule> panel = new RulesTablePanel<>("rulesPanel", dataProvider);
        panel.setOnDrop(
                (moved, target) -> {
                    dataProvider.onDrop(moved, target);
                    doReturn(AccessRulesACLPage.class);
                });
        panel.setOnSelectionUpdate(
                target -> {
                    removeLink.setEnabled(rulesPanel.getSelection().size() > 0);
                    target.add(removeLink);
                });
        panel.setOnEdit(
                rule -> {
                    DataAccessRuleEditModel editModel = new DataAccessRuleEditModel(rule);
                    setResponsePage(new DataAccessRuleEditPage(editModel));
                });
        return panel;
    }
}
