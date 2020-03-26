package org.workcraft.plugins.punf.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.dialogs.ReachibilityDialog;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.ResultHandler;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunfLtlxOutputHandler implements ResultHandler<PunfOutput, Boolean> {

    private static final String TITLE = "Verification results";

    private static final Pattern MISSING_STUTTER_INVARIANT_PATTERN = Pattern.compile(
            "Warning: the automaton does not declare the `stutter-invariant' property, "
            + "on which the verification relies");

    private static final Pattern SOLUTION_PATTERN = Pattern.compile(
            "The property is violated:\\s*\\R\\s*Prefix: (.+)\\R\\s*Loop: (.+)",
            Pattern.UNIX_LINES);

    private final WorkspaceEntry we;
    private final boolean interactive;


    public PunfLtlxOutputHandler(WorkspaceEntry we, boolean interactive) {
        this.we = we;
        this.interactive = interactive;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    private Solution parseLtlxSolution(String punfStdout) {
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

    public String getMessage(boolean isViolated, String punfStderr) {
        String result = "Temporal property ";
        result += isViolated ? "is violated." : "holds.";
        Matcher matcher = MISSING_STUTTER_INVARIANT_PATTERN.matcher(punfStderr);
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
    public Boolean handle(Result<? extends PunfOutput> punfResult) {
        PunfOutput punfOutput = (punfResult == null) ? null : punfResult.getPayload();
        if (punfOutput == null) {
            return null;
        }
        Solution solution = parseLtlxSolution(punfOutput.getStdoutString());
        boolean isViolated = TraceUtils.hasTraces(solution);
        String message = getMessage(isViolated, punfOutput.getStderrString());
        if (!isViolated) {
            if (interactive) {
                DialogUtils.showInfo(message, TITLE);
            } else {
                LogUtils.logInfo(message);
            }
        } else {
            LogUtils.logWarning(message);
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode() && interactive) {
                message = extendMessage(message);
                ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                        framework.getMainWindow(), getWorkspaceEntry(), TITLE, message, solution);

                solutionsDialog.reveal();
            }
        }
        return !isViolated;
    }

}
