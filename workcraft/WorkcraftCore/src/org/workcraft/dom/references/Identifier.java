package org.workcraft.dom.references;

import org.workcraft.dom.hierarchy.NamespaceHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Identifier {

    private static final String NAME_REGEX = "[_A-Za-z][_A-Za-z0-9]*";

    private static final String INTERNAL_PREFIX = "@";
    private static final String NAMESPACE_SUFFIX = NamespaceHelper.getHierarchySeparator();

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

    public static final String makeInternal(String value) {
        return value.startsWith(INTERNAL_PREFIX) ? value : INTERNAL_PREFIX + value;
    }

    public static final String appendNamespaceSeparator(String value) {
        return value.endsWith(NAMESPACE_SUFFIX) ? value : value + NAMESPACE_SUFFIX;
    }

    public static String truncateNamespaceSeparator(String value) {
        return value.endsWith(NAMESPACE_SUFFIX)
                ? value.substring(0, value.length() - NAMESPACE_SUFFIX.length())
                : value;
    }

    public static String compose(String prefix, String suffix) {
        return isNamespace(prefix)
                ? appendNamespaceSeparator(truncateNamespaceSeparator(prefix) + suffix)
                : prefix + suffix;
    }

}
