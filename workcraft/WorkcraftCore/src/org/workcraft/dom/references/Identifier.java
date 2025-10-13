package org.workcraft.dom.references;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.ArgumentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Identifier {

    private static final String INTERNAL_PREFIX = "@";
    private static final String NAMESPACE_SUFFIX = NamespaceHelper.getHierarchySeparator();
    private static final Pattern VALID_PATTERN = Pattern.compile("^[_A-Za-z][_A-Za-z0-9]*$");

    public static boolean isNamespace(String value) {
        return value.endsWith(NAMESPACE_SUFFIX);
    }

    public static boolean isValid(String value) {
        if (value == null) return false;
        final Matcher matcher = VALID_PATTERN.matcher(value);
        return matcher.find();
    }

    public static void validate(String value) {
        if (!isValid(value)) {
            throw new ArgumentException("'" + value + "' is not a valid C-style identifier.\n"
                    + "The first character must be alphabetic or '_' and the following -- alphanumeric or '_'.");
        }
    }

    public static boolean hasInternalPrefix(String value) {
        return value.startsWith(INTERNAL_PREFIX);
    }

    public static String addInternalPrefix(String value) {
        return value.startsWith(INTERNAL_PREFIX) ? value : INTERNAL_PREFIX + value;
    }

    public static String removeInternalPrefix(String value) {
        return value.startsWith(INTERNAL_PREFIX) ? value.substring(INTERNAL_PREFIX.length()) : value;
    }

    public static String getTemporaryName() {
        return INTERNAL_PREFIX;
    }

    public static String appendNamespaceSeparator(String value) {
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
