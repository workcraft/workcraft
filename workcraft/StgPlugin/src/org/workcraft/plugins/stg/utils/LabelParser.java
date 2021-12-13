package org.workcraft.plugins.stg.utils;

import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LabelParser {

    private static final int SIGNAL_TRANSITION_NAME_GROUP = 1;
    private static final int SIGNAL_TRANSITION_SIGN_GROUP = 2;
    private static final int SIGNAL_TRANSITION_INSTANCE_GROUP = 4;
    private static final Pattern SIGNAL_TRANSITION_PATTERN =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9.]*)([+\\-~])?(/([0-9]+))?");

    private static final int DUMMY_TRANSITION_NAME_GROUP = 1;
    private static final int DUMMY_TRANSITION_INSTANCE_GROUP = 3;
    private static final Pattern DUMMY_TRANSITION_PATTERN =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9.]*)(/([0-9]+))?");

    private static final int INSTANCED_TRANSITION_NAME_GROUP = 1;
    private static final int INSTANCED_TRANSITION_INSTANCE_GROUP = 3;
    private static final Pattern INSTANCED_TRANSITION_PATTERN =
            Pattern.compile("^([_A-Za-z][_A-Za-z0-9.]*[+\\-~]?)(/([0-9]+))?");

    public static Pair<String, String> parseImplicitPlace(String ref) {
        String[] parts = ref.replace(" ", "").split(",");
        return parts.length < 2 || !parts[0].startsWith("<") || !parts[1].endsWith(">") ? null
                : Pair.of(parts[0].substring(1), parts[1].substring(0, parts[1].length() - 1));
    }

    public static Triple<String, SignalTransition.Direction, Integer> parseSignalTransition(String ref) {
        Triple<String, SignalTransition.Direction, Integer> result = null;
        final Matcher matcher = SIGNAL_TRANSITION_PATTERN.matcher(ref);
        if (matcher.find() && (matcher.end() == ref.length())) {
            final String signalName = matcher.group(SIGNAL_TRANSITION_NAME_GROUP);

            final SignalTransition.Direction direction;
            String directionGroup = matcher.group(SIGNAL_TRANSITION_SIGN_GROUP);
            if (directionGroup == null) {
                direction = SignalTransition.Direction.TOGGLE;
            } else {
                if ("+".equals(directionGroup)) {
                    direction = SignalTransition.Direction.PLUS;
                } else if ("-".equals(directionGroup)) {
                    direction = SignalTransition.Direction.MINUS;
                } else {
                    direction = SignalTransition.Direction.TOGGLE;
                }
            }

            final Integer instance;
            String instanceGroup = matcher.group(SIGNAL_TRANSITION_INSTANCE_GROUP);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Triple.of(signalName, direction, instance);
        }
        return result;
    }

    public static Pair<String, Integer> parseDummyTransition(String ref) {
        Pair<String, Integer> result = null;
        final Matcher matcher = DUMMY_TRANSITION_PATTERN.matcher(ref);
        if (matcher.find() && (matcher.end() == ref.length())) {
            final String name = matcher.group(DUMMY_TRANSITION_NAME_GROUP);

            final Integer instance;
            String instanceGroup = matcher.group(DUMMY_TRANSITION_INSTANCE_GROUP);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Pair.of(name, instance);
        }
        return result;
    }

    public static Pair<String, Integer> parseInstancedTransition(String ref) {
        Pair<String, Integer> result = null;
        final Matcher matcher = INSTANCED_TRANSITION_PATTERN.matcher(ref);
        if (matcher.find() && (matcher.end() == ref.length())) {
            final String name = matcher.group(INSTANCED_TRANSITION_NAME_GROUP);
            final Integer instance;
            String instanceGroup = matcher.group(INSTANCED_TRANSITION_INSTANCE_GROUP);
            if (instanceGroup == null) {
                instance = null;
            } else {
                instance = Integer.parseInt(instanceGroup);
            }
            result = Pair.of(name, instance);
        }
        return result;
    }

    public static String getSignalTransitionReference(Triple<String, SignalTransition.Direction, Integer> r) {
        String eventRef = r.getFirst() + r.getSecond();
        return r.getThird() == null ? eventRef : getInstancedTransitionReference(eventRef, r.getThird());
    }

    public static String getInstancedTransitionReference(String transitionRef, int instance) {
        return transitionRef + "/" + instance;
    }

    public static String getImplicitPlaceReference(String predTransitionRef, String succTransitionRef) {
        return "<" + predTransitionRef + "," + succTransitionRef + ">";
    }

}
