package org.workcraft.utils;

import java.awt.*;

public class ParseUtils {

    public static int parseInt(String s, int defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDouble(String s, double defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean parseBoolean(String s, boolean defaultValue) {
        if ("true".equalsIgnoreCase(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s)) {
            return false;
        }
        return defaultValue;
    }


    public static <T extends Enum<T>> T parseEnum(String s, Class<T> enumType, T defaultValue) {
        if (s == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumType, s);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static Color parseColor(String s, Color defaultValue) {
        if ((s == null) || !s.startsWith("#")) {
            return defaultValue;
        }
        try {
            int rgba = Integer.parseUnsignedInt(s.substring(1), 16);
            boolean hasAlpha = s.length() > 7;
            return new Color(rgba, hasAlpha);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
