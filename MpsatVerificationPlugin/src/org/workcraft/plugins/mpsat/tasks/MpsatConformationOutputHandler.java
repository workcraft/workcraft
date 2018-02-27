package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;

import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
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
        LogUtils.logMessage("Processing reported trace: " + solution.getMainTrace());
        Trace trace = getProjectedTrace(solution.getMainTrace(), data);
        MpsatSolution result = null;
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            LogUtils.logWarning("Cannot execute projected trace: " + trace);
        } else {
            for (SignalTransition transition: stg.getSignalTransitions()) {
                if (stg.isEnabled(transition) && (transition.getSignalType() == Type.OUTPUT)) {
                    String signalRef = transition.getSignalName();
                    LogUtils.logMessage("Output '" + signalRef + "' is unexpectedly enabled after projected trace: " + trace);
                    trace.add(stg.getNodeReference(transition));
                    String comment = "Unexpected change of output '" + signalRef + "'";
                    result = new MpsatSolution(trace, null, comment);
                    break;
                }
            }
        }
        PetriUtils.setMarking(stg, marking);
        if (result == null) {
            LogUtils.logMessage("No outputs are enabled after projected trace: " + trace);
        }
        return result;
    }

}
