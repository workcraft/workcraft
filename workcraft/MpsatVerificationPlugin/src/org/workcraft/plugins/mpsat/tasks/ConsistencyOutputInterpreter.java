package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Triple;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ConsistencyOutputInterpreter extends ReachabilityOutputInterpreter {

    ConsistencyOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, VerificationOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);
        for (Solution solution: solutions) {
            LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            int size = trace.size();
            if (size <= 0) {
                LogUtils.logMessage("No consistency violation detected");
            } else {
                String lastTransitionRef = trace.get(size - 1);
                final Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(lastTransitionRef);
                if (r == null) continue;
                String signalRef = r.getFirst();
                String comment = "Signal '" + signalRef + "' is inconsistent";
                LogUtils.logWarning(comment + " after trace: " + trace);
                Solution processedSolution = new Solution(trace, null, comment);
                result.add(processedSolution);
            }
        }
        return result;
    }

}
