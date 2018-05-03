package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriUtils;
import org.workcraft.plugins.petri.Place;
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
        final StgModel compStg = getMpsatOutput().getInputStg();
        StgModel stg = getSrcStg(data);
        if ((solution == null) || (compStg == null) || (stg == null)) {
            return solution;
        }
        MpsatSolution result = null;
        Trace compTrace = solution.getMainTrace();
        LogUtils.logMessage("Processing reported composition trace: " + compTrace);
        HashMap<Place, Integer> compMarking = PetriUtils.getMarking(compStg);
        if (!PetriUtils.fireTrace(compStg, compTrace)) {
            LogUtils.logError("Cannot execute reported composition trace: " + compTrace);
        } else {
            HashSet<String> compEnabledSignals = StgUtils.getEnabledSignals(compStg);
            Trace trace = getProjectedTrace(compTrace, data);
            HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
            if (!PetriUtils.fireTrace(stg, trace)) {
                LogUtils.logError("Cannot execute projected trace: " + trace);
            } else {
                for (SignalTransition transition: stg.getSignalTransitions()) {
                    if (stg.isEnabled(transition) && (transition.getSignalType() == Type.OUTPUT)
                            && !compEnabledSignals.contains(transition.getSignalName())) {
                        String signalRef = transition.getSignalName();
                        LogUtils.logWarning("Output '" + signalRef + "' is unexpectedly enabled after projected trace: " + trace);
                        trace.add(stg.getNodeReference(transition));
                        String comment = "Unexpected change of output '" + signalRef + "'";
                        result = new MpsatSolution(trace, null, comment);
                        break;
                    }
                }
                if (result == null) {
                    LogUtils.logMessage("No outputs are enabled after projected trace: " + trace);
                }
            }
            PetriUtils.setMarking(stg, marking);
        }
        PetriUtils.setMarking(compStg, compMarking);
        return result;
    }

}
