/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.components;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Select2MultiChoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Select2SetMultiChoice<T> extends FormComponentPanel<Set<T>> {

    private Select2MultiChoice<T> select2;

    public Select2SetMultiChoice(String id, IModel<Set<T>> model, ChoiceProvider<T> provider) {
        super(id, model);

        Collection<T> initalValue =
                new ArrayList<T>(model.getObject() == null ? Set.of() : model.getObject());
        IModel<Collection<T>> selectModel = Model.of(initalValue);

        add(select2 = new Select2MultiChoice<T>("select", selectModel, provider));
        select2.getSettings().setQueryParam("qm");
        select2.getSettings().setPlaceholder("select"); // required for allowClear
        select2.getSettings().setAllowClear(true);
        select2.getSettings().setWidth("50%");
        select2.setOutputMarkupPlaceholderTag(true);
        this.setOutputMarkupPlaceholderTag(true);
    }

    @Override
    public void convertInput() {
        Collection<T> choices = select2.getConvertedInput();
        setConvertedInput(new LinkedHashSet<>(choices));
    }
}
