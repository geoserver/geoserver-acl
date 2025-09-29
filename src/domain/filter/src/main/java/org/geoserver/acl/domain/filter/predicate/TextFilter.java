/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

/** Contains a fixed text OR a special filtering condition (i.e. ANY, DEFAULT). */
@EqualsAndHashCode(callSuper = true)
public class TextFilter extends RulePredicate<String> implements Serializable, Cloneable {

    private static final long serialVersionUID = 6565336016075974626L;
    private String text;
    private boolean forceUppercase = false;

    public TextFilter(FilterType type) {
        super(type);
    }

    public TextFilter(FilterType type, boolean forceUppercase) {
        super(type);
        this.forceUppercase = forceUppercase;
    }

    public TextFilter(FilterType type, boolean forceUppercase, boolean includeDefault) {
        super(type, includeDefault);
        this.forceUppercase = forceUppercase;
    }

    public TextFilter(String text, boolean forceUppercase, boolean includeDefault) {
        this(text);
        this.forceUppercase = forceUppercase;
        setIncludeDefault(includeDefault);
    }

    public TextFilter(String text) {
        super(FilterType.NAMEVALUE);
        this.text = text;
    }

    public void setHeuristically(String text) {
        if (text == null) {
            this.type = FilterType.DEFAULT;
        } else if (text.equals("*")) {
            this.type = FilterType.ANY;
        } else {
            this.type = FilterType.NAMEVALUE;
            this.text = forceUppercase ? text.toUpperCase() : text;
        }
    }

    public void setText(String name) {
        this.text = forceUppercase ? name.toUpperCase() : name;
        this.type = FilterType.NAMEVALUE;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        switch (type) {
            case ANY:
            case DEFAULT:
                return type.toString();

            case NAMEVALUE:
                return (text == null ? "(null)" : text.isEmpty() ? "(empty)" : '"' + text + '"')
                        + (includeDefault ? "+" : "");

            case IDVALUE:
            default:
                throw new AssertionError();
        }
    }

    @Override
    public TextFilter clone() throws CloneNotSupportedException {
        return (TextFilter) super.clone();
    }

    @Override
    public boolean test(String value) {
        switch (type) {
            case ANY:
                return true;
            case DEFAULT:
                return value == null;
            case NAMEVALUE:
                if (this.isIncludeDefault()) {
                    return value == null || value.equals(getText());
                }
                return value != null && value.equals(getText());
            case IDVALUE:
            default:
                throw new IllegalArgumentException();
        }
    }
}
