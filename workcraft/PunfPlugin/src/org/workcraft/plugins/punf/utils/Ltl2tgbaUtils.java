package org.workcraft.plugins.punf.utils;

import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutput;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ltl2tgbaUtils {

    private static final Pattern STUTTER_EXAMPLE_PATTERN = Pattern.compile(
            "spot-accepted-word: \"(.*)\"\\Rspot-rejected-word: \"(.*)\"",
            Pattern.UNIX_LINES);

    public static Pair<String, String> extraxtStutterExample(Ltl2tgbaOutput output) throws IOException {
        String text = FileUtils.readAllText(output.getOutputFile());
        return extraxtStutterExample(text);
    }

    public static Pair<String, String> extraxtStutterExample(String text) {
        Matcher matcher = STUTTER_EXAMPLE_PATTERN.matcher(text);
        if (matcher.find()) {
            String acceptedWord = matcher.group(1);
            String rejectedWord = matcher.group(2);
            return Pair.of(acceptedWord, rejectedWord);
        }
        return null;
    }

}
