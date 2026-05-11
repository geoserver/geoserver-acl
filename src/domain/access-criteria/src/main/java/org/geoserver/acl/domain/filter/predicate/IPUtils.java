/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.filter.predicate;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * Utility for validating IP address literals as used by ACL filter predicates.
 *
 * <p>Exposes two pattern-based checks:
 *
 * <ul>
 *   <li>{@link #isAddressValid(String)} - tests whether a string is a syntactically valid
 *       IPv4 dotted-quad address or an IPv6 address in either its standard or compressed form.
 *   <li>{@link #isRangeValid(String)} - tests whether a string is a valid CIDR range
 *       (an IPv4 or IPv6 address followed by {@code /<prefix-length>}).
 * </ul>
 *
 * <p>Both methods return {@code false} for {@code null} input and rely purely on regular
 * expressions; numeric ranges (e.g. IPv4 octet {@code 0-255}, CIDR prefix bounds) are not
 * enforced beyond their digit count.
 *
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 * @author Gabriel Roldan - Camptocamp
 */
@UtilityClass
class IPUtils {
    /**
     * Regular expression for an IPv4 address in dotted-quad notation
     * (e.g. {@code 192.168.0.1}).
     *
     * <p>Each octet is matched as one to three decimal digits; numeric range
     * (0-255) is not enforced by the pattern.
     */
    private static final String IPV4_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";

    /**
     * Regular expression for an IPv6 address in the full, uncompressed form
     * of eight hexadecimal groups separated by colons
     * (e.g. {@code 2001:0db8:85a3:0000:0000:8a2e:0370:7334}).
     */
    private static final String IPV6_STANDARD_ADDRESS = "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}";

    /**
     * Regular expression for an IPv6 address in the compressed form using
     * {@code ::} to elide one or more zero groups (e.g. {@code ::1},
     * {@code 2001:db8::1}, {@code fe80::}).
     *
     * <p>Uses possessive quantifiers ({@code *+}) on the inner repetitions to
     * prevent catastrophic backtracking on inputs that look like an IPv6
     * address but do not contain {@code ::}.
     */
    private static final String IPV6_COMPRESSED_ADDRESS =
            "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*+)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*+)?)";

    private static final String SLASH_FORMAT = "/(\\d{1,3})";

    private static final Pattern[] ipAddressPatterns = new Pattern[] {
        Pattern.compile(IPV4_ADDRESS), Pattern.compile(IPV6_STANDARD_ADDRESS), Pattern.compile(IPV6_COMPRESSED_ADDRESS)
    };

    private static final Pattern[] cidrPatterns = new Pattern[] {
        Pattern.compile(IPV4_ADDRESS + SLASH_FORMAT),
        Pattern.compile(IPV6_STANDARD_ADDRESS + SLASH_FORMAT),
        Pattern.compile(IPV6_COMPRESSED_ADDRESS + SLASH_FORMAT)
    };

    public static boolean isAddressValid(String ipAddress) {
        return checkAllPatterns(ipAddress, ipAddressPatterns);
    }

    public static boolean isRangeValid(String ipAddressRange) {
        return checkAllPatterns(ipAddressRange, cidrPatterns);
    }

    private static boolean checkAllPatterns(String ipAddress, Pattern[] patterns) {
        if (ipAddress == null) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(ipAddress).matches()) {
                return true;
            }
        }
        return false;
    }
}
