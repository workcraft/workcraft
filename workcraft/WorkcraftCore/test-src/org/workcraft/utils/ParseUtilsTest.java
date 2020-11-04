package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

class ParseUtilsTest {

    private enum Enum { A, B, C, D }

    @Test
    void testParseInt() {
        Assertions.assertEquals(123, ParseUtils.parseInt(null, 123));
        Assertions.assertEquals(123, ParseUtils.parseInt("", 123));
        Assertions.assertEquals(123, ParseUtils.parseInt("abc", 123));
        Assertions.assertEquals(123, ParseUtils.parseInt("4.56", 123));
        Assertions.assertEquals(456, ParseUtils.parseInt("456", 123));
    }

    @Test
    void testParseDouble() {
        Assertions.assertEquals(1.23, ParseUtils.parseDouble(null, 1.23));
        Assertions.assertEquals(1.23, ParseUtils.parseDouble("", 1.23));
        Assertions.assertEquals(1.23, ParseUtils.parseDouble("abc", 1.23));
        Assertions.assertEquals(456, ParseUtils.parseDouble("456", 1.23));
        Assertions.assertEquals(4.56, ParseUtils.parseDouble("4.56", 1.23));
    }

    @Test
    void testParseBoolean() {
        Assertions.assertTrue(ParseUtils.parseBoolean(null, true));
        Assertions.assertFalse(ParseUtils.parseBoolean(null, false));

        Assertions.assertTrue(ParseUtils.parseBoolean("", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("", false));

        Assertions.assertTrue(ParseUtils.parseBoolean("yes", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("yes", false));

        Assertions.assertTrue(ParseUtils.parseBoolean("1", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("1", false));

        Assertions.assertFalse(ParseUtils.parseBoolean("true ", false));
        Assertions.assertTrue(ParseUtils.parseBoolean("true", false));
        Assertions.assertTrue(ParseUtils.parseBoolean("True", false));
        Assertions.assertTrue(ParseUtils.parseBoolean("TRUE", false));

        Assertions.assertTrue(ParseUtils.parseBoolean("false ", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("false", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("False", true));
        Assertions.assertFalse(ParseUtils.parseBoolean("FALSE", true));
    }

    @Test
    void testParseEnum() {
        Assertions.assertEquals(Enum.A, ParseUtils.parseEnum(null, Enum.class, Enum.A));
        Assertions.assertEquals(Enum.B, ParseUtils.parseEnum("", Enum.class, Enum.B));
        Assertions.assertEquals(Enum.C, ParseUtils.parseEnum("a", Enum.class, Enum.C));
        Assertions.assertEquals(Enum.D, ParseUtils.parseEnum("E", Enum.class, Enum.D));
        Assertions.assertEquals(Enum.A, ParseUtils.parseEnum("A", Enum.class, null));
    }

    @Test
    void testParseColor() {
        Assertions.assertEquals(Color.RED, ParseUtils.parseColor(null, Color.RED));
        Assertions.assertEquals(Color.GREEN, ParseUtils.parseColor("", Color.GREEN));
        Assertions.assertEquals(Color.BLUE, ParseUtils.parseColor("red", Color.BLUE));
        Assertions.assertNull(ParseUtils.parseColor("#abcdefg", null));
        Assertions.assertNull(ParseUtils.parseColor(" #12345678", null));

        Assertions.assertEquals(Color.RED, ParseUtils.parseColor("#ff0000", null));
        Assertions.assertEquals(new Color(0x00, 0xFF, 0x00, 0xFF), ParseUtils.parseColor("#ff00FF00", null));
        Assertions.assertEquals(new Color(0x44, 0x88, 0xFF, 0x22), ParseUtils.parseColor("#224488ff", null));
        Assertions.assertEquals(new Color(0x34, 0x56, 0x78, 0x12), ParseUtils.parseColor("#12345678", null));
    }

}