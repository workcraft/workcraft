package org.workcraft.plugins.petri_expression.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.LogUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionUtils {

    private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
            "parse error:\\R>>> (.+)\\R    (.*)(\\^+)\\Rsyntax error, (.+)\\R",
            Pattern.UNIX_LINES);

    private static final int POSITION_GROUP = 2;
    private static final int LENGTH_GROUP = 3;
    private static final int MESSAGE_GROUP = 4;

    public static void checkSyntax(CodePanel codePanel) {
        String data = codePanel.getText();

        String errorText = parseData(data);

        if (errorText == null) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        } else {
            Matcher matcher = SYNTAX_ERROR_PATTERN.matcher(errorText);
            if (matcher.find()) {
                String message = "Syntax error: " + matcher.group(MESSAGE_GROUP);
                LogUtils.logError(message);
                int pos = matcher.group(POSITION_GROUP).length();
                int len = matcher.group(LENGTH_GROUP).length();
                int fromPos = getCodePosition(codePanel.getText(), pos);
                int toPos = getCodePosition(codePanel.getText(), pos + len);
                codePanel.highlightError(fromPos, toPos, message);
            } else {
                String message = "Syntax check failed";
                LogUtils.logError(message);
                codePanel.showErrorStatus(message);
            }
        }
    }

    private static String parseData(String data) {
        // TODO: Parse Petri expression and return error text or null
        return data == null ? "Bad Petri expression" : null;
    }

    public static int getCodePosition(String text, int pos) {
        for (int i = 0; i < text.length(); i++) {
            if (i > pos) {
                break;
            }
            if (text.charAt(i) == '\n') {
                pos++;
            }
        }
        return pos;
    }

    public static boolean insert(VisualPetri petri, String expressionText) {
        // TODO: Insert expression as a Petri net into petri model and return success status
        return (petri != null) && (expressionText != null);
    }

    public static boolean insert(VisualStg stg, String expressionText) {
        // TODO: Insert expression as an STG into stg model and return success status
        return (stg != null) && (expressionText != null);
    }

}
