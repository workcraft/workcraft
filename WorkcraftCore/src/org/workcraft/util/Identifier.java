package org.workcraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.dom.hierarchy.NamespaceHelper;

public class Identifier {
    private static final Pattern identifierPattern = Pattern.compile("[_A-Za-z][_A-Za-z0-9]*");

    public static boolean isValid (String s) {
        // disallow starting with __ and var_ to avoid possible further problems
//        if (s.contains(HierarchicalNames.flatNameSeparator)) return false;
        if (s.startsWith("var_")) return false;

        final Matcher matcher = identifierPattern.matcher(s);
        return (matcher.find() && matcher.start() == 0 && matcher.end() == s.length());
    }

    private static final Pattern numberPattern = Pattern.compile("[0-9]*");
    public static boolean isNumber (String s) {
        if (s==null) return false;
        final Matcher matcher = numberPattern.matcher(s);
        return (matcher.find() && matcher.start() == 0 && matcher.end() == s.length());
    }
}
