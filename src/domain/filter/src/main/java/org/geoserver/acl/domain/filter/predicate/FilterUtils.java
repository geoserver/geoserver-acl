/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 *
 * Original from GeoFence 3.6 under GPL 2.0 license
 */

package org.geoserver.acl.domain.filter.predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Emanuele Tajariol (etj at geo-solutions.it) (originally as part of GeoFence)
 */
@Slf4j
class FilterUtils {

    /**
     * Filters out rules not matching with ip address filter.
     *
     * <p>IP address filtering is not performed by DAO at the moment, so we'll have to filter out
     * such results by hand.
     */
    public static boolean filterByAddress(
            IPAddressRangeFilter addressFilter, String addressRangeCIDR) {

        final FilterType type = addressFilter.getType();

        switch (type) {
            case ANY:
                return true;
            case DEFAULT:
                return null == addressRangeCIDR;
            case NAMEVALUE:
                return nameValueFilter(addressFilter, addressRangeCIDR);
            case IDVALUE:
            default:
                throw new IllegalArgumentException("Bad address filter type" + type);
        }
    }

    private static boolean nameValueFilter(IPAddressRangeFilter filter, String addressRangeCIDR) {

        final String ipvalue = filter.getAddress();
        if (!IPUtils.isAddressValid(ipvalue)) {
            log.warn("Bad address filter " + ipvalue);
            return false;
        }

        if (filter.isIncludeDefault()) {
            return null == addressRangeCIDR || matches(ipvalue, addressRangeCIDR);
        }

        return null != addressRangeCIDR && matches(ipvalue, addressRangeCIDR);
    }

    public static boolean matches(String address, String addressRange) {
        if (!SubnetV4Utils.isAddress(address)) return false;

        SubnetV4Utils su = new SubnetV4Utils(addressRange);
        return su.getInfo().isInRange(address);
    }
}
