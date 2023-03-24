/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.model.IModel;
import org.geoserver.acl.plugin.web.support.SerializableFunction;

import java.util.Iterator;

/**
 * {@link AutoCompleteTextField} that dynamically updates the model value by calling {@code
 * getModel().setObject(getConvertedInput())}
 *
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ModelUpdatingAutoCompleteTextField<T> extends AutoCompleteTextField<T> {

    private SerializableFunction<String, Iterator<T>> choiceResolver;

    public ModelUpdatingAutoCompleteTextField(
            String id, IModel<T> model, SerializableFunction<String, Iterator<T>> choiceResolver) {
        this(
                id,
                model,
                choiceResolver,
                new AutoCompleteSettings().setMaxHeightInPx(200).setShowListOnEmptyInput(true));
    }

    public ModelUpdatingAutoCompleteTextField(
            String id,
            IModel<T> model,
            SerializableFunction<String, Iterator<T>> choiceResolver,
            AutoCompleteSettings settings) {
        super(id, model, settings);
        this.choiceResolver = choiceResolver;

        add(
                new OnChangeAjaxBehavior() {
                    protected @Override void onUpdate(AjaxRequestTarget target) {
                        T convertedInput = getConvertedInput();
                        getModel().setObject(convertedInput);
                    }
                });
    }

    @Override
    protected Iterator<T> getChoices(String input) {
        return choiceResolver.apply(input);
    }
}
