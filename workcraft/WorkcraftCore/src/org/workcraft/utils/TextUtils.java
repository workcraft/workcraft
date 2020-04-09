package org.workcraft.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextUtils {

    private static final String ELLIPSIS_SYMBOL = Character.toString((char) 0x2026);

    public static String truncateText(String text, int length) {
        StringBuffer result = new StringBuffer();
        boolean firstLine = true;
        for (String line : splitLines(text)) {
            if (firstLine) {
                firstLine = false;
            } else {
                result.append("\n");
            }
            result.append(truncateLine(line, length));
        }
        return result.toString();
    }

    public static String truncateLine(String line, int length) {
        if (line.length() <= length) {
            return line;
        }
        StringBuffer result = new StringBuffer();
        int curLength = 0;
        for (String word : splitWords(line)) {
            int wordLength = word.length();
            if (curLength > 0) {
                if (curLength + wordLength < length) {
                    result.append(" ");
                } else {
                    result.append(ELLIPSIS_SYMBOL);
                    break;
                }
            }
            result.append(word);
            curLength += wordLength;
        }
        return result.toString();
    }

    public static String wrapText(String text, int length) {
        StringBuffer result = new StringBuffer();
        boolean firstLine = true;
        for (String line : splitLines(text)) {
            if (firstLine) {
                firstLine = false;
            } else {
                result.append("\n");
            }
            result.append(wrapLine(line, length));
        }
        return result.toString();
    }

    public static String wrapLine(String line, int length) {
        if (line.length() <= length) {
            return line;
        }
        StringBuffer result = new StringBuffer();
        int curLength = 0;
        for (String word : splitWords(line)) {
            int wordLength = word.length();
            if (curLength > 0) {
                if (curLength + wordLength < length) {
                    result.append(" ");
                } else {
                    result.append("\n");
                    curLength = 0;
                }
            }
            result.append(word);
            curLength += wordLength;
        }
        return result.toString();
    }

    public static List<String> splitLines(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.split("\\r?\\n", -1));
    }

    public static List<String> splitWords(String text) {
        if ((text == null) || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.trim().split("\\s+", -1));
    }

    public static String getHeadAndTail(String text, int firstCount, int lastCount) {
        StringBuffer result = new StringBuffer();
        List<String> lines = splitLines(text);
        int index = 0;
        boolean dotsInserted = false;
        for (String line : lines) {
            if ((index < firstCount) || (index >= lines.size() - lastCount)) {
                if (index > 0) {
                    result.append("\n");
                }
                result.append(line);
            } else if (!dotsInserted) {
                result.append("\n");
                result.append(ELLIPSIS_SYMBOL);
                dotsInserted = true;
            }
            index++;
        }
        return result.toString();
    }

}
