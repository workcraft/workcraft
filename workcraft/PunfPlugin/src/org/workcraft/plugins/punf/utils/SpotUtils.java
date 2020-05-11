package org.workcraft.plugins.punf.utils;

import org.workcraft.Framework;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutput;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaOutputInterpreter;
import org.workcraft.plugins.punf.tasks.Ltl2tgbaTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
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
        String text = FileUtils.readAllText(output.getOutputFile());
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

    public static void checkSyntax(WorkspaceEntry we, JTextArea textArea) {
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        File spotFile = new File(directory, "assertion.spot");
        spotFile.deleteOnExit();
        String data = textArea.getText();
        try {
            FileUtils.dumpString(spotFile, TextUtils.removeLinebreaks(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TaskManager manager = Framework.getInstance().getTaskManager();
        Ltl2tgbaTask ltl2tgbaTask = new Ltl2tgbaTask(spotFile, directory);
        ltl2tgbaTask.setQuiet(!PunfSettings.getVerboseSyntaxCheck());
        Result<? extends Ltl2tgbaOutput> result = manager.execute(ltl2tgbaTask, "Checking SPOT assertion syntax");
        Ltl2tgbaOutput ltl2tgbaOutput = result.getPayload();

        if (result.isSuccess()) {
            new Ltl2tgbaOutputInterpreter(we, ltl2tgbaOutput, true).interpret();
        }

        if (result.isFailure()) {
            String message = "Syntax error in SPOT expression";
            Matcher matcher = SYNTAX_ERROR_PATTERN.matcher(ltl2tgbaOutput.getStderrString());
            if (matcher.find()) {
                String detail = matcher.group(MESSAGE_GROUP);
                int pos = matcher.group(POSITION_GROUP).length();
                int len = matcher.group(LENGTH_GROUP).length();
                message += ":\n" + detail + "\n\nHighlight the problem in editor?";

                if (DialogUtils.showConfirmError(message)) {
                    int fromPos = getTextAreaPosition(textArea, pos);
                    int toPos = getTextAreaPosition(textArea, pos + len);
                    GuiUtils.highlightText(textArea, fromPos, toPos);
                    textArea.setCaretPosition(fromPos);
                }
                textArea.requestFocus();
            } else {
                Throwable cause = result.getCause();
                if (cause != null) {
                    message += ".\n\n" + cause.toString();
                }
                DialogUtils.showError(message);
            }
        }
    }

    public static int getTextAreaPosition(JTextArea textArea, int pos) {
        String text = textArea.getText();
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
