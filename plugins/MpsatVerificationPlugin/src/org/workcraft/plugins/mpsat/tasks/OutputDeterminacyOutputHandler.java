package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.utils.WorkUtils;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.EnablednessUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class OutputDeterminacyOutputHandler extends ReachabilityOutputHandler {

    OutputDeterminacyOutputHandler(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, VerificationOutput mpsatOutput, VerificationParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();

        StgModel compStg = getMpsatOutput().getInputStg();
        StgModel stg = getSrcStg(we);
        ComponentData devData = getCompositionData(0);
        ComponentData envData = getCompositionData(1);
        Map<String, String> substitutions = getSubstitutions(we);

        for (Solution solution: solutions) {
            Trace compTrace = solution.getMainTrace();
            LogUtils.logMessage("Violation trace of the auto-composition: " + compTrace.toString());

            Trace devTrace = getProjectedTrace(compTrace, devData, substitutions);
            Trace envTrace = getProjectedTrace(compTrace, envData, substitutions);
            LogUtils.logMessage("Projected pair of traces:\n    " + devTrace.toString() + "\n    " + envTrace.toString());

            Enabledness compEnabledness = EnablednessUtils.getOutputEnablednessAfterTrace(compStg, compTrace);
            Solution projectedSolution = new Solution(devTrace, envTrace);
            Solution processedSolution = processSolution(stg, projectedSolution, compEnabledness);
            if (processedSolution != null) {
                result.add(processedSolution);
            }
        }
        return result;
    }

    private Solution processSolution(StgModel stg, Solution solution, Enabledness compEnabledness) {
        // Execute trace to potentially interesting state
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        Trace trace = solution.getMainTrace();
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute projected trace: " + trace.toString());
        }
        // Check if any output can be fired that is not enabled in the composition
        HashSet<String> suspiciousSignals = EnablednessUtils.getEnabledSignals(stg, Signal.Type.OUTPUT);
        suspiciousSignals.retainAll(compEnabledness.getUnknownSet());
        if (suspiciousSignals.size() == 1) {
            compEnabledness.alter(Collections.emptySet(), suspiciousSignals, Collections.emptySet());
        }

        SignalTransition problematicTransition = null;
        for (SignalTransition transition: stg.getSignalTransitions(Signal.Type.OUTPUT)) {
            String signalRef = stg.getSignalReference(transition);
            if (stg.isEnabled(transition) && compEnabledness.isDisabled(signalRef)) {
                problematicTransition = transition;
                break;
            }
        }
        String comment = null;
        if (problematicTransition != null) {
            String ref = stg.getSignalReference(problematicTransition);
            LogUtils.logWarning("Output '" + ref + "' is non-deterministically enabled");
            comment = "Non-deterministic enabling of output '" + ref + "'";
        } else if (!suspiciousSignals.isEmpty()) {
            String refs = String.join(", ", suspiciousSignals);
            LogUtils.logWarning("One of these outputs is non-deterministically enabled (via internal signals or dummies):\n" + refs);
            comment = "Non-deterministic enabling of one of the outputs: " + refs;
        }
        PetriUtils.setMarking(stg, marking);
        return new Solution(solution.getMainTrace(), solution.getBranchTrace(), comment);
    }

    @Override
    public StgModel getSrcStg(WorkspaceEntry we) {
        ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
        return WorkspaceUtils.getAs(me, StgModel.class);
    }

}
