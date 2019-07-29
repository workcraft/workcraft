package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.utils.EnablednessUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.types.Triple;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

class ConformationOutputHandler extends ReachabilityOutputHandler {

    ConformationOutputHandler(WorkspaceEntry we, ExportOutput exportOutput,
            PcompOutput pcompOutput, VerificationOutput mpsatOutput, VerificationParameters settings) {

        super(we, exportOutput, pcompOutput, mpsatOutput, settings);
    }

    @Override
    public void reportSolutions(List<Solution> solutions) {
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Violation traces of the composition:");
        }
        StgModel compStg = getMpsatOutput().getInputStg();
        for (Solution solution: solutions) {
            // FIXME: This is to rename toggle events from x to x~
            Trace compTrace = fixTraceToggleEvents(compStg, solution.getMainTrace());
            if (needsMultiLineMessage) {
                LogUtils.logMessage("  " + compTrace.toText());
            } else {
                LogUtils.logMessage("Violation trace of the composition: " + compTrace.toText());
            }
        }
    }

    @Override
    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();

        StgModel stg = getSrcStg(we);

        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + we.getTitle() + "':");
        }

        StgModel compStg = getMpsatOutput().getInputStg();
        for (Solution solution : solutions) {
            // Get unique projection trace
            Trace trace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            String traceText = trace.toText();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + we.getTitle() + "': " + traceText);
                }
                // FIXME: This is to rename toggle events from x to x~
                Trace compTrace = fixTraceToggleEvents(compStg, solution.getMainTrace());
                Enabledness compEnabledness = EnablednessUtils.getOutputEnablednessAfterTrace(compStg, compTrace);
                Solution processedSolution = processSolution(stg, trace, compEnabledness);
                if (processedSolution != null) {
                    result.add(processedSolution);
                }
            }
        }
        return result;
    }

    private Trace fixTraceToggleEvents(StgModel stg, Trace trace) {
        Trace result = new Trace();
        for (String ref : trace) {
            if (stg.getNodeByReference(ref) == null) {
                Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
                if (r != null) {
                    String newRef = r.getFirst() + r.getSecond();
                    if (r.getThird() != null) {
                        newRef += "/" + r.getThird();
                    }
                    if (stg.getNodeByReference(newRef) != null) {
                        ref = newRef;
                    }
                }
            }
            result.add(ref);
        }
        return result;
    }

    private Solution processSolution(StgModel stg, Trace trace, Enabledness compEnabledness) {
        // Execute trace to a potentially problematic state
        HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
        if (!PetriUtils.fireTrace(stg, trace)) {
            PetriUtils.setMarking(stg, marking);
            throw new RuntimeException("Cannot execute projected trace: " + trace.toText());
        }
        // Check if any output can be fired that is not enabled in the composition STG

        // Find enabled signals whose state is unknown (due to dummies) in the composition STG.
        // If there is only one such signal, then it is actually the one disabled in the composition STG.
        HashSet<String> suspiciousSignals = EnablednessUtils.getEnabledSignals(stg, Signal.Type.OUTPUT);
        suspiciousSignals.retainAll(compEnabledness.getUnknownSet());
        if (suspiciousSignals.size() == 1) {
            compEnabledness.alter(Collections.emptySet(), suspiciousSignals, Collections.emptySet());
        }
        // Find the first enabled transition that is definitely disabled in composition STG.
        SignalTransition problematicTransition = null;
        for (SignalTransition transition: stg.getSignalTransitions(Signal.Type.OUTPUT)) {
            String signalRef = stg.getSignalReference(transition);
            if (stg.isEnabled(transition) && compEnabledness.isDisabled(signalRef)) {
                problematicTransition = transition;
                break;
            }
        }
        // If problematic transition found, add it to the trace. Otherwise add suspicious signals to the trace description.
        Solution processedSolution = null;
        if (problematicTransition != null) {
            String ref = stg.getSignalReference(problematicTransition) + problematicTransition.getDirection();
            LogUtils.logWarning("Output '" + ref + "' becomes unexpectedly enabled");
            trace.add(stg.getNodeReference(problematicTransition));
            String comment = "Unexpected change of output '" + ref + "'";
            processedSolution = new Solution(trace, null, comment);
        } else if (!suspiciousSignals.isEmpty()) {
            String refs = String.join(", ", suspiciousSignals);
            LogUtils.logWarning("One of these outputs becomes unexpectedly enabled (via internal signals or dummies):\n" + refs);
            String comment = "Unexpected change of one of the outputs: " + refs;
            processedSolution = new Solution(trace, null, comment);
        }
        PetriUtils.setMarking(stg, marking);
        return processedSolution;
    }

}
