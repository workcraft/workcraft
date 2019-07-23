package org.workcraft.plugins.stg;

import org.workcraft.types.Pair;
import org.workcraft.types.Triple;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelParser {

    private static final Pattern signalTransitionPattern =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*)([\\+\\-\\~])?(\\/([0-9]+))?");

    private static final Pattern dummyTransitionPattern =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*)(\\/([0-9]+))?");

    private static final Pattern instancedTransitionPattern =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9]*[\\+\\-\\~]?)(\\/([0-9]+))?");

    public static Pair<String, String> parseImplicitPlaceReference(String ref) {
        String[] parts = ref.replace(" ", "").split(",");
        if (parts.length < 2 || !parts[0].startsWith("<") || !parts[1].endsWith(">")) {
            return null;
        }
        return Pair.of(parts[0].substring(1), parts[1].substring(0, parts[1].length() - 1));
    }

    public static Triple<String, SignalTransition.Direction, Integer> parseSignalTransition(String s) {
        Triple<String, SignalTransition.Direction, Integer> result = null;
        final Matcher matcher = signalTransitionPattern.matcher(s);
        if (matcher.find() && (matcher.end() == s.length())) {
            final String signalName = matcher.group(1);

            final SignalTransition.Direction direction;
            String directionGroup = matcher.group(2);
            if (directionGroup == null) {
                direction = SignalTransition.Direction.TOGGLE;
            } else {
                if (directionGroup.equals("+")) {
                    direction = SignalTransition.Direction.PLUS;
                } else if (directionGroup.equals("-")) {
                    direction = SignalTransition.Direction.MINUS;
                } else {
                    direction = SignalTransition.Direction.TOGGLE;
                }
            }

            final Integer instance;
            String instanceGroup = matcher.group(4);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Triple.of(signalName, direction, instance);
        }
        return result;
    }

    public static Pair<String, Integer> parseDummyTransition(String s) {
        Pair<String, Integer> result = null;
        final Matcher matcher = dummyTransitionPattern.matcher(s);
        if (matcher.find() && (matcher.end() == s.length())) {
            final String name = matcher.group(1);

            final Integer instance;
            String instanceGroup = matcher.group(3);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Pair.of(name, instance);
        }
        return result;
    }
            // FIXME: This is to rename toggle events from x to x~

    public static Pair<String, Integer> parseInstancedTransition(String s) {
        Pair<String, Integer> result = null;
        final Matcher matcher = instancedTransitionPattern.matcher(s);
        if (matcher.find() && (matcher.end() == s.length())) {
            final String name = matcher.group(1);
            final Integer instance;
            String instanceGroup = matcher.group(3);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Pair.of(name, instance);
        }
        return result;
    }

    public static String getTransitionName(String s) {
        String result = null;
        Pair<String, Integer> instancedTransition = LabelParser.parseInstancedTransition(s);
        if (instancedTransition != null) {
            result = instancedTransition.getFirst();
        }
        return result;
    }

    public static Integer getTransitionInstance(String s) {
        Integer result = null;
        Pair<String, Integer> instancedTransition = LabelParser.parseInstancedTransition(s);
        if (instancedTransition != null) {
            result = instancedTransition.getSecond();
        }
        return result;
    }

}
