package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.tasks.ExtendedExportOutput;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Triple;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.TextUtils;

import java.io.File;
import java.util.*;

public class CompositionUtils {

    public static void applyExportSubstitutions(CompositionData compositionData, ExportOutput exportOutput) {

        if (exportOutput instanceof ExtendedExportOutput) {
            ExtendedExportOutput extendedExportOutput = (ExtendedExportOutput) exportOutput;
            for (File file : extendedExportOutput.getFiles()) {
                Map<String, String> substitutions = extendedExportOutput.getSubstitutions(file);
                applyComponentSubstitutions(compositionData, file, substitutions);
            }
        }
    }

    public static void applyComponentSubstitutions(CompositionData compositionData, File file,
            Map<String, String> substitutions) {

        ComponentData componentData = compositionData.getComponentData(file);
        if (componentData != null) {
            componentData.substituteSrcTransitions(substitutions);
        }
    }

    public static Trace projectTrace(Trace trace, ComponentData componentData) {
        if ((trace == null) || trace.isEmpty() || (componentData == null)) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref : trace) {
            String srcRef = componentData.getSrcTransition(ref);
            if (srcRef != null) {
                result.add(srcRef);
            }
        }
        return result;
    }

    public static Enabledness getEnabledness(Collection<Trace> compositionContinuations, ComponentData componentData) {
        Enabledness result = new Enabledness();
        for (Trace compositionContinuation : compositionContinuations) {
            Trace projectedContinuation = projectTrace(compositionContinuation, componentData);
            if ((projectedContinuation != null) && !projectedContinuation.isEmpty()) {
                int lastIndex = projectedContinuation.size() - 1;
                String ref = projectedContinuation.get(lastIndex);
                Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
                String event = r.getFirst() + r.getSecond();
                projectedContinuation.remove(lastIndex);
                result.put(event, projectedContinuation);
            }
        }
        return result;
    }

    public static List<Solution> getDisabledViolatorSolutions(Trace trace, Set<String> disabledViolators) {
        return getViolatorSolutions(trace, disabledViolators, "Unexpected disabling of signal");
    }

    public static List<Solution> getViolatorSolutions(Trace trace, Set<String> violator, String message) {
        List<Solution> result = new LinkedList<>();
        if (!violator.isEmpty()) {
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, violator));
            String comment = TextUtils.wrapMessageWithItems(message, violator);
            result.add(new Solution(trace, null, comment));
        }
        return result;
    }

    public static List<Solution> getEnabledViolatorSolutions(Trace trace, Set<String> enabledViolators,
            Enabledness enabledness) {

        return getExtendedViolatorSolutions(trace, enabledViolators, enabledness, "Unexpected enabling of signal");
    }

    public static List<Solution> getExtendedViolatorSolutions(Trace trace, Set<String> violators,
            Enabledness enabledness, String message) {

        List<Solution> result = new LinkedList<>();
        if (!violators.isEmpty()) {
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, violators));
        }

        Map<Trace, Set<String>> continuationToEventsMap = new HashMap<>();
        for (String event : violators) {
            Trace continuation = enabledness.get(event);
            if (continuation != null) {
                Set<String> events = continuationToEventsMap.computeIfAbsent(continuation, HashSet::new);
                events.add(event);
            }
        }

        for (Trace continuation : continuationToEventsMap.keySet()) {
            Trace violationTrace = new Trace(trace);
            violationTrace.addAll(continuation);
            Set<String> events = continuationToEventsMap.get(continuation);
            String comment = TextUtils.wrapMessageWithItems(message, events);
            result.add(new Solution(violationTrace, null, comment));
        }
        return result;
    }

}
