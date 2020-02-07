package org.workcraft.plugins.punf.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.simulation.ReachibilityDialog;
import org.workcraft.gui.simulation.SimulationUtils;
import org.workcraft.gui.simulation.Solution;
import org.workcraft.gui.simulation.Trace;
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
            Trace prefixTrace = SimulationUtils.getTrace(matcherLtlxSolution.group(1));
            prefixTrace.setPosition(prefixTrace.size() + 1);
            Trace loopTrace = SimulationUtils.getTrace(matcherLtlxSolution.group(2));
            return new Solution(prefixTrace, loopTrace);
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
        String traceInfo = "Trace prefix and loop for counter-example:";
        return "<html>" + message + "<br><br>" + traceInfo + "</html>";
    }

    @Override
    public void run() {
        Solution solution = parseLtlxSolution();
        boolean isViolated = SimulationUtils.hasTraces(solution);
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
