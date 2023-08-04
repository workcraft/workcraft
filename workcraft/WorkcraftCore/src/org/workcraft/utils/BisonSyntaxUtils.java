package org.workcraft.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.tasks.ExternalProcessOutput;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BisonSyntaxUtils {

    private static final String MESSAGE_REGEX = "(.+)\\R";
    private static final String MESSAGE_WITH_LOCATION_REGEX = "(.+) \\(located at (\\d+).(\\d+)(-((\\d+).)?(\\d+))?\\)\\R";
    private static final int MESSAGE_GROUP = 1;
    private static final int START_ROW_GROUP = 2;
    private static final int START_COL_GROUP = 3;
    private static final int END_ROW_GROUP = 6;
    private static final int END_COL_GROUP = 7;

    private static final Pattern UNEXPECTED_MESSAGE_PATTERN = Pattern.compile("^syntax error, unexpected (.+)$");
    private static final Pattern UNDECLARED_MESSAGE_PATTERN = Pattern.compile("^Undeclared identifier: (.+)$");

    private static final String SYNTAX_ERROR_PREFIX = "Syntax error: ";
    private static final String WARNING_PREFIX = "Potential issue: ";

    public static void processPossibleWarning(String patternPrefix, ExternalProcessOutput output, CodePanel codePanel) {
        String text = output == null ? "" : output.getStderrString();
        Pattern messageWithLocationPattern = Pattern.compile(patternPrefix + MESSAGE_WITH_LOCATION_REGEX, Pattern.UNIX_LINES);
        Matcher messageWithLocationMatcher = messageWithLocationPattern.matcher(text);
        Pattern messagePattern = Pattern.compile(patternPrefix + MESSAGE_REGEX, Pattern.UNIX_LINES);
        Matcher messageMatcher = messagePattern.matcher(text);
        if (messageWithLocationMatcher.find()) {
            String message = WARNING_PREFIX + processDetail(messageWithLocationMatcher.group(MESSAGE_GROUP));
            LogUtils.logWarning(message);
            String startRowStr = messageWithLocationMatcher.group(START_ROW_GROUP);
            String startColStr = messageWithLocationMatcher.group(START_COL_GROUP);
            String endRowStr = messageWithLocationMatcher.group(END_ROW_GROUP);
            String endColStr = messageWithLocationMatcher.group(END_COL_GROUP);
            int startRow = Integer.parseInt(startRowStr);
            int startCol = Integer.parseInt(startColStr);
            int endRow = endRowStr == null ? startRow : Integer.parseInt(endRowStr);
            int endCol = endColStr == null ? startCol + 1 : Integer.parseInt(endColStr);
            int startPos = TextUtils.getTextPosition(codePanel.getText(), startRow, startCol);
            int endPos = TextUtils.getTextPosition(codePanel.getText(), endRow, endCol);
            codePanel.highlightWarning(startPos, endPos, message);
        } else if (messageMatcher.find()) {
            String message = WARNING_PREFIX + processDetail(messageMatcher.group(MESSAGE_GROUP));
            LogUtils.logWarning(message);
            codePanel.showWarningStatus(message);
        } else {
            String message = "Expression is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        }
    }

    public static void processSyntaxError(String patternPrefix, ExternalProcessOutput output, CodePanel codePanel) {
        String text = output == null ? "" : output.getStderrString();
        Pattern messageWithLocationPattern = Pattern.compile(patternPrefix + MESSAGE_WITH_LOCATION_REGEX, Pattern.UNIX_LINES);
        Matcher messageWithLocationMatcher = messageWithLocationPattern.matcher(text);
        Pattern messagePattern = Pattern.compile(patternPrefix + MESSAGE_REGEX, Pattern.UNIX_LINES);
        Matcher messageMatcher = messagePattern.matcher(text);
        if (messageWithLocationMatcher.find()) {
            String message = SYNTAX_ERROR_PREFIX + processDetail(messageWithLocationMatcher.group(MESSAGE_GROUP));
            LogUtils.logError(message);
            String startRowStr = messageWithLocationMatcher.group(START_ROW_GROUP);
            String startColStr = messageWithLocationMatcher.group(START_COL_GROUP);
            String endRowStr = messageWithLocationMatcher.group(END_ROW_GROUP);
            String endColStr = messageWithLocationMatcher.group(END_COL_GROUP);
            int startRow = Integer.parseInt(startRowStr);
            int startCol = Integer.parseInt(startColStr);
            int endRow = endRowStr == null ? startRow : Integer.parseInt(endRowStr);
            int endCol = endColStr == null ? startCol + 1 : Integer.parseInt(endColStr);
            int startPos = TextUtils.getTextPosition(codePanel.getText(), startRow, startCol);
            int endPos = TextUtils.getTextPosition(codePanel.getText(), endRow, endCol);
            codePanel.highlightError(startPos, endPos, message);
        } else if (messageMatcher.find()) {
            String message = SYNTAX_ERROR_PREFIX + processDetail(messageMatcher.group(MESSAGE_GROUP));
            LogUtils.logError(message);
            codePanel.showErrorStatus(message);
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
