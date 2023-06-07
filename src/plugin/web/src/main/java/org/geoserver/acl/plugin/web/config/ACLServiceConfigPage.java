/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoServer 2.24-SNAPSHOT under GPL 2.0 license (org.geoserver.geofence.web.GeofencePage)
 */
package org.geoserver.acl.plugin.web.config;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.AbstractSubmitLink;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.acl.plugin.accessmanager.AccessManagerConfig;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.wicket.model.ExtPropertyModel;

import java.util.logging.Level;

/**
 * ACL wicket administration UI for GeoServer.
 *
 * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it" - Originally as part of
 *     GeoFence's GeoServer extension
 */
public class ACLServiceConfigPage extends GeoServerSecuredPage {

    private static final long serialVersionUID = 5845823599005718408L;

    private ACLServiceConfigPageModel pageModel;

    public ACLServiceConfigPage() {
        pageModel = ACLServiceConfigPageModel.newInstance();

        Form<AccessManagerConfig> form = new Form<>("form", pageModel.getConfigModel());
        form.setOutputMarkupId(true);
        super.add(form);

        form.add(instanceName());
        // TODO: allow to configure the url, user, and pwd
        form.add(serviceURLField());
        form.add(testConnectionLink());

        //        form.add(allowRemoteAndInlineLayers());
        //        form.add(grantWriteToWorkspacesToAuthenticatedUsers());
        //        form.add(useRolesToFilter());

        // form.add(new TextField<>("acceptedRoles", new PropertyModel<>(configModel,
        // "acceptedRoles")));

        //        form.add(submitButton());
        //        form.add(cancelButton());
    }

    private CheckBox useRolesToFilter() {
        return new CheckBox("useRolesToFilter", pageModel.getUseRolesToFilter());
    }

    private CheckBox grantWriteToWorkspacesToAuthenticatedUsers() {
        return new CheckBox(
                "grantWriteToWorkspacesToAuthenticatedUsers",
                pageModel.getGrantWriteToWorkspacesToAuthenticatedUsers());
    }

    private CheckBox allowRemoteAndInlineLayers() {
        return new CheckBox(
                "allowRemoteAndInlineLayers", pageModel.getAllowRemoteAndInlineLayers());
    }

    private FormComponent<String> instanceName() {
        FormComponent<String> instanceName =
                new TextField<>("instanceName", pageModel.getInstanceName()).setRequired(true);
        instanceName.setEnabled(false);
        return instanceName;
    }

    private TextField<String> serviceURLField() {
        final boolean isInternal = pageModel.isInternal();
        ExtPropertyModel<String> serviceUrl = pageModel.getServiceUrl().setReadOnly(isInternal);
        TextField<String> serviceURLField = new TextField<>("servicesUrl", serviceUrl);
        serviceURLField.setRequired(true);
        //        serviceURLField.setEnabled(!isInternal);
        serviceURLField.setEnabled(false);
        return serviceURLField;
    }

    private AbstractSubmitLink testConnectionLink() {
        return new AjaxSubmitLink("test") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    ((FormComponent<?>) form.get("servicesUrl")).processInput();
                    // String servicesUrl = (String) ((FormComponent<?>)
                    // form.get("servicesUrl")).getConvertedInput();
                    pageModel.testConnection();
                    info(
                            new StringResourceModel(
                                            ACLServiceConfigPage.class.getSimpleName()
                                                    + ".connectionSuccessful")
                                    .getObject());
                } catch (Exception e) {
                    error(e);
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }

                if (getPage() instanceof GeoServerBasePage) {
                    ((GeoServerBasePage) getPage()).addFeedbackPanels(target);
                }
            }
        }.setDefaultFormProcessing(false);
    }

    private Button submitButton() {
        return new Button("submit") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                try {
                    pageModel.applyAndSaveConfiguration();
                    doReturn();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Save error", e);
                    error(e);
                }
            }
        };
    }

    private Button cancelButton() {
        return new Button("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                doReturn();
            }
        }.setDefaultFormProcessing(false);
    }
}
