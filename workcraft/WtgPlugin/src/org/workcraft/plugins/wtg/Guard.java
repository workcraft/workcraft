package org.workcraft.plugins.wtg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Guard extends LinkedHashMap<String, Boolean> {

    private static final Pattern LITERAL_PATTERN = Pattern.compile("^(!?)(\\w+)$");

    @Override
    public String toString() {
        String result = "";
        for (Map.Entry<String, Boolean> entry : entrySet()) {
            if (!result.isEmpty()) {
                result += ", ";
            }
            if (!entry.getValue()) {
                result +=  "!";
            }
            result +=  entry.getKey();
        }
        return result;
    }

    public static Guard createFromString(String str) {
        Guard result = new Guard();
        if ((str != null) && !str.isEmpty()) {
            for (String literal : str.replaceAll("\\s", "").split(",")) {
                Matcher matcher = LITERAL_PATTERN.matcher(literal);
                if (matcher.find()) {
                    result.put(matcher.group(2), matcher.group(1).isEmpty());
                } else {
                    throw new RuntimeException("Unrecognised literal '" + literal + "'");
                }
            }
        }
        return result;
    }

}
