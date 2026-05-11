/* (c) 2026  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.domain.filter.predicate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IPUtilsTest {

    @Test
    void isAddressValid_nullReturnsFalse() {
        assertThat(IPUtils.isAddressValid(null)).isFalse();
    }

    @Test
    void isAddressValid_emptyStringReturnsFalse() {
        assertThat(IPUtils.isAddressValid("")).isFalse();
    }

    @Test
    void isAddressValid_acceptsIPv4DottedQuad() {
        assertThat(IPUtils.isAddressValid("192.168.1.1")).isTrue();
        assertThat(IPUtils.isAddressValid("127.0.0.1")).isTrue();
        assertThat(IPUtils.isAddressValid("0.0.0.0")).isTrue();
        assertThat(IPUtils.isAddressValid("255.255.255.255")).isTrue();
    }

    @Test
    void isAddressValid_rejectsMalformedIPv4() {
        assertThat(IPUtils.isAddressValid("192.168.1")).isFalse();
        assertThat(IPUtils.isAddressValid("192.168.1.1.2")).isFalse();
        assertThat(IPUtils.isAddressValid("192.168.1.")).isFalse();
        assertThat(IPUtils.isAddressValid("a.b.c.d")).isFalse();
    }

    @Test
    void isAddressValid_acceptsIPv6StandardForm() {
        assertThat(IPUtils.isAddressValid("0:0:0:0:0:0:0:1")).isTrue();
        assertThat(IPUtils.isAddressValid("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
                .isTrue();
        assertThat(IPUtils.isAddressValid("B012:a000:361:44:f87:11:0:0")).isTrue();
    }

    @Test
    void isAddressValid_rejectsMalformedIPv6Standard() {
        assertThat(IPUtils.isAddressValid("B012:a000:361:44:f87:11:0:g0")).isFalse();
        assertThat(IPUtils.isAddressValid("B012:a000:361:44:f87:11:0")).isFalse();
    }

    @Test
    void isAddressValid_acceptsIPv6CompressedForm() {
        assertThat(IPUtils.isAddressValid("::1")).isTrue();
        assertThat(IPUtils.isAddressValid("::")).isTrue();
        assertThat(IPUtils.isAddressValid("fe80::")).isTrue();
        assertThat(IPUtils.isAddressValid("2001:db8::1")).isTrue();
        assertThat(IPUtils.isAddressValid("2001:db8::8a2e:370:7334")).isTrue();
    }

    @Test
    void isAddressValid_rejectsMalformedIPv6Compressed() {
        assertThat(IPUtils.isAddressValid(":")).isFalse();
        assertThat(IPUtils.isAddressValid(":::")).isFalse();
        assertThat(IPUtils.isAddressValid("2001:db8:::1")).isFalse();
        assertThat(IPUtils.isAddressValid("g::1")).isFalse();
    }

    @Test
    void isAddressValid_pathologicalInputFailsFast() {
        String longHexSequence = "aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaa:aaaaX";
        long start = System.nanoTime();
        boolean result = IPUtils.isAddressValid(longHexSequence);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertThat(result).isFalse();
        assertThat(elapsedMillis)
                .as("possessive quantifiers must prevent catastrophic backtracking")
                .isLessThan(1_000);
    }

    @Test
    void isRangeValid_nullReturnsFalse() {
        assertThat(IPUtils.isRangeValid(null)).isFalse();
    }

    @Test
    void isRangeValid_emptyStringReturnsFalse() {
        assertThat(IPUtils.isRangeValid("")).isFalse();
    }

    @Test
    void isRangeValid_requiresSlashSuffix() {
        assertThat(IPUtils.isRangeValid("192.168.1.1")).isFalse();
        assertThat(IPUtils.isRangeValid("::1")).isFalse();
    }

    @Test
    void isRangeValid_acceptsIPv4Cidr() {
        assertThat(IPUtils.isRangeValid("192.168.1.0/32")).isTrue();
        assertThat(IPUtils.isRangeValid("127.0.0.1/8")).isTrue();
        assertThat(IPUtils.isRangeValid("10.0.0.0/0")).isTrue();
    }

    @Test
    void isRangeValid_rejectsMalformedIPv4Cidr() {
        assertThat(IPUtils.isRangeValid("127.0.0.1/32/2")).isFalse();
        assertThat(IPUtils.isRangeValid("127.0.0.1/")).isFalse();
        assertThat(IPUtils.isRangeValid("127.0.0.1/a")).isFalse();
    }

    @Test
    void isRangeValid_acceptsIPv6Cidr() {
        assertThat(IPUtils.isRangeValid("0:0:0:0:0:0:0:1/32")).isTrue();
        assertThat(IPUtils.isRangeValid("2001:db8::/32")).isTrue();
        assertThat(IPUtils.isRangeValid("::1/128")).isTrue();
        assertThat(IPUtils.isRangeValid("fe80::/10")).isTrue();
    }

    @Test
    void isRangeValid_rejectsMalformedIPv6Cidr() {
        assertThat(IPUtils.isRangeValid("0:0:0:0:0:0:0:1/32/1")).isFalse();
        assertThat(IPUtils.isRangeValid("2001:db8::/")).isFalse();
        assertThat(IPUtils.isRangeValid("2001:db8::/g")).isFalse();
    }
}
