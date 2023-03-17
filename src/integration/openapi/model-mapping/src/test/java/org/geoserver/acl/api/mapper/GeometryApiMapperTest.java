package org.geoserver.acl.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GeometryApiMapperTest {

    @BeforeEach
    void setUp() throws Exception {}

    @Test
    void testPattern() {
        Pattern pattern = GeometryApiMapper.pattern;

        final String test1 = "SRID=123456567 ;\t\t MULTIPOLYGON ( ( ( )))";
        final String test2 = "SRID=4326;             MULTIPOLYGON((()))";
        final String test3 = "\tMULTIPOLYGON ( ( ( )))   ";

        assertTrue(pattern.matcher(test1).matches());
        assertTrue(pattern.matcher(test2).matches());
        assertTrue(pattern.matcher(test3).matches());

        Matcher matcher = pattern.matcher(test1);
        assertEquals(4, matcher.groupCount());

        assertTrue(matcher.matches());
        assertEquals("SRID=123456567 ;", matcher.group(1));
        assertEquals("SRID=123456567", matcher.group(2));
        assertEquals("123456567", matcher.group(3));
        assertEquals("MULTIPOLYGON ( ( ( )))", matcher.group(4));

        matcher = pattern.matcher(test2);
        assertTrue(matcher.matches());
        assertEquals("SRID=4326;", matcher.group(1));
        assertEquals("SRID=4326", matcher.group(2));
        assertEquals("4326", matcher.group(3));
        assertEquals("MULTIPOLYGON((()))", matcher.group(4));

        matcher = pattern.matcher(test3);
        assertTrue(matcher.matches());
        assertNull(matcher.group(1));
        assertNull(matcher.group(2));
        assertNull(matcher.group(3));
        assertEquals("MULTIPOLYGON ( ( ( )))   ", matcher.group(4));

        assertFalse(pattern.matcher("  ;MULTIPOLYGON((()))").matches());
        assertFalse(pattern.matcher("4326;MULTIPOLYGON((()))").matches());
        assertFalse(pattern.matcher("SRID=4326;").matches());
    }
}
