package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.mpsat_verification.utils.EnablednessUtils;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
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
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class RefinementOutputInterpreter extends ConformationOutputInterpreter {

    private static final int IMPLEMENTATION_INDEX = 0;
    private static final int SPECIFICATION_INDEX = 1;

    RefinementOutputInterpreter(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(we, exportOutput, pcompOutput, mpsatOutput, interactive);
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();

        StgModel compositionStg = getOutput().getInputStg();

        ComponentData implementationData = getCompositionData(IMPLEMENTATION_INDEX);
        StgModel implementationStg = getSrcStg(IMPLEMENTATION_INDEX);

        ComponentData specificationData = getCompositionData(SPECIFICATION_INDEX);
        StgModel specificationStg = getSrcStg(SPECIFICATION_INDEX);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + we.getTitle() + "':");
        }

        for (Solution solution : solutions) {
            // Get unique projection trace
            Trace compositionTrace = MpsatUtils.fixTraceToggleEvents(compositionStg, solution.getMainTrace());
            Trace implementationTrace = getProjectedTrace(compositionTrace, implementationData, Collections.emptyMap());
            String traceText = implementationTrace.toString();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                Trace specificationTrace = getProjectedTrace(compositionTrace, specificationData, Collections.emptyMap());
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + we.getTitle() + "': " + traceText);
                }
                Enabledness specificationEnabledness = EnablednessUtils.getEnablednessAfterTrace(specificationStg, specificationTrace);
                Solution processedSolution = processSolution(implementationStg, implementationTrace, specificationEnabledness);
                if (processedSolution != null) {
                    result.add(processedSolution);
                }
            }
        }
        return result;
    }

    @Override
    public Solution processSolution(StgModel stg, Trace trace, Enabledness specificationEnabledness) {
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute trace: " + trace.toString());
        }

        SignalTransition unexpectedlyEnabledOutputTransition = null;
        Set<SignalTransition> possiblyEnabledOutputTransitions = new HashSet<>();
        for (SignalTransition outputTransition : stg.getSignalTransitions(Signal.Type.OUTPUT)) {
            String outputRef = stg.getSignalReference(outputTransition);
            if (stg.isEnabled(outputTransition)) {
                if (specificationEnabledness.isDisabled(outputRef)) {
                    unexpectedlyEnabledOutputTransition = outputTransition;
                    break;
                } else if (!specificationEnabledness.isEnabled(outputRef)) {
                    possiblyEnabledOutputTransitions.add(outputTransition);
                }
            }
        }

        Set<String> possiblyUnexpectedlyDisabledSignals = stg.getSignalReferences();
        possiblyUnexpectedlyDisabledSignals.removeAll(EnablednessUtils.getEnabledSignals(stg, null));
        possiblyUnexpectedlyDisabledSignals.removeAll(specificationEnabledness.getDisabledSet());
        possiblyUnexpectedlyDisabledSignals.retainAll(specificationEnabledness.getEnabledSet());

        if (possiblyUnexpectedlyDisabledSignals.isEmpty() && (possiblyEnabledOutputTransitions.size() == 1)) {
            unexpectedlyEnabledOutputTransition = possiblyEnabledOutputTransitions.iterator().next();
        }

        String comment = null;
        if (unexpectedlyEnabledOutputTransition != null) {
            // If an unexpectedly enabled output transition found then add it to the trace
            trace.add(stg.getNodeReference(unexpectedlyEnabledOutputTransition));
            String transitionLabel = stg.getSignalReference(unexpectedlyEnabledOutputTransition)
                    + unexpectedlyEnabledOutputTransition.getDirection();

            comment = "Unexpected change of output '" + transitionLabel + "'";
            LogUtils.logWarning("Output '" + transitionLabel + "' becomes unexpectedly enabled");
        } else {
            // Otherwise suggest unexpectedly enabled and/or unexpectedly disabled signals
            Set<String> possiblyUnexpectedlyEnabledSignals = stg.getSignalReferences();
            possiblyUnexpectedlyEnabledSignals.removeAll(EnablednessUtils.getDisabledSignals(stg, null));
            possiblyUnexpectedlyEnabledSignals.removeAll(specificationEnabledness.getEnabledSet());
            possiblyUnexpectedlyEnabledSignals.retainAll(specificationEnabledness.getDisabledSet());

            String enabledRefs = String.join(", ", possiblyUnexpectedlyEnabledSignals);
            String disabledRefs = String.join(", ", possiblyUnexpectedlyDisabledSignals);

            if (!possiblyUnexpectedlyDisabledSignals.isEmpty() && !possiblyUnexpectedlyEnabledSignals.isEmpty()) {
                String refs = "{" + enabledRefs + "} / {" + disabledRefs + "}";
                comment = "Unexpected enabling / disabling of one of the signals " + refs;
                LogUtils.logWarning("One of these signals becomes unexpectedly enabled / disabled:\n" + refs);
            } else if (!possiblyUnexpectedlyDisabledSignals.isEmpty()) {
                if (possiblyUnexpectedlyDisabledSignals.size() == 1) {
                    comment = "Unexpected disabling of signal '" + disabledRefs + "'";
                    LogUtils.logWarning("Signal '" + disabledRefs + "' becomes unexpectedly disabled");
                } else {
                    comment = "Unexpected disabling of one of the signals {" + disabledRefs + "}";
                    LogUtils.logWarning("One of these signals becomes unexpectedly disabled:\n" + disabledRefs);
                }
            } else if (!possiblyUnexpectedlyEnabledSignals.isEmpty()) {
                if (possiblyUnexpectedlyEnabledSignals.size() == 1) {
                    comment = "Unexpected enabling of signal '" + enabledRefs + "'";
                    LogUtils.logWarning("Signal '" + enabledRefs + "' becomes unexpectedly enabled"
                            + " (via internal signals or dummies)");
                } else {
                    comment = "Unexpected enabling of one of the signals {" + enabledRefs + "}";
                    LogUtils.logWarning("One of these signals becomes unexpectedly enabled"
                            + " (via internal signals or dummies):\n" + enabledRefs);
                }
            }
        }

        PetriUtils.setMarking(stg, marking);
        Map<String, String> substitutions = getSubstitutions(null);
        return new Solution(getSubstitutedTrace(trace, substitutions), null, comment);
    }

}
