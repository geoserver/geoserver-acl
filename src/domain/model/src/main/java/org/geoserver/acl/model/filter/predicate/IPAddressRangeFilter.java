/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.model.filter.predicate;

import org.geoserver.acl.model.rules.IPAddressRange;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

/** Contains a fixed text OR a special filtering condition (i.e. ANY, DEFAULT). */
public class IPAddressRangeFilter extends RulePredicate<IPAddressRange>
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 4180011525571457537L;
    private String text;

    public IPAddressRangeFilter(FilterType type) {
        super(type);
    }

    public IPAddressRangeFilter(FilterType type, boolean includeDefault) {
        super(type, includeDefault);
    }

    public IPAddressRangeFilter(String text, boolean includeDefault) {
        this(text);
        this.includeDefault = includeDefault;
    }

    public IPAddressRangeFilter(String text) {
        super(FilterType.NAMEVALUE);
        this.text = text;
    }

    public void setHeuristically(String text) {
        if (text == null) {
            this.type = FilterType.DEFAULT;
        } else if (text.equals("*")) {
            this.type = FilterType.ANY;
        } else {
            this.text = text;
            this.type = FilterType.NAMEVALUE;
        }
    }

    public void setText(String value) {
        this.text = value;
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
    public IPAddressRangeFilter clone() throws CloneNotSupportedException {
        return (IPAddressRangeFilter) super.clone();
    }

    @Override
    public boolean test(IPAddressRange addressRange) {
        return toIPAddressPredicate(Function.identity()).test(addressRange);
    }

    public <T> Predicate<T> toIPAddressPredicate(Function<T, IPAddressRange> addrRangeExtractor) {
        return FilterUtils.filterByAddress(this, addrRangeExtractor);
    }
}
