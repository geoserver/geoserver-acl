/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter.predicate;

import java.io.Serial;
import java.io.Serializable;

/**
 * Predicate for filtering rules by IP address or CIDR ranges.
 *
 * <p>Matches client IPs against rule address restrictions:
 * <ul>
 *   <li><b>Single IP:</b> Exact match (e.g., "192.168.1.100")
 *   <li><b>CIDR:</b> IPv4 subnet (e.g., "192.168.1.0/24" matches 192.168.1.0-255)
 *   <li><b>IPv6:</b> Pattern validation (full CIDR support not implemented)
 *   <li><b>ANY:</b> Match all IPs (wildcard "*")
 *   <li><b>DEFAULT:</b> Match only rules with null address (no IP restriction)
 * </ul>
 *
 * <p>{@link #setHeuristically(String)} parses: null -> DEFAULT, "*" -> ANY, IP/CIDR -> NAMEVALUE.
 *
 * <p>Example:
 * <pre>{@code
 * // Specific IP
 * IPAddressRangeFilter filter = new IPAddressRangeFilter("192.168.1.100");
 * filter.test("192.168.1.100"); // true
 *
 * // Subnet
 * IPAddressRangeFilter subnet = new IPAddressRangeFilter("192.168.1.0/24");
 * subnet.test("192.168.1.0");   // true
 * subnet.test("192.168.1.255"); // true
 * }</pre>
 *
 * @since 1.0
 * @see RulePredicate
 */
public class IPAddressRangeFilter extends RulePredicate<String> implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 4180011525571457537L;

    /** IP address or CIDR range to match (only used when type is NAMEVALUE). */
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
