package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.traces.Solution;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpsatLtlxOutputInterpreter extends AbstractOutputInterpreter<MpsatOutput, Boolean> {

    private static final String TITLE = "Verification results";

    private static final Pattern MISSING_STUTTER_INVARIANT_PATTERN = Pattern.compile(
            "Warning: the automaton does not declare the `stutter-invariant' property, "
            + "on which the verification relies");

    public MpsatLtlxOutputInterpreter(WorkspaceEntry we, MpsatOutput output, boolean interactive) {
        super(we, output, interactive);
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
        List<Solution> solutions = getOutput().getSolutions();
        Solution solution = (solutions == null) || solutions.isEmpty() ? null : solutions.iterator().next();
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
                ReachabilityDialog solutionsDialog = new ReachabilityDialog(mainWindow, getWorkspaceEntry(),
                        TITLE, extendedMessage, Collections.singletonList(solution));

                solutionsDialog.reveal();
            }
        }
        return propertyHolds;
    }

}
