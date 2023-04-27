package org.workcraft.plugins.circuit.utils;

import org.workcraft.types.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InitMappingRules {

    private static final String RULE_ARROW = "->";
    private static final String RULE_SEPARATOR = ",";
    private static final Pattern RULE_PATTERN = Pattern.compile("^(\\w+)" + RULE_ARROW + "(\\w+)\\((\\w+)\\)$");
    private static final int ORIGINAL_GROUP = 1;
    private static final int REPLACEMENT_GROUP = 2;
    private static final int PIN_GROUP = 3;

    public static Map<String, Pair<String, String>> parse(String value) {
        Map<String, Pair<String, String>> result = new LinkedHashMap<>();
        for (String initRule : value.split(RULE_SEPARATOR)) {
            Matcher matcher = RULE_PATTERN.matcher(initRule.replaceAll("\\s", ""));
            if (matcher.find()) {
                String originalName = matcher.group(ORIGINAL_GROUP);
                String replacementName = matcher.group(REPLACEMENT_GROUP);
                String initPinName = matcher.group(PIN_GROUP);
                result.put(originalName, Pair.of(replacementName, initPinName));
            }
        }
        return result;
    }

    public static String compose(Map<String, Pair<String, String>> initGateRules) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Pair<String, String>> entry : initGateRules.entrySet()) {
            String originalGateName = entry.getKey();
            Pair<String, String> gatePinPair = entry.getValue();
            if (gatePinPair != null) {
                if (result.length() > 0) {
                    result.append(RULE_SEPARATOR).append(' ');
                }
                String replacementGateName = gatePinPair.getFirst();
                String initPinName = gatePinPair.getSecond();
                result.append(originalGateName).append(RULE_ARROW).append(replacementGateName)
                        .append("(").append(initPinName).append(")");
            }
        }
        return result.toString();
    }

    public static String sanitise(String value) {
        return compose(parse(value));
    }

}
