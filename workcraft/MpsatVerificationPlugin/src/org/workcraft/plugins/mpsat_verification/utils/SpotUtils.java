package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.Framework;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.mpsat_verification.tasks.Ltl2tgbaOutput;
import org.workcraft.plugins.mpsat_verification.tasks.Ltl2tgbaOutputInterpreter;
import org.workcraft.plugins.mpsat_verification.tasks.Ltl2tgbaTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotUtils {

    private static final Pattern STUTTER_EXAMPLE_PATTERN = Pattern.compile(
            "spot-accepted-word: \"(.*)\"\\Rspot-rejected-word: \"(.*)\"",
            Pattern.UNIX_LINES);

    private static final int ACCEPT_WORD_GROUP = 1;
    private static final int REJECT_WORD_GROUP = 2;

    private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
            "parse error:\\R>>> (.+)\\R    (.*)(\\^+)\\Rsyntax error, (.+)\\R",
            Pattern.UNIX_LINES);

    private static final int POSITION_GROUP = 2;
    private static final int LENGTH_GROUP = 3;
    private static final int MESSAGE_GROUP = 4;

    public static Pair<String, String> extractStutterExample(Ltl2tgbaOutput output) throws IOException {
        String text = FileUtils.readAllText(output.getHoaFile());
        return extractStutterExample(text);
    }

    public static Pair<String, String> extractStutterExample(String text) {
        Matcher matcher = STUTTER_EXAMPLE_PATTERN.matcher(text);
        if (matcher.find()) {
            String acceptedWord = matcher.group(ACCEPT_WORD_GROUP);
            String rejectedWord = matcher.group(REJECT_WORD_GROUP);
            return Pair.of(acceptedWord, rejectedWord);
        }
        return null;
    }

    public static void checkSyntax(WorkspaceEntry we, CodePanel codePanel) {
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        File spotFile = new File(directory, "assertion.spot");
        spotFile.deleteOnExit();
        String data = codePanel.getText();
        try {
            FileUtils.dumpString(spotFile, TextUtils.removeLinebreaks(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskManager manager = Framework.getInstance().getTaskManager();
        Ltl2tgbaTask ltl2tgbaTask = new Ltl2tgbaTask(spotFile, directory);
        Result<? extends Ltl2tgbaOutput> result = manager.execute(ltl2tgbaTask, "Checking SPOT assertion syntax");
        Ltl2tgbaOutput ltl2tgbaOutput = result.getPayload();

        if (result.isSuccess()) {
            if (new Ltl2tgbaOutputInterpreter(we, ltl2tgbaOutput, false).interpret()) {
                String message = "Property is syntactically correct and stutter-invariant";
                codePanel.showInfoStatus(message);
            } else {
                String message = "Property is syntactically correct but stutter-sensitive";
                codePanel.showErrorStatus(message);
            }
        }

        if (result.isFailure()) {
            String text = ltl2tgbaOutput == null ? "" : ltl2tgbaOutput.getStderrString();
            Matcher matcher = SYNTAX_ERROR_PATTERN.matcher(text);
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

}
