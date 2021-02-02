package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class ConformationOutputInterpreter extends AbstractCompositionOutputInterpreter {

    public ConformationOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(List<Solution> solutions) {
        String title = getWorkspaceEntry().getTitle();
        StgModel compositionStg = getCompositionStg();
        CompositionData compositionData = getCompositionData();
        ComponentData devData = compositionData.getComponentData(0);
        ComponentData envData = compositionData.getComponentData(1);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + title + "':");
        }

        Set<String> outputSignals = compositionStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> outputEvents = StgUtils.getAllEvents(outputSignals);

        List<Solution> result = new LinkedList<>();
        for (Solution solution : solutions) {
            // Get unique projection trace
            Solution compositionSolution = MpsatUtils.fixSolutionToggleEvents(compositionStg, solution);
            Trace devTrace = CompositionUtils.projectTrace(compositionSolution.getMainTrace(), devData);
            String traceText = devTrace.toString();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + title + "': " + traceText);
                }

                Set<Trace> compositionContinuations = compositionSolution.getContinuations();
                Enabledness devEnabledness = CompositionUtils.getEnabledness(compositionContinuations, devData);
                Enabledness envEnabledness = CompositionUtils.getEnabledness(compositionContinuations, envData);

                Set<String> unexpectedlyEnabledOutputEvents = new HashSet<>(outputEvents);
                unexpectedlyEnabledOutputEvents.retainAll(devEnabledness.keySet());
                unexpectedlyEnabledOutputEvents.removeAll(envEnabledness.keySet());
                result.addAll(CompositionUtils.getEnabledViolatorSolutions(devTrace, unexpectedlyEnabledOutputEvents,
                        devEnabledness));
            }
        }
        return result;
    }

}
