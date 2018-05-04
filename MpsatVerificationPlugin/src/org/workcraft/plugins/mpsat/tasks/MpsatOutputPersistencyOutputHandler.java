package org.workcraft.plugins.mpsat.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.references.ReferenceHelper;
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
        ComponentData data = getCompositionData(0);
        StgModel stg = getSrcStg(data);
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        for (MpsatSolution solution: solutions) {
            if ((solution == null) || (stg == null)) {
                result.add(solution);
            }
            Trace trace = solution.getMainTrace();
            String traceText = trace.toText();
            LogUtils.logMessage("Violation trace: " + traceText);
            Trace projectedTrace = getProjectedTrace(trace, data);
            if (!traceText.equals(projectedTrace.toText())) {
                LogUtils.logMessage("Projection trace: " + projectedTrace.toText());
            }
            if (!PetriUtils.fireTrace(stg, projectedTrace)) {
                PetriUtils.setMarking(stg, marking);
                throw new RuntimeException("Cannot execute trace: " + projectedTrace.toText());
            }
            // Check if any local signal gets disabled by firing other signal event
            HashSet<String> enabledLocalSignals = StgUtils.getEnabledLocalSignals(stg);
            for (SignalTransition transition: StgUtils.getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(StgUtils.getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String signalList = ReferenceHelper.getReferencesAsString(nonpersistentLocalSignals);
                    String comment = "Non-persistent signal(s) " + signalList;
                    String ref = stg.getNodeReference(transition);
                    LogUtils.logWarning("Event '" + ref + "' disables signal(s) " + signalList);
                    Trace processedTrace = new Trace(projectedTrace);
                    processedTrace.add(ref);
                    MpsatSolution processedSolution = new MpsatSolution(processedTrace, null, comment);
                    result.add(processedSolution);
                }
                stg.unFire(transition);
            }
            PetriUtils.setMarking(stg, marking);
        }
        return result;
    }

}
