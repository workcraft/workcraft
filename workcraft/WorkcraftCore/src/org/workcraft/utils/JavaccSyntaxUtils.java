package org.workcraft.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.types.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaccSyntaxUtils {

    private static final Pattern LEXER_MESSAGE_PATTERN = Pattern.compile(
            "^Lexical error at line (\\d+), column (\\d+).  Encountered: (.+)$",
            Pattern.UNIX_LINES);

    private static final int LEXER_ROW_GROUP = 1;
    private static final int LEXER_COL_GROUP = 2;
    private static final int LEXER_ENCOUNTERED_GROUP = 3;

    private static final Pattern LEXER_ENCOUNTERED_PATTERN = Pattern.compile(
            "^'(\\d+)' \\((\\d+)\\),$", Pattern.UNIX_LINES);

    private static final int LEXER_ENCOUNTERED_CODE_GROUP = 1;

    private static final Pattern PARSER_MESSAGE_PATTERN = Pattern.compile(
            "^Encountered \"(.+)\" at line (\\d+), column (\\d+).\\RWas expecting one of:\\R((.+)(\\R(.+))+)\\R$",
            Pattern.UNIX_LINES | Pattern.MULTILINE);

    private static final int PARSER_ENCOUNTERED_GROUP = 1;
    private static final int PARSER_ROW_GROUP = 2;
    private static final int PARSER_COL_GROUP = 3;
    private static final int PARSER_EXPECTED_GROUP = 4;

    private static final Pattern PARSER_ENCOUNTERED_PATTERN = Pattern.compile(
            "^ (.+) \"(.+) \"$", Pattern.UNIX_LINES);

    private static final int PARSER_ENCOUNTERED_TYPE_GROUP = 1;
    private static final int PARSER_ENCOUNTERED_VALUE_GROUP = 2;

    public static void processSyntaxError(String text, CodePanel codePanel) {
        Matcher lexerMatcher = LEXER_MESSAGE_PATTERN.matcher(text);
        Matcher parserMatcher = PARSER_MESSAGE_PATTERN.matcher(text);
        if (lexerMatcher.find()) {
            int row = Integer.parseInt(lexerMatcher.group(LEXER_ROW_GROUP));
            int col = Integer.parseInt(lexerMatcher.group(LEXER_COL_GROUP));
            String encountered = lexerMatcher.group(LEXER_ENCOUNTERED_GROUP);
            String message = "Syntax error: unexpected " + extractLexerEncounteredToken(encountered);
            LogUtils.logError(message);
            int pos = TextUtils.getTextPosition(codePanel.getText(), row, col);
            codePanel.highlightError(pos, pos + 1, message);
        } else if (parserMatcher.find()) {
            int row = Integer.parseInt(parserMatcher.group(PARSER_ROW_GROUP));
            int col = Integer.parseInt(parserMatcher.group(PARSER_COL_GROUP));
            String encountered = parserMatcher.group(PARSER_ENCOUNTERED_GROUP);
            String expected = parserMatcher.group(PARSER_EXPECTED_GROUP)
                    .replaceAll(" ", "")
                    .replaceAll("\\.\\.\\.", "")
                    .replaceAll("\\R", ", ");

            Pair<String, String> token = extractParserEncounteredToken(encountered);
            String message = "Syntax error: encountered " + token.getFirst() + ", while expecting one of " + expected;

            LogUtils.logError(message);
            int pos = TextUtils.getTextPosition(codePanel.getText(), row, col);
            codePanel.highlightError(pos, pos + token.getSecond().length(), message);
        } else {
            String message = "Syntax check failed.";
            LogUtils.logError(message);
            codePanel.showErrorStatus(message);
        }
    }

    private static String extractLexerEncounteredToken(String text) {
        Matcher matcher = LEXER_ENCOUNTERED_PATTERN.matcher(text);
        if (matcher.find()) {
            String code = matcher.group(LEXER_ENCOUNTERED_CODE_GROUP);
            return "\"" + (char) Integer.parseInt(code) + "\"";
        }
        return text;
    }

    private static Pair<String, String> extractParserEncounteredToken(String text) {
        Matcher matcher = PARSER_ENCOUNTERED_PATTERN.matcher(text);
        if (matcher.find()) {
            return Pair.of(matcher.group(PARSER_ENCOUNTERED_TYPE_GROUP), matcher.group(PARSER_ENCOUNTERED_VALUE_GROUP));
        }
        return Pair.of(text, "");
    }

}
