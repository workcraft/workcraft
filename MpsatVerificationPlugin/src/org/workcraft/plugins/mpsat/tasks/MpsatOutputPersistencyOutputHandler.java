package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.Trace;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatOutputPersistencyOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatOutputPersistencyOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
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
            HashSet<String> enabledLocalSignals = StgUtils.getEnabledLocalSignals(stg);
            for (SignalTransition transition: StgUtils.getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(StgUtils.getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String comment = null;
                    String signalList = ReferenceHelper.getReferencesAsString(nonpersistentLocalSignals);
                    if (nonpersistentLocalSignals.size() > 1) {
                        comment = "Non-persistent signals " + signalList;
                    } else {
                        comment = "Non-persistent signal '" + signalList + "'";
                    }
                    LogUtils.logMessage(comment + " after projected trace: " + trace);
                    trace.add(stg.getNodeReference(transition));
                    result = new MpsatSolution(trace, null, comment);
                    break;
                }
                stg.unFire(transition);
            }
        }
        PetriUtils.setMarking(stg, marking);
        if (result == null) {
            LogUtils.logMessage("No non-persistent signals detected after projected trace: " + trace);
        }
        return result;
    }

}
