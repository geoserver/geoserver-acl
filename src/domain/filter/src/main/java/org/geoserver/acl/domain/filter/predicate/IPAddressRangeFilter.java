/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serial;
import java.io.Serializable;

public class IPAddressRangeFilter extends RulePredicate<String> implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 4180011525571457537L;

    private String ipAddress;

    public IPAddressRangeFilter(FilterType type) {
        super(type);
    }

    public IPAddressRangeFilter(FilterType type, boolean includeDefault) {
        super(type, includeDefault);
    }

    public IPAddressRangeFilter(String ipAddress, boolean includeDefault) {
        this(ipAddress);
        this.includeDefault = includeDefault;
    }

    public IPAddressRangeFilter(String ipAddress) {
        super(FilterType.NAMEVALUE);
        this.ipAddress = ipAddress;
    }

    public void setHeuristically(String ipAddress) {
        if (ipAddress == null) {
            this.type = FilterType.DEFAULT;
        } else if (ipAddress.equals("*")) {
            this.type = FilterType.ANY;
        } else {
            this.ipAddress = ipAddress;
            this.type = FilterType.NAMEVALUE;
        }
    }

    public void setAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        this.type = FilterType.NAMEVALUE;
    }

    public String getAddress() {
        return ipAddress;
    }

    @Override
    public String toString() {
        switch (type) {
            case ANY:
            case DEFAULT:
                return type.toString();

            case NAMEVALUE:
                return (ipAddress == null ? "(null)" : ipAddress.isEmpty() ? "(empty)" : '"' + ipAddress + '"')
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
    public boolean test(String addressRange) {
        return FilterUtils.filterByAddress(this, addressRange);
    }

    public static String toCidrSignature(Long low, Integer size) {
        if (low != null && size != null) {
            SubnetV4Utils su = new SubnetV4Utils(low, size);
            return su.getInfo().getCidrSignature();
        }
        return null;
    }
}
