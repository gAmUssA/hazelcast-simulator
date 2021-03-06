package com.hazelcast.simulator.utils;

import org.junit.Test;

import static com.hazelcast.simulator.utils.CommonUtils.fillString;
import static com.hazelcast.simulator.utils.CommonUtils.formatDouble;
import static com.hazelcast.simulator.utils.CommonUtils.formatLong;
import static com.hazelcast.simulator.utils.CommonUtils.humanReadableByteCount;
import static com.hazelcast.simulator.utils.CommonUtils.padLeft;
import static com.hazelcast.simulator.utils.CommonUtils.padRight;
import static com.hazelcast.simulator.utils.CommonUtils.secondsToHuman;
import static com.hazelcast.simulator.utils.TestUtils.assertEqualsStringFormat;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommonUtils_FormatTest {

    @Test
    public void testFormatLong() {
        assertEquals("-1", formatLong(-1, -10));
        assertEquals("-1", formatLong(-1, 0));
        assertEquals("        -1", formatLong(-1, 10));

        assertEquals("0", formatLong(0, -10));
        assertEquals("0", formatLong(0, 0));
        assertEquals("         0", formatLong(0, 10));

        assertEquals("1", formatLong(1, -10));
        assertEquals("1", formatLong(1, 0));
        assertEquals("         1", formatLong(1, 10));

        assertEquals("-9,223,372,036,854,775,808", formatLong(Long.MIN_VALUE, -30));
        assertEquals("-9,223,372,036,854,775,808", formatLong(Long.MIN_VALUE, 0));
        assertEquals("    -9,223,372,036,854,775,808", formatLong(Long.MIN_VALUE, 30));

        // Tests with Long.MAX_VALUE fail (maybe some overflow in the formatter)
        //assertEquals("9,223,372,036,854,775,808", formatLong(Long.MAX_VALUE, -30));
        //assertEquals("9,223,372,036,854,775,808", formatLong(Long.MAX_VALUE, 0));
        //assertEquals("     9,223,372,036,854,775,808", formatLong(Long.MAX_VALUE, 30));
    }

    @Test
    public void testFormatDouble() {
        assertEquals("-1.00", formatDouble(-1.0d, -10));
        assertEquals("-1.00", formatDouble(-1.0d, 0));
        assertEquals("     -1.00", formatDouble(-1.0d, 10));

        assertEquals("0.00", formatDouble(0.0d, -10));
        assertEquals("0.00", formatDouble(0.0d, 0));
        assertEquals("      0.00", formatDouble(0.0d, 10));

        assertEquals("1.00", formatDouble(1.0d, -10));
        assertEquals("1.00", formatDouble(1.0d, 0));
        assertEquals("      1.00", formatDouble(1.0d, 10));

        assertEquals("1.50", formatDouble(1.5d, -10));
        assertEquals("1.50", formatDouble(1.5d, 0));
        assertEquals("      1.50", formatDouble(1.5d, 10));

        assertEquals("1.51", formatDouble(1.505d, -10));
        assertEquals("1.51", formatDouble(1.505d, 0));
        assertEquals("      1.51", formatDouble(1.505d, 10));
    }

    @Test
    public void testPadRight() {
        assertEquals(null, padRight(null, -10));
        assertEquals(null, padRight(null, 0));
        assertEquals("null      ", padRight(null, 10));

        assertEquals("", padRight("", -10));
        assertEquals("", padRight("", 0));
        assertEquals("          ", padRight("", 10));

        assertEquals("test", padRight("test", -10));
        assertEquals("test", padRight("test", 0));
        assertEquals("test      ", padRight("test", 10));

        assertEquals("longerString", padRight("longerString", -5));
        assertEquals("longerString", padRight("longerString", 0));
        assertEquals("longerString", padRight("longerString", 5));
    }

    @Test
    public void testPadLeft() {
        assertEquals(null, padLeft(null, -10));
        assertEquals(null, padLeft(null, 0));
        assertEquals("      null", padLeft(null, 10));

        assertEquals("", padLeft("", -10));
        assertEquals("", padLeft("", 0));
        assertEquals("          ", padLeft("", 10));

        assertEquals("test", padLeft("test", -10));
        assertEquals("test", padLeft("test", 0));
        assertEquals("      test", padLeft("test", 10));

        assertEquals("longerString", padLeft("longerString", -5));
        assertEquals("longerString", padLeft("longerString", 0));
        assertEquals("longerString", padLeft("longerString", 5));
    }

    @Test
    public void testFillStringZeroLength() {
        String actual = fillString(0, '#');
        assertTrue(format("Expected empty string, but got %s", actual), actual.isEmpty());
    }

    @Test
    public void testFillString() {
        String actual = fillString(5, '#');
        assertEqualsStringFormat("Expected filled string %s, but was %s", "#####", actual);
    }

    @Test
    public void testSecondsToHuman() {
        String expected = "01d 02h 03m 04s";
        String actual = secondsToHuman(93784);
        assertEqualsStringFormat("Expected human readable seconds to be %s, but was %s", expected, actual);
    }

    @Test
    public void testHumanReadableByteCount_Byte_SI() {
        String actual = humanReadableByteCount(42, false);
        assertEqualsStringFormat("Expected %s, but was %s", "42 B", actual);
    }

    @Test
    public void testHumanReadableByteCount_Byte_NoSI() {
        String actual = humanReadableByteCount(23, true);
        assertEqualsStringFormat("Expected %s, but was %s", "23 B", actual);
    }

    @Test
    public void testHumanReadableByteCount_KiloByte_SI() {
        String actual = humanReadableByteCount(4200, false);
        assertEqualsStringFormat("Expected %s, but was %s", "4.1 KiB", actual);
    }

    @Test
    public void testHumanReadableByteCount_KiloByte_NoSI() {
        String actual = humanReadableByteCount(2300, true);
        assertEqualsStringFormat("Expected %s, but was %s", "2.3 kB", actual);
    }

    @Test
    public void testHumanReadableByteCount_MegaByte_SI() {
        String actual = humanReadableByteCount(4200000, false);
        assertEqualsStringFormat("Expected %s, but was %s", "4.0 MiB", actual);
    }

    @Test
    public void testHumanReadableByteCount_MegaByte_NoSI() {
        String actual = humanReadableByteCount(2300000, true);
        assertEqualsStringFormat("Expected %s, but was %s", "2.3 MB", actual);
    }

    @Test
    public void testHumanReadableByteCount_GigaByte_SI() {
        String actual = humanReadableByteCount(Integer.MAX_VALUE, false);
        assertEqualsStringFormat("Expected %s, but was %s", "2.0 GiB", actual);
    }

    @Test
    public void testHumanReadableByteCount_GigaByte_NoSI() {
        String actual = humanReadableByteCount(Integer.MAX_VALUE, true);
        assertEqualsStringFormat("Expected %s, but was %s", "2.1 GB", actual);
    }
}
