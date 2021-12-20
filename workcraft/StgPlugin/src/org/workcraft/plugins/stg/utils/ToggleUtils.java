package org.workcraft.plugins.stg.utils;

import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ToggleUtils {

    public static List<Solution> toggleSignalTransitions(List<Solution> solutions, Set<String> signals) {
        List<Solution> result = new LinkedList<>();
        for (Solution solution : solutions) {
            result.add(ToggleUtils.toggleSignalTransitions(solution, signals));
        }
        return result;
    }

    public static Solution toggleSignalTransitions(Solution solution, Set<String> signals) {
        Trace mainTrace = toggleSignalTransitions(solution.getMainTrace(), signals);
        Trace branchTrace = toggleSignalTransitions(solution.getBranchTrace(), signals);
        Solution result = new Solution(mainTrace, branchTrace, solution.getComment());
        for (Trace continuation : solution.getContinuations()) {
            result.addContinuation(toggleSignalTransitions(continuation, signals));
        }
        return result;
    }

    public static Trace toggleSignalTransitions(Trace trace, Set<String> signals) {
        if ((trace == null) || trace.isEmpty()) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref : trace) {
            result.add(toggleIfSignalTransition(ref, signals));
        }
        return result;
    }

    public static String toggleIfSignalTransition(String transitionRef, Set<String> signals) {
        Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(transitionRef);
        if ((r != null) && signals.contains(r.getFirst())) {
            return LabelParser.getSignalTransitionReference(r);
        }
        return transitionRef;
    }

    public static String toggleIfImplicitPlace(String placeRef, Set<String> signals) {
        Pair<String, String> r = LabelParser.parseImplicitPlace(placeRef);
        if (r != null) {
            String fromTransitionRef = toggleIfSignalTransition(r.getFirst(), signals);
            String toTransitionRef = toggleIfSignalTransition(r.getSecond(), signals);
            return LabelParser.getImplicitPlaceReference(fromTransitionRef, toTransitionRef);
        }
        return placeRef;
    }

}
