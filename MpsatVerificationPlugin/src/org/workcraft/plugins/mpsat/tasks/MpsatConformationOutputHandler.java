package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatConformationOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatConformationOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public MpsatSolution processSolution(MpsatSolution solution, ComponentData data) {
        StgModel stg = getSrcStg(data);
        if ((solution == null) || (stg == null)) {
            return solution;
        }
        Trace trace = getProjectedTrace(solution.getMainTrace(), data);
        LogUtils.logMessage("Processing conformation violation trace: ");
        LogUtils.logMessage("  reported: " + solution.getMainTrace());
        LogUtils.logMessage("  projected: " + trace);
        String comment = null;
        if (!StgUtils.fireTrace(stg, trace)) {
            LogUtils.logWarning("Cannot execute projected conformation violation trace: " + trace);
        } else {
            for (SignalTransition transition: stg.getSignalTransitions()) {
                if (stg.isEnabled(transition) && (transition.getSignalType() == Type.OUTPUT)) {
                    String signal = transition.getSignalName();
                    trace.add(stg.getNodeReference(transition));
                    LogUtils.logMessage("  extended: " + trace);
                    comment = "Unexpected change of output '" + signal + "'";
                    break;
                }
            }
        }
        return new MpsatSolution(trace, null, comment);
    }

}
