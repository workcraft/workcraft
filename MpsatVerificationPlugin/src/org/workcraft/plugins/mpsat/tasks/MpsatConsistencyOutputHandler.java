package org.workcraft.plugins.mpsat.tasks;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Triple;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatConsistencyOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatConsistencyOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();
        ComponentData data = getCompositionData(0);
        for (MpsatSolution solution: solutions) {
            LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
            Trace trace = getProjectedTrace(solution.getMainTrace(), data);
            int size = trace.size();
            if (size <= 0) {
                LogUtils.logMessage("No consistency violation detected");
            } else {
                String lastTransitionRef = trace.get(size - 1);
                final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(lastTransitionRef);
                if (r == null) continue;
                String signalRef = r.getFirst();
                String comment = "Signal '" + signalRef + "' is inconsistent";
                LogUtils.logMessage(comment + " after projected trace: " + trace);
                MpsatSolution processedSolution = new MpsatSolution(trace, null, comment);
                result.add(processedSolution);
            }
        }
        return result;
    }

}
