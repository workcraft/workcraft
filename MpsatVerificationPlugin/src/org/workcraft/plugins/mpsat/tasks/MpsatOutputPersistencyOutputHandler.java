package org.workcraft.plugins.mpsat.tasks;

import java.util.HashSet;

import org.workcraft.Trace;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatOutputPersistencyOutputHandler extends MpsatReachabilityOutputHandler {

    MpsatOutputPersistencyOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        super(we, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public MpsatSolution processSolution(MpsatSolution solution) {
        StgModel stg = getSrcStg();
        if ((solution == null) || (stg == null)) {
            return solution;
        }
        Trace trace = getProjectedTrace(solution.getMainTrace());
        LogUtils.logMessage("Processing output percistency violation trace: ");
        LogUtils.logMessage("  reported: " + solution.getMainTrace());
        LogUtils.logMessage("  projected: " + trace);
        String comment = null;
        if (!StgUtils.fireTrace(stg, trace)) {
            LogUtils.logWarning("Cannot execute projected output persistency violation trace: " + trace);
        } else {
            HashSet<String> enabledLocalSignals = getEnabledLocalSignals(stg);
            for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String signalList = ReferenceHelper.getReferencesAsString(nonpersistentLocalSignals);
                    if (nonpersistentLocalSignals.size() > 1) {
                        solution.setComment("Non-persistent signals " + signalList);
                    } else {
                        solution.setComment("Non-persistent signal '" + signalList + "'");
                    }
                    trace.add(stg.getNodeReference(transition));
                    LogUtils.logMessage("  extended: " + trace);
                    break;
                }
                stg.unFire(transition);
            }
        }
        return new MpsatSolution(trace, null, comment);
    }

    private HashSet<SignalTransition> getEnabledSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (SignalTransition transition: stg.getSignalTransitions()) {
            if (stg.isEnabled(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    private HashSet<String> getEnabledLocalSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
            if ((transition.getSignalType() == Type.OUTPUT) || (transition.getSignalType() == Type.INTERNAL)) {
                result.add(transition.getSignalName());
            }
        }
        return result;
    }

}
