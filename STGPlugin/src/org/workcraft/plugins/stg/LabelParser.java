package org.workcraft.plugins.stg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Pair;
import org.workcraft.util.Triple;

public class LabelParser {
	private static final Pattern signalTransitionPattern =
			Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*)([\\+\\-\\~])(\\/([0-9]+))?");

	private static final Pattern dummyTransitionPattern =
			Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*)(\\/([0-9]+))?");

	private static final Pattern instancedTransitionPattern =
			Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*[\\+\\-\\~]?)(\\/([0-9]+))?");

	public static Pair<String, String> parseImplicitPlaceReference(String ref) {
		String[] parts = ref.replaceAll(" ", "").split(",");
		if (parts.length < 2 || !parts[0].startsWith("<") || !parts[1].endsWith(">")) {
			return null;
		}
		return Pair.of(parts[0].substring(1), parts[1].substring(0, parts[1].length()-1));
	}

	public static Triple<String, SignalTransition.Direction, Integer> parseSignalTransition(String s) {
		final Matcher matcher = signalTransitionPattern.matcher(s);
		if (!matcher.find()) {
			throw new ArgumentException("\"" + s + "\" is not a valid STG label.");
		}

		if (! (matcher.end() == s.length())) {
			throw new ArgumentException("\"" + s + "\" is not a valid STG label.");
		}

		final String instanceGroup = matcher.group(4);

		final Direction second;

		if (matcher.group(2).equals("+")) {
			second = SignalTransition.Direction.PLUS;
		} else if (matcher.group(2).equals("-")) {
			second = SignalTransition.Direction.MINUS;
		} else {
			second = SignalTransition.Direction.TOGGLE;
		}
		return Triple.of(matcher.group(1),	second,
				instanceGroup == null ? null : Integer.parseInt(instanceGroup));
	}

	public static Pair<String, Integer> parseDummyTransition(String s) {
		final Matcher matcher = dummyTransitionPattern.matcher(s);

		if (!matcher.find()) {
			return null;
		}
		if (! (matcher.end() == s.length())) {
			return null;
		}
		final String instanceGroup = matcher.group(3);

		return Pair.of(matcher.group(1),
				instanceGroup == null? null : Integer.parseInt(instanceGroup));
	}

	public static Pair<String, Integer> parseInstancedTransition(String s) {
		final Matcher matcher = instancedTransitionPattern.matcher(s);

		if (!matcher.find()) {
			return null;
		}
		if (! (matcher.end() == s.length())) {
			return null;
		}
		final String instanceGroup = matcher.group(3);

		return Pair.of(matcher.group(1),
				instanceGroup == null? null : Integer.parseInt(instanceGroup));
	}

}
