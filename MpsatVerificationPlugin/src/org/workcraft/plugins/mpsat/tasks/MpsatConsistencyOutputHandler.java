package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Trace;
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
    public MpsatSolution processSolution(MpsatSolution solution, ComponentData data) {
        LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
        Trace trace = getProjectedTrace(solution.getMainTrace(), data);
        MpsatSolution result = null;
        int size = trace.size();
        if (size <= 0) {
            LogUtils.logMessage("No consistency violation detected");
        } else {
            String lastTransitionRef = trace.get(size - 1);
            final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(lastTransitionRef);
            if (r != null) {
                String signalRef = r.getFirst();
                String comment = "Signal '" + signalRef + "' is inconsistent";
                LogUtils.logMessage(comment + " after projected trace: " + trace);
                result = new MpsatSolution(trace, null, comment);
            }
        }
        return result;
    }

}
