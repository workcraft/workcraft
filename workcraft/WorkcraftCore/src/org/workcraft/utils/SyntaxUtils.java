package org.workcraft.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.tasks.ExternalProcessOutput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxUtils {

    private static final String LOCATION_REGEX = "(.+) \\(located at (\\d+).(\\d+)(-((\\d+).)?(\\d+))?\\)\\R";
    private static final int MESSAGE_GROUP = 1;
    private static final int START_ROW_GROUP = 2;
    private static final int START_COL_GROUP = 3;
    private static final int END_ROW_GROUP = 6;
    private static final int END_COL_GROUP = 7;

    private static final Pattern UNEXPECTED_MESSAGE_PATTERN = Pattern.compile("^syntax error, unexpected (.+)$");
    private static final Pattern UNDECLARED_MESSAGE_PATTERN = Pattern.compile("^Undeclared identifier: (.+)$");

    public static void processBisonSyntaxError(String patternPrefix, ExternalProcessOutput output, CodePanel codePanel) {
        String text = output == null ? "" : output.getStderrString();
        Pattern pattern = Pattern.compile(patternPrefix + LOCATION_REGEX, Pattern.UNIX_LINES);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String message = "Syntax error: " + processDetail(matcher.group(MESSAGE_GROUP));
            LogUtils.logError(message);
            String startRowStr = matcher.group(START_ROW_GROUP);
            String startColStr = matcher.group(START_COL_GROUP);
            String endRowStr = matcher.group(END_ROW_GROUP);
            String endColStr = matcher.group(END_COL_GROUP);
            int startRow = Integer.valueOf(startRowStr);
            int startCol = Integer.valueOf(startColStr);
            int endRow = endRowStr == null ? startRow : Integer.valueOf(endRowStr);
            int endCol = endColStr == null ? startCol + 1 : Integer.valueOf(endColStr);
            int startPos = TextUtils.getTextPosition(codePanel.getText(), startRow, startCol);
            int endPos = TextUtils.getTextPosition(codePanel.getText(), endRow, endCol);
            codePanel.highlightError(startPos, endPos, message);
        } else {
            String message = "Syntax check failed.";
            LogUtils.logError(message);
            codePanel.showErrorStatus(message);
        }
    }

    private static String processDetail(String detail) {
        Matcher unexpectedMessageMatcher = UNEXPECTED_MESSAGE_PATTERN.matcher(detail);
        if (unexpectedMessageMatcher.find()) {
            return "unexpected " + unexpectedMessageMatcher.group(1);
        }
        Matcher undeclaredMessageMatcher = UNDECLARED_MESSAGE_PATTERN.matcher(detail);
        if (undeclaredMessageMatcher.find()) {
            return "undeclared identifier '" + undeclaredMessageMatcher.group(1) + "'";
        }
        return detail;
    }

}
