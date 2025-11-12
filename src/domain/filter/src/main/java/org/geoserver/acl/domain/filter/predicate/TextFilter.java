/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serial;
import java.io.Serializable;
import lombok.EqualsAndHashCode;

/**
 * Predicate for filtering rules by text fields.
 *
 * <p>Matches string values against rule fields (username, rolename, service, request,
 * workspace, layer). Supports three modes:
 * <ul>
 *   <li><b>NAMEVALUE:</b> Match exact text (e.g., "admin", "WMS")
 *   <li><b>ANY:</b> Match all rules (wildcard "*")
 *   <li><b>DEFAULT:</b> Match only rules with null value (catch-all rules)
 * </ul>
 *
 * <p>The {@code forceUppercase} flag normalizes text to uppercase for OGC service/request names
 * (WMS, WFS, GetMap, GetFeature) where the spec requires case-insensitive matching.
 *
 * <p>{@link #setHeuristically(String)} parses inputs:
 * null -> DEFAULT, "*" -> ANY, other -> NAMEVALUE.
 *
 * <p>Example:
 * <pre>{@code
 * TextFilter userFilter = new TextFilter("admin");
 * userFilter.test("admin"); // true
 *
 * TextFilter serviceFilter = new TextFilter(FilterType.ANY);
 * serviceFilter.test("WMS"); // true
 * }</pre>
 *
 * @since 1.0
 * @see RulePredicate
 * @see FilterType
 */
@EqualsAndHashCode(callSuper = true)
public class TextFilter extends RulePredicate<String> implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 6565336016075974626L;

    /** Text value to match (only used when type is NAMEVALUE). */
    private String text;

    /** Force uppercase for case-insensitive matching (used for OGC service/request names). */
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
