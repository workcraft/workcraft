package org.workcraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Identifier {
	private static final Pattern identifierPattern = Pattern.compile("[_A-Za-z][_A-Za-z0-9]*");

	public static boolean isValid (String s) {
		final Matcher matcher = identifierPattern.matcher(s);
		if (!matcher.find()) return false;
		return matcher.end() == s.length();
	}
}
