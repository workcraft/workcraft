package org.workcraft.utils;

import org.workcraft.gui.properties.PropertyHelper;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextUtils {

    public static final int DEFAULT_WRAP_LENGTH = 120;
    public static final int DEFAULT_TRUNCATE_LENGTH = 120;

    private static final String ELLIPSIS_SYMBOL = Character.toString((char) 0x2026);
    private static final String NEWLINE_REGEX = "\\r?\\n";
    private static final Pattern LEADING_SPACES_PATTERN = Pattern.compile("^\\s+");
    private static final Pattern XML_ELEMENT_PATTERN = Pattern.compile(
            "^\\s*<([\\w-.]+).*(</\\1\\s*|/)>\\s*$", Pattern.DOTALL);

    public static String truncateLines(String text) {
        return truncateLines(text, DEFAULT_TRUNCATE_LENGTH);
    }

    public static String truncateLines(String text, int length) {
        StringBuilder result = new StringBuilder();
        boolean firstLine = true;
        for (String line : splitLines(text)) {
            if (firstLine) {
                firstLine = false;
            } else {
                result.append('\n');
            }
            result.append(truncateLine(line, length));
        }
        return result.toString();
    }

    public static String truncateLine(String line) {
        return truncateLine(line, DEFAULT_TRUNCATE_LENGTH);
    }

    public static String truncateLine(String line, int length) {
        if (line == null) {
            return "";
        }
        if (line.length() <= length) {
            return line;
        }
        StringBuilder result = new StringBuilder();
        for (String word : splitWords(line)) {
            int spaceCount = 1;
            if (result.length() == 0) {
                spaceCount = countLeadingSpaces(line);
            }
            if (result.length() + spaceCount < length) {
                result.append(repeat(" ", spaceCount));
                result.append(word);
            } else {
                result.append(ELLIPSIS_SYMBOL);
                break;
            }
        }
        return result.toString();
    }

    public static String wrapText(String text) {
        return wrapText(text, DEFAULT_WRAP_LENGTH);
    }

    public static String wrapText(String text, int length) {
        StringBuilder result = new StringBuilder();
        boolean firstLine = true;
        for (String line : splitLines(text)) {
            if (firstLine) {
                firstLine = false;
            } else {
                result.append('\n');
            }
            result.append(wrapLine(line, length));
        }
        return result.toString();
    }

    public static String wrapLine(String line) {
        return wrapLine(line, DEFAULT_WRAP_LENGTH);
    }

    public static String wrapLine(String line, int length) {
        if (line.length() <= length) {
            return line;
        }
        StringBuilder result = new StringBuilder();
        int curLength = 0;
        for (String word : splitWords(line)) {
            int wordLength = word.length();
            if (curLength > 0) {
                if (curLength + wordLength < length) {
                    result.append(' ');
                    curLength++;
                } else {
                    result.append('\n');
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
        return Arrays.asList(text.split(NEWLINE_REGEX, -1));
    }

    public static List<String> splitWords(String text) {
        if ((text == null) || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(text.trim().split("\\s+", -1));
    }

    public static String getHeadAndTail(String text, int firstCount, int lastCount) {
        StringBuilder result = new StringBuilder();
        List<String> lines = splitLines(text);
        int index = 0;
        boolean dotsInserted = false;
        for (String line : lines) {
            if ((index < firstCount) || (index >= lines.size() - lastCount)) {
                if (index > 0) {
                    result.append('\n');
                }
                result.append(line);
            } else if (!dotsInserted) {
                result.append('\n');
                result.append(ELLIPSIS_SYMBOL);
                dotsInserted = true;
            }
            index++;
        }
        return result.toString();
    }

    public static String wrapItems(Collection<String> items) {
        return wrapItems(items, DEFAULT_WRAP_LENGTH);
    }

    public static String wrapItems(Collection<String> items, int length) {
        return wrapText(String.join(", ", items), length);
    }

    public static String wrapMessageWithItems(String message, Collection<String> items) {
        return wrapMessageWithItems(message, items, DEFAULT_WRAP_LENGTH);
    }

    public static String wrapMessageWithItems(String message, Collection<String> items, int length) {
        if ((items == null) || items.isEmpty()) {
            return message;
        }
        if (items.size() == 1) {
            return message + ' ' + items.iterator().next();
        }
        String text = makePlural(message) + ":";
        String str = String.join(", ", items);
        if (text.length() + str.length() > length) {
            text += '\n';
        } else {
            text += ' ';
        }
        return text + wrapItems(items, length);
    }

    public static String makePlural(String word) {
        if (word.endsWith("y") && !word.endsWith("ay") && !word.endsWith("ey")
                && !word.endsWith("iy") && !word.endsWith("oy") && !word.endsWith("uy")) {

            return word.substring(0, word.length() - 1) + "ies";
        }
        if (word.endsWith("s") || word.endsWith("x") || word.endsWith("z") || word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        }
        return word + "s";
    }

    public static String makeFirstCapital(String text) {
        if ((text != null) && !text.isEmpty()) {
            char c = text.charAt(0);
            if (Character.isLetter(c)) {
                return Character.toUpperCase(c) + text.substring(1);
            }
        }
        return text;
    }

    public static String repeat(String str, int count) {
        return String.join("", Collections.nCopies(count, str));
    }

    public static String removeHtmlSimpleTags(String str) {
        return str.replaceAll("</?[A-Za-z][A-Za-z0-9]*>", "");
    }

    public static String escapeHtml(String str) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getHtmlSpanColor(String text, Color color) {
        return "<span style=\"" + getHtmlColorAttribute("color", color) + "\">" + text + "</span>";
    }

    public static String getHtmlSpanHighlight(String text, Color color) {
        return "<span style=\"" + getHtmlColorAttribute("background-color", color) + "\">" + text + "</span>";
    }

    public static String getHtmlSpan(String text, Color foregroundColor, Color backgroundColor) {
        String foregroundAttribute = getHtmlColorAttribute("color", foregroundColor);
        String backgroundAttribute = getHtmlColorAttribute("background-color", backgroundColor);
        String attributes = Stream.of(foregroundAttribute, backgroundAttribute)
                .filter(item -> (item != null) && !item.isEmpty())
                .collect(Collectors.joining("; "));

        return "<span style=\"" + attributes + "\">" + text + "</span>";
    }

    private static String getHtmlColorAttribute(String name, Color color) {
        return (color == null) ? "" : String.format(name + ": #%06x", color.getRGB() & 0xffffff);
    }

    public static int countLeadingSpaces(String text) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = LEADING_SPACES_PATTERN.matcher(text);
        return matcher.find() ? matcher.group().length() : 0;
    }

    public static String removeLinebreaks(String text) {
        return replaceLinebreaks(text, "");
    }

    public static String useHtmlLinebreaks(String text) {
        return replaceLinebreaks(text, "<br>");
    }

    public static String replaceLinebreaks(String text, String replacement) {
        if (text == null) {
            return null;
        }
        return text.replaceAll(NEWLINE_REGEX, replacement);
    }

    public static int getTextPosition(String text, int row, int col) {
        int curRow = 1;
        int curCol = 1;
        for (int i = 0; i < text.length(); i++) {
            if (curRow == row) {
                if (curCol == col) {
                    return i;
                }
                curCol++;
            } else if (text.charAt(i) == '\n') {
                curRow++;
                curCol = 1;
            }
        }
        return text.length();
    }

    public static String abbreviate(String s) {
        StringBuilder result = new StringBuilder();
        int len = s == null ? 0 : s.length();
        boolean b = true;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (b && !Character.isSpaceChar(c) || Character.isUpperCase(c)) {
                result.append(c);
            }
            b = Character.isSpaceChar(c);
        }
        return result.toString();
    }

    public static boolean isXmlElement(String text) {
        if (text != null) {
            return XML_ELEMENT_PATTERN.matcher(text).matches();
        }
        return false;
    }

    public static String codeToString(int code) {
        StringBuilder result = new StringBuilder();
        do {
            result.append((char) ('a' + code % 26));
            code /= 26;
        } while (code > 0);
        return result.toString();
    }

    public static String getBulletpointPair(String key, String value) {
        return "\n" + PropertyHelper.BULLET_PREFIX + key
                + ((value == null) || value.isEmpty() ? " is empty" : (": " + value));
    }

    public static String getBulletpointPair(String key, Collection<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        return wrapMessageWithItems("\n" + PropertyHelper.BULLET_PREFIX + key, values);
    }

    public static String getCurrentTimestamp() {
        long currentTime = System.currentTimeMillis();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), ZoneId.systemDefault());
        return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
