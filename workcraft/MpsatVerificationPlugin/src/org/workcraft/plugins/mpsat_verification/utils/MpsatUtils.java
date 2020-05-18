package org.workcraft.plugins.mpsat_verification.utils;

import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.MpsatDataSerialiser;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.MpsatSyntaxCheckTask;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.presets.PresetManager;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatUtils {

    private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
            "Error: incorrect syntax of the expression: (.+) \\(located at (\\d+).(\\d+)(-(\\d+))?\\)\\R",
            Pattern.UNIX_LINES);

    private static final int MESSAGE_GROUP = 1;
    private static final int LINE_GROUP = 2;
    private static final int START_GROUP = 3;
    private static final int END_GROUP = 5;

    private static final Pattern UNEXPECTED_MESSAGE_PATTERN = Pattern.compile("^syntax error, unexpected '(.+)'$");
    private static final Pattern UNDECLARED_MESSAGE_PATTERN = Pattern.compile("^Undeclared identifier: (.+)$");

    public static  String getToolchainDescription(String title) {
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }

    public static boolean mutexStructuralCheck(Stg stg, boolean allowEmptyMutexPlaces) {
        Collection<StgPlace> mutexPlaces = stg.getMutexPlaces();
        if (!allowEmptyMutexPlaces && mutexPlaces.isEmpty()) {
            DialogUtils.showWarning("No mutex places found to check implementability.");
            return false;
        }
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: mutexPlaces) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            Collection<String> problematicPlacesRefs = ReferenceHelper.getReferenceList(stg, problematicPlaces);
            DialogUtils.showError("A mutex place must precede two transitions of distinct\n" +
                    "output or internal signals, each with a single trigger.\n\n" +
                    TextUtils.wrapMessageWithItems("Problematic place", problematicPlacesRefs));

            return false;
        }
        return true;
    }

    public static VerificationParameters deserialiseData(String data, MpsatDataSerialiser dataSerialiser) {
        String description = "Custom REACH assertion";
        if (data.startsWith("<") && data.endsWith(">")) {
            Document document = PresetManager.buildPresetDocumentFromSettings(description, data);
            return dataSerialiser.fromXML(document.getDocumentElement());
        }
        return new VerificationParameters(description,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                data, true);
    }

    public static void checkSyntax(WorkspaceEntry we, JTextArea textArea, VerificationParameters verificationParameters) {
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);

        MpsatSyntaxCheckTask task = new MpsatSyntaxCheckTask(verificationParameters, directory);
        TaskManager manager = Framework.getInstance().getTaskManager();
        Result<? extends ExternalProcessOutput> result = manager.execute(task, "Checking REACH assertion syntax");

        if (result.isSuccess()) {
            DialogUtils.showInfo("Property is syntactically correct.");
        }

        if (result.isFailure()) {
            String message = "Syntax error in REACH expression";
            Matcher matcher = SYNTAX_ERROR_PATTERN.matcher(result.getPayload().getStderrString());
            if (matcher.find()) {
                String detail = processDetail(matcher.group(MESSAGE_GROUP));
                String lineStr = matcher.group(LINE_GROUP);
                String fromStr = matcher.group(START_GROUP);
                String toStr = matcher.group(END_GROUP);
                int line = Integer.valueOf(lineStr);
                int pos = Integer.valueOf(fromStr);
                int len = toStr == null ? 1 : Integer.valueOf(toStr) - pos;
                message += ":\n" + detail + "\n\nHighlight the problem in editor?";

                if (DialogUtils.showConfirmError(message)) {
                    int fromPos = getTextAreaPosition(textArea, line, pos);
                    int toPos = fromPos + len;
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

    private static String processDetail(String detail) {
        Matcher unexpectedMessageMatcher = UNEXPECTED_MESSAGE_PATTERN.matcher(detail);
        if (unexpectedMessageMatcher.find()) {
            return "unexpected '" + unexpectedMessageMatcher.group(1) + "'";
        }
        Matcher undeclaredMessageMatcher = UNDECLARED_MESSAGE_PATTERN.matcher(detail);
        if (undeclaredMessageMatcher.find()) {
            return "undeclared identifier '" + undeclaredMessageMatcher.group(1) + "'";
        }
        return detail;
    }

    private static int getTextAreaPosition(JTextArea textArea, int row, int col) {
        String text = textArea.getText();
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

}
