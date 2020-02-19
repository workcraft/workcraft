package org.workcraft.plugins.punf.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.dialogs.ReachibilityDialog;
import org.workcraft.utils.TraceUtils;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunfLtlxOutputHandler implements Runnable {

    private static final String TITLE = "Verification results";

    private static final Pattern MISSING_STUTTER_INVARIANT_PATTERN = Pattern.compile(
            "Warning: the automaton does not declare the `stutter-invariant' property, "
            + "on which the verification relies");

    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "The property is violated:\\s*\\R\\s*Prefix: (.+)\\R\\s*Loop: (.+)",
            Pattern.UNIX_LINES);

    private final WorkspaceEntry we;
    private final PunfOutput punfOutput;


    public PunfLtlxOutputHandler(WorkspaceEntry we, PunfOutput punfOutput) {
        this.we = we;
        this.punfOutput = punfOutput;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    private Solution parseLtlxSolution() {
        String punfStdout = punfOutput.getStdoutString();
        Matcher matcherLtlxSolution = SOLUTION_PATTERN.matcher(punfStdout);
        if (matcherLtlxSolution.find()) {
            Trace trace = TraceUtils.deserialiseTrace(matcherLtlxSolution.group(1));
            int loopPosition = trace.size();
            trace.addAll(TraceUtils.deserialiseTrace(matcherLtlxSolution.group(2)));
            Solution solution = new Solution(trace);
            solution.setLoopPosition(loopPosition);
            return solution;
        }
        return null;
    }

    public String getMessage(boolean isViolated) {
        String result = "Temporal property ";
        result += isViolated ? "is violated." : "holds.";
        Matcher matcher = MISSING_STUTTER_INVARIANT_PATTERN.matcher(punfOutput.getStderrString());
        if (matcher.find()) {
            result += isViolated ? "<br>" : "\n";
            result += "Warning: the automaton does not declare the `stutter-invariant' property.";
        }
        return result;
    }

    public String extendMessage(String message) {
        String traceInfo = "Violation trace in <i>PREFIX</i>{<i>LOOP</i>} form:";
        return "<html>" + message + "<br><br>" + traceInfo + "</html>";
    }

    @Override
    public void run() {
        Solution solution = parseLtlxSolution();
        boolean isViolated = TraceUtils.hasTraces(solution);
        String message = getMessage(isViolated);
        if (!isViolated) {
            DialogUtils.showInfo(message, TITLE);
        } else {
            LogUtils.logWarning(message);
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()) {
                message = extendMessage(message);
                ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                        framework.getMainWindow(), getWorkspaceEntry(), TITLE, message, solution);

                solutionsDialog.reveal();
            }
        }
    }

}
