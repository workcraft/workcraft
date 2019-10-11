package org.workcraft.plugins.xbm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Conditional extends LinkedHashMap<String, Boolean> {

    private static final Pattern LITERAL_PATTERN = Pattern.compile("^(\\w+)(=(0|1))$"); //Only include valid C syntax names

    public void setConditional(String str) {
        this.clear();
        Conditional result = new Conditional();
        if ((str != null) && !str.isEmpty()) {
            for (String literal: str.replaceAll("\\s", "").split(",")) {
                Matcher matcher  = LITERAL_PATTERN.matcher(literal);
                if (matcher.find()) {
                    String name = literal.split("=")[0];
                    String value = literal.split("=")[1];
                    if (!result.containsKey(name)) {
                        switch (value) {
                        case "0":
                            result.put(name, false);
                            break;
                        case "1":
                            result.put(name, true);
                            break;
                        default:
                            throw new RuntimeException("The literal \'" + str + "\' unknowningly passed.");
                        }
                    }
                } else {
                    throw new RuntimeException("The literal \'" + str + "\' is not valid. ");
                }
            }
        }
        this.putAll(result);
    }

    @Override
    public String toString() {
        String result = "";
        for (Map.Entry<String, Boolean> e: this.entrySet()) {
            final String signalName = e.getKey();
            if (!result.isEmpty()) {
                result += ", ";
            }
            if (e.getValue()) {
                result += signalName + "=1";
            } else {
                result += signalName + "=0";
            }
        }
        return result;
    }
}