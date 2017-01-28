package org.workcraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Identifier {

    private static final String INTENAL_PREFIX = "@";
    private static final Pattern INTERNAL_PATTERN = Pattern.compile("^" + INTENAL_PREFIX + "[_A-Za-z][_A-Za-z0-9]*$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[_A-Za-z][_A-Za-z0-9]*$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    public static boolean isInternal(String s) {
        final Matcher matcher = INTERNAL_PATTERN.matcher(s);
        return matcher.find();
    }

    public static boolean isName(String s) {
        final Matcher matcher = NAME_PATTERN.matcher(s);
        return matcher.find();
    }

    public static boolean isNumber(String s) {
        if (s == null) return false;
        final Matcher matcher = NUMBER_PATTERN.matcher(s);
        return matcher.find();
    }

    public static String createInternal(String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        return INTENAL_PREFIX + suffix;
    }

}
