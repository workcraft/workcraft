package org.workcraft.plugins.mpsat_verification.utils;

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

    public static Map<String, Trace> getEnabledness(Collection<Trace> compositionContinuations, ComponentData componentData) {
        Map<String, Trace> result = new HashMap<>();
        for (Trace compositionContinuation : compositionContinuations) {
            Trace projectedContinuation = projectContinuation(compositionContinuation, componentData);
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

    private static Trace projectContinuation(Trace continuation, ComponentData data) {
        if ((continuation == null) || continuation.isEmpty() || (data == null)) {
            return continuation;
        }
        Trace result = new Trace();
        for (String ref : continuation) {
            String srcRef = data.getSrcTransition(ref);
            if (srcRef == null) {
                return new Trace();
            }
            result.add(srcRef);
        }
        return result;
    }

    public static List<Solution> extendTraceToViolations(Trace trace, Map<String, Trace> enabledness,
            Set<String> violationEvents, String message) {

        List<Solution> result = new LinkedList<>();
        if (!violationEvents.isEmpty()) {
            LogUtils.logWarning(TextUtils.wrapMessageWithItems(message, violationEvents));
        }

        Map<Trace, Set<String>> continuationToEventsMap = new HashMap<>();
        for (String event : violationEvents) {
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
