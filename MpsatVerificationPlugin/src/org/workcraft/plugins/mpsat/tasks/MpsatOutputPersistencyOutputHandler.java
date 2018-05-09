package org.workcraft.plugins.mpsat.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.gui.graph.tools.Trace;
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
    public List<MpsatSolution> processSolutions(WorkspaceEntry we, List<MpsatSolution> solutions) {
        List<MpsatSolution> result = new LinkedList<>();
        ComponentData data = getCompositionData(we);
        StgModel stg = getSrcStg(data);
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        for (MpsatSolution solution: solutions) {
            if ((solution == null) || (stg == null)) {
                result.add(solution);
            }
            Trace trace = solution.getMainTrace();
            LogUtils.logMessage("Violation trace: " + trace.toText());
            if (data != null) {
                trace = getProjectedTrace(trace, data);
                LogUtils.logMessage("Projection trace: " + trace.toText());
            }
            if (!PetriUtils.fireTrace(stg, trace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute trace: " + trace.toText());
            }
            // Check if any local signal gets disabled by firing other signal event
            HashSet<String> enabledLocalSignals = StgUtils.getEnabledLocalSignals(stg);
            for (SignalTransition transition: StgUtils.getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(StgUtils.getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String comment = getMessageWithList("Non-persistent signal", nonpersistentLocalSignals);
                    String transitionRef = stg.getNodeReference(transition);
                    String msg = getMessageWithList("Event '" + transitionRef + "' disables signal", nonpersistentLocalSignals);
                    LogUtils.logWarning(msg);
                    Trace processedTrace = new Trace(trace);
                    processedTrace.add(transitionRef);
                    MpsatSolution processedSolution = new MpsatSolution(processedTrace, null, comment);
                    result.add(processedSolution);
                }
                stg.unFire(transition);
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

    private String getMessageWithList(String message, Collection<String> refs) {
        if ((refs == null) || refs.isEmpty()) {
            return message;
        } else if (refs.size() == 1) {
            return message + " '" + refs.iterator().next() + "'";
        } else {
            return message + "s: " + String.join(", ", refs);
        }
    }

}
