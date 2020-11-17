package org.workcraft.plugins.punf.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunfLtlxOutputInterpreter extends AbstractOutputInterpreter<PunfOutput, Boolean> {

    private static final String TITLE = "Verification results";

    private static final Pattern MISSING_STUTTER_INVARIANT_PATTERN = Pattern.compile(
            "Warning: the automaton does not declare the `stutter-invariant' property, "
            + "on which the verification relies");

    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "The property is violated:\\s*\\R\\s*Prefix: (.+)\\R\\s*Loop: (.+)",
            Pattern.UNIX_LINES);

    public PunfLtlxOutputInterpreter(WorkspaceEntry we, PunfOutput output, boolean interactive) {
        super(we, output, interactive);
    }

    private Solution getSolution() {
        String mpsatStdout = getOutput().getStdoutString();
        Matcher matcher = SOLUTION_PATTERN.matcher(mpsatStdout);
        if (matcher.find()) {
            Trace trace = TraceUtils.deserialiseTrace(matcher.group(1));
            int loopPosition = trace.size();
            trace.addAll(TraceUtils.deserialiseTrace(matcher.group(2)));
            Solution solution = new Solution(trace);
            solution.setLoopPosition(loopPosition);
            return solution;
        }
        return null;
    }

    public String getMessage(boolean propertyHolds) {
        String result = "Temporal property ";
        result += propertyHolds ? "holds." : "is violated.";
        String mpsatStderr = getOutput().getStderrString();
        Matcher matcher = MISSING_STUTTER_INVARIANT_PATTERN.matcher(mpsatStderr);
        if (matcher.find()) {
            result += propertyHolds ? "\n" : "<br>";
            result += "Warning: the automaton does not declare the `stutter-invariant' property.";
        }
        return result;
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        Solution solution = getSolution();
        boolean propertyHolds = solution == null;
        String message = getMessage(propertyHolds);
        if (propertyHolds) {
            if (isInteractive()) {
                DialogUtils.showInfo(message, TITLE);
            } else {
                LogUtils.logInfo(message);
            }
        } else {
            LogUtils.logWarning(message);
            Framework framework = Framework.getInstance();
            if (isInteractive() && framework.isInGuiMode()) {
                MainWindow mainWindow = framework.getMainWindow();
                String traceInfo = "Violation trace in <i>PREFIX</i>{<i>LOOP</i>} form:";
                String extendedMessage = "<html>" + message + "<br><br>" + traceInfo + "</html>";
                ReachabilityDialog solutionsDialog = new ReachabilityDialog(
                        mainWindow, getWorkspaceEntry(), TITLE, extendedMessage, solution);

                solutionsDialog.reveal();
            }
        }
        return propertyHolds;
    }

}
