package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class OutputDeterminacyOutputInterpreter extends AbstractCompositionOutputInterpreter {

    OutputDeterminacyOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(List<Solution> solutions) {
        CompositionData compositionData = getCompositionData();
        ComponentData devData = compositionData.getComponentData(0);
        ComponentData envData = compositionData.getComponentData(1);

        List<Solution> result = new LinkedList<>();
        for (Solution solution : solutions) {
            Trace compositionTrace = solution.getMainTrace();
            LogUtils.logMessage("Violation trace of the auto-composition: " + compositionTrace);

            Trace devTrace = CompositionUtils.projectTrace(compositionTrace, devData);
            Trace envTrace = CompositionUtils.projectTrace(compositionTrace, envData);
            LogUtils.logMessage("Projected pair of traces:\n    " + devTrace + "\n    " + envTrace);

            Set<Trace> compositionContinuations = solution.getContinuations();
            Enabledness devEnabledness = CompositionUtils.getEnabledness(compositionContinuations, devData);
            Enabledness envEnabledness = CompositionUtils.getEnabledness(compositionContinuations, envData);
            Set<String> nondeterministicEnabledEvents = new HashSet<>(devEnabledness.keySet());
            nondeterministicEnabledEvents.removeAll(envEnabledness.keySet());

            result.addAll(CompositionUtils.getExtendedViolatorSolutions(devTrace, nondeterministicEnabledEvents,
                    devEnabledness, "Non-deterministic enabling of signal"));
        }
        return result;
    }

}
