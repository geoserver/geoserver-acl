/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.filter.predicate;

import java.util.regex.Pattern;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
class IPUtils {
    private static final String IPV4_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String IPV6_STANDARD_ADDRESS = "(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}";
    private static final String IPV6_COMPRESSED_ADDRESS =
            "((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)";

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

    public static boolean isRangeValid(String ipAddressRange) {
        return checkAllPatterns(ipAddressRange, cidrPatterns);
    }
}
