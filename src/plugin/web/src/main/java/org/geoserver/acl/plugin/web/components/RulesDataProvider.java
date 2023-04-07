/* (c) 2023 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.plugin.web.components;

import com.google.common.base.Objects;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("serial")
public abstract class RulesDataProvider<R> extends GeoServerDataProvider<R> {

    private final List<R> _rules = new ArrayList<>();

    private Class<R> modelClass;

    public RulesDataProvider(Class<R> modelClass) {
        this.modelClass = modelClass;
        setSort("priority", SortOrder.ASCENDING);
    }

    public Class<R> getModelClass() {
        return modelClass;
    }

    @Override
    protected Comparator<R> getComparator(SortParam<?> sort) {
        return null;
    }

    @Override
    public void setSort(SortParam<Object> param) {
        super.setSort(param);
        Collections.sort(getItems(), super.getComparator(param));
    }

    @Override
    public List<R> getItems() {
        if (_rules.isEmpty()) reload();
        return _rules;
    }

    private final void reload() {
        List<R> fresh = doReload();
        _rules.clear();
        _rules.addAll(fresh);
    }

    public void remove(Collection<R> selected) {
        // rules.removeAll(selected);
        if (selected.isEmpty()) return;
        for (R rule : selected) {
            delete(rule);
        }
        reload();
    }

    public void save(R rule) throws DuplicateKeyException {
        update(rule);
        reload();
    }

    public boolean canUp(R rule) {
        return getItems().indexOf(rule) > 0;
    }

    public void moveUp(R rule) {
        List<R> rules = getItems();
        int index = rules.indexOf(rule);
        if (index > 0) {
            swap(rule, rules.get(index - 1));
            rules.remove(index);
            rules.add(index - 1, rule);
        }
    }

    public boolean canDown(R rule) {
        List<R> rules = getItems();
        return rules.indexOf(rule) < rules.size() - 1;
    }

    public void moveDown(R rule) {
        List<R> rules = getItems();
        int index = rules.indexOf(rule);
        if (index < rules.size() - 1) {
            swap(rule, rules.get(index + 1));
            rules.remove(index);
            rules.add(index + 1, rule);
        }
    }

    protected abstract R update(R rule) throws DuplicateKeyException;

    public void onDrop(R movedRule, R targetRule) {
        if (Objects.equal(getId(movedRule), getId(targetRule))) {
            return;
        }
        final long pmoved = getPriority(movedRule);
        long ptarget = getPriority(targetRule);
        if (pmoved < ptarget) {
            setPriority(movedRule, ptarget + 1);
        } else {
            setPriority(movedRule, ptarget);
        }
        save(movedRule);
    }

    @Override
    public abstract List<Property<R>> getProperties();

    protected abstract void delete(R rule);

    protected abstract void swap(R rule, R otherRule);

    protected abstract List<R> doReload();

    protected abstract String getId(R movedRule);

    protected abstract long getPriority(R movedRule);

    protected abstract void setPriority(R movedRule, long l);
}
