package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbstractCompositionOutputInterpreter extends ReachabilityOutputInterpreter {

    private static final Pattern DEAD_SIGNAL_PATTERN = Pattern.compile(
            "Warning: signal (\\w+) is dead");

    AbstractCompositionOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public String getMessage(boolean propertyHolds) {
        String result = super.getMessage(propertyHolds);
        String mpsatStderr = getOutput().getStderrString();
        Matcher matcher = DEAD_SIGNAL_PATTERN.matcher(mpsatStderr);
        List<String> signals = new ArrayList<>();
        while (matcher.find()) {
            signals.add(matcher.group(1));
        }
        if (!signals.isEmpty()) {
            if (propertyHolds) {
                result += TextUtils.wrapMessageWithItems("\nYet composition has dead signal", signals);
                result += "\n\nWarning: dead signals may indicate design issues!";
            } else {
                result += " Composition has dead signals.";
            }
        }
        return result;
    }

    @Override
    public void reportSolutions(String message, List<Solution> solutions) {
        boolean needsMultiLineMessage = (solutions.size() > 1);
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Violation traces of the composition:");
        }
        for (Solution solution : solutions) {
            Trace trace = solution.getMainTrace();
            if (needsMultiLineMessage) {
                LogUtils.logMessage("  " + trace);
            } else {
                LogUtils.logMessage("Violation trace of the composition: " + trace);
            }
        }
        super.reportSolutions(message, solutions);
    }

    public StgModel getCompositionStg() {
        return StgUtils.importStg(getExportOutput().getFile());
    }

}
