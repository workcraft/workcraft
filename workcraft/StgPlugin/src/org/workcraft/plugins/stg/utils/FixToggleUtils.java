package org.workcraft.plugins.stg.utils;

import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;

import java.util.Set;

public class FixToggleUtils {

    public static Solution fixSolutionToggleEvents(StgModel stg, Solution solution) {
        Trace mainTrace = fixTraceToggleEvents(stg, solution.getMainTrace());
        Trace branchTrace = fixTraceToggleEvents(stg, solution.getBranchTrace());
        Solution result = new Solution(mainTrace, branchTrace, solution.getComment());
        for (Trace continuation : solution.getContinuations()) {
            result.addContinuation(fixTraceToggleEvents(stg, continuation));
        }
        return result;
    }

    public static Trace fixTraceToggleEvents(StgModel stg, Trace trace) {
        if ((trace == null) || trace.isEmpty()) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref : trace) {
            String fixedRef = fixToggleEvent(stg, ref);
            if (fixedRef != null) {
                result.add(fixedRef);
            }
        }
        return result;
    }

    private static String fixToggleEvent(StgModel stg, String ref) {
        if (stg.getNodeByReference(ref) != null) {
            return ref;
        }
        Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
        if (r != null) {
            String fixedRef = LabelParser.getSignalTransitionReference(r);
            if (stg.getNodeByReference(fixedRef) != null) {
                return fixedRef;
            }
        }
        return null;
    }

    public static String fixToggleIfSignalTransition(String transitionRef, Set<String> signals) {
        Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(transitionRef);
        if ((r != null) && signals.contains(r.getFirst())) {
            return LabelParser.getSignalTransitionReference(r);
        }
        return transitionRef;
    }

    public static String fixToggleIfImplicitPlace(String placeRef, Set<String> signals) {
        Pair<String, String> r = LabelParser.parseImplicitPlace(placeRef);
        if (r != null) {
            String fromTransitionRef = fixToggleIfSignalTransition(r.getFirst(), signals);
            String toTransitionRef = fixToggleIfSignalTransition(r.getSecond(), signals);
            return LabelParser.getImplicitPlaceReference(fromTransitionRef, toTransitionRef);
        }
        return placeRef;
    }

}
