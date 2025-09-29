/* (c) 2023  Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.acl.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.stream.IntStream;
import javax.validation.constraints.Pattern;
import org.junit.jupiter.api.Test;

class RuleTest {

    @Test
    void testAddressRangeRegEx_invalid_values() throws Exception {
        assertInvalidRange("192.168.0");
        assertInvalidRange("192.168.0.1/-1");
        assertInvalidRange("192.168.0.1/");
        assertInvalidRange("192.168.0.1/33");
        assertInvalidRange("192.168.0.1/asd");
        assertInvalidRange("192.168.0.1/01");
        assertInvalidRange("192.168.0.1/00");
    }

    @Test
    void testAddressRangeRegEx_valid_expressions() throws Exception {
        testAddressRangeRegEx("10.0.0.1", true);
        testAddressRangeRegEx("192.168.0.1", true);
        IntStream.rangeClosed(1, 32).mapToObj(i -> "192.168.0.1/" + i).forEach(this::assertValidRange);
    }

    private void assertInvalidRange(String range) {
        testAddressRangeRegEx(range, false);
    }

    private void assertValidRange(String range) {
        testAddressRangeRegEx(range, true);
    }

    /**
     * Validates the regex in {@link Rule#getAddressRange()} {@link Pattern @Pattern} annotation,
     * not whether hibernate-validator itself works
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private void testAddressRangeRegEx(String range, boolean validates) {

        Method getAddressRange;
        try {
            getAddressRange = Rule.class.getMethod("getAddressRange");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        javax.validation.constraints.Pattern[] annotations =
                getAddressRange.getAnnotationsByType(javax.validation.constraints.Pattern.class);

        final String regexp = annotations[0].regexp();
        assertEquals(range.matches(regexp), validates);
    }
}
