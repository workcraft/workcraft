package org.workcraft.utils;

import org.workcraft.gui.controls.CodePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaccSyntaxUtils {

    public record SyntaxError(String message, int row, int col, int length) {
    }

    // Lexical error at line 5, column 2.  Encountered: ' ' (32), after prefix "#"
    private static final Pattern LEXER_MESSAGE_PATTERN = Pattern.compile(
            "^Lexical error at line (\\d+), column (\\d+)\\."
                    + "\\s+Encountered:\\s+(?:<EOF>|'.*' \\((\\d+)\\)),?\\s+after (?:prefix |: )?\"(.*)\"$");

    private static final int LEXER_ROW_GROUP = 1;
    private static final int LEXER_COL_GROUP = 2;
    private static final int LEXER_CODE_GROUP = 3;
    private static final int LEXER_PREFIX_GROUP = 4;

    // Encountered " <NAME> "baz "" at line 5, column 13.
    private static final Pattern PARSER_MESSAGE_PATTERN = Pattern.compile(
            "^Encountered \"(.+)\" at line (\\d+), column (\\d+)\\.$", Pattern.MULTILINE);

    private static final int PARSER_ENCOUNTERED_GROUP = 1;
    private static final int PARSER_ROW_GROUP = 2;
    private static final int PARSER_COL_GROUP = 3;

    private static final Pattern PARSER_ENCOUNTERED_PATTERN = Pattern.compile("^ (.+) \"(.+) \"$");

    private static final int PARSER_ENCOUNTERED_VALUE_GROUP = 2;

    // "(" ...
    private static final Pattern PARSER_EXPECTED_PATTERN = Pattern.compile("^\\s+(.+?)\\s*\\.\\.\\.$");

    private static final String END_OF_FILE = "end of file";

    public static void processSyntaxError(String text, CodePanel codePanel) {
        SyntaxError error = parseSyntaxError(text);
        if (error == null) {
            String message = "Syntax check failed.";
            LogUtils.logError(message);
            codePanel.showErrorStatus(message);
        } else {
            String message = "Syntax error: " + error.message();
            LogUtils.logError(message);
            int pos = TextUtils.getTextPosition(codePanel.getText(), error.row(), error.col());
            codePanel.highlightError(pos, pos + error.length(), message);
        }
    }

    /**
     * Extract user-friendly description and location from the message of a JavaCC lexer or parser exception.
     *
     * @return syntax error, or null if the text is not a recognised JavaCC message
     */
    public static SyntaxError parseSyntaxError(String text) {
        if (text == null) {
            return null;
        }
        Matcher lexerMatcher = LEXER_MESSAGE_PATTERN.matcher(text);
        if (lexerMatcher.find()) {
            int row = Integer.parseInt(lexerMatcher.group(LEXER_ROW_GROUP));
            int col = Integer.parseInt(lexerMatcher.group(LEXER_COL_GROUP));
            String code = lexerMatcher.group(LEXER_CODE_GROUP);
            String prefix = lexerMatcher.group(LEXER_PREFIX_GROUP);
            String message = "unexpected " + (code == null ? END_OF_FILE : "\"" + (char) Integer.parseInt(code) + "\"")
                    + (prefix.isEmpty() ? "" : " after \"" + prefix + "\"");

            return new SyntaxError(message, row, col, 1);
        }
        Matcher parserMatcher = PARSER_MESSAGE_PATTERN.matcher(text);
        if (parserMatcher.find()) {
            int row = Integer.parseInt(parserMatcher.group(PARSER_ROW_GROUP));
            int col = Integer.parseInt(parserMatcher.group(PARSER_COL_GROUP));
            String encountered = parserMatcher.group(PARSER_ENCOUNTERED_GROUP);
            Matcher encounteredMatcher = PARSER_ENCOUNTERED_PATTERN.matcher(encountered);
            String value = encounteredMatcher.find() ? encounteredMatcher.group(PARSER_ENCOUNTERED_VALUE_GROUP) : "";
            String unexpected = !value.isEmpty() ? "\"" + value + "\""
                    : encountered.startsWith("<EOF>") ? END_OF_FILE : encountered;

            String message = "unexpected " + unexpected + getExpectedSuffix(text.substring(parserMatcher.end()));

            return new SyntaxError(message, row, col, value.length());
        }
        return null;
    }

    private static String getExpectedSuffix(String text) {
        List<String> expected = new ArrayList<>();
        for (String line : text.split("\\R")) {
            Matcher matcher = PARSER_EXPECTED_PATTERN.matcher(line);
            if (matcher.find()) {
                expected.add(matcher.group(1));
            }
        }
        return expected.isEmpty() ? "" : ", expected one of " + String.join(", ", expected);
    }

}
