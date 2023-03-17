/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under LGPL 2.0 license
 */
package org.geoserver.acl.model.filter.predicate;

import java.io.Serializable;

/** A filter that can be either: - an id - a string - a special constraint (DEFAULT, ANY) */
public class IdNameFilter extends RulePredicate<Object> implements Serializable, Cloneable {

    private static final long serialVersionUID = -5984311150423659545L;
    private Long id;
    private String name;

    public IdNameFilter(FilterType type) {
        super(type);
    }

    public IdNameFilter(FilterType type, boolean includeDefault) {
        super(type, includeDefault);
    }

    public IdNameFilter(long id) {
        super(FilterType.IDVALUE);
        this.id = id;
    }

    public IdNameFilter(long id, boolean includeDefault) {
        super(FilterType.IDVALUE, includeDefault);
        this.id = id;
    }

    public IdNameFilter(String name, boolean includeDefault) {
        super(FilterType.NAMEVALUE, includeDefault);
        this.name = name;
    }

    public void setHeuristically(String name) {
        if (name == null) {
            this.type = FilterType.DEFAULT;
        } else if (name.equals("*")) {
            this.type = FilterType.ANY;
        } else {
            this.type = FilterType.NAMEVALUE;
            this.name = name;
        }
    }

    public void setHeuristically(Long id) {
        if (id == null) {
            this.type = FilterType.DEFAULT;
        } else {
            this.type = FilterType.IDVALUE;
            this.id = id;
        }
    }

    public void setId(Long id) {
        this.id = id;
        this.type = FilterType.IDVALUE;
    }

    public void setName(String name) {
        this.name = name;
        this.type = FilterType.NAMEVALUE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        switch (type) {
            case ANY:
            case DEFAULT:
                return type.toString();

            case IDVALUE:
                return "id"
                        + (includeDefault ? "+:" : ":")
                        + (id == null ? "(null)" : id.toString());

            case NAMEVALUE:
                String tmp;
                if (name == null) tmp = "(null)";
                else if (name.isEmpty()) tmp = "(empty)";
                else tmp = name;
                return "name" + (includeDefault ? "+:" : ":") + tmp;

            default:
                throw new AssertionError();
        }
    }

    @Override
    public IdNameFilter clone() throws CloneNotSupportedException {
        return (IdNameFilter) super.clone();
    }

    @Override
    public boolean test(Object value) {
        switch (type) {
            case ANY:
                return true;
            case DEFAULT:
                return value == null || value.equals(getName());
            case NAMEVALUE:
                if (this.isIncludeDefault()) {
                    return value == null || value.equals(getName());
                }
                return value != null && value.equals(getName());
            case IDVALUE:
                if (this.isIncludeDefault()) {
                    return value == null || value.equals(getId());
                }
                return value != null && value.equals(getId());
            default:
                throw new IllegalArgumentException();
        }
    }
}
