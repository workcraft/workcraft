package org.workcraft.dom.references;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Identifier {

    private static final String NAME_REGEX = "[_A-Za-z][_A-Za-z0-9]*";

    private static final String INTERNAL_PREFIX = "@";
    private static final String NAMESPACE_SUFFIX = ":";

    private static final Pattern NAME_PATTERN = Pattern.compile("^" + NAME_REGEX + "$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    public static boolean isInternal(String value) {
        return value.startsWith(INTERNAL_PREFIX);
    }

    public static boolean isNamespace(String value) {
        return value.endsWith(NAMESPACE_SUFFIX);
    }

    public static boolean isName(String value) {
        if (value == null) return false;
        final Matcher matcher = NAME_PATTERN.matcher(value);
        return matcher.find();
    }

    public static boolean isNumber(String value) {
        if (value == null) return false;
        final Matcher matcher = NUMBER_PATTERN.matcher(value);
        return matcher.find();
    }

    public static final String createInternal(String value) {
        return value.startsWith(INTERNAL_PREFIX) ? value : INTERNAL_PREFIX + value;
    }

    public static final String createNamespace(String value) {
        return value.endsWith(NAMESPACE_SUFFIX) ? value : value + NAMESPACE_SUFFIX;
    }

    public static String createName(String pattern, Integer count) {
        if (isNamespace(pattern)) {
            return createNamespace(pattern.substring(0, pattern.lastIndexOf(NAMESPACE_SUFFIX)) + count);
        }
        return pattern + count;
    }

}
