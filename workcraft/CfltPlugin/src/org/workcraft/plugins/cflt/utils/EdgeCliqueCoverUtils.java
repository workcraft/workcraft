package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;
import org.workcraft.plugins.cflt.algorithms.MaxMinHeuristic;
import org.workcraft.plugins.cflt.algorithms.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

public final class EdgeCliqueCoverUtils {

    private EdgeCliqueCoverUtils() {
    }

    public static List<List<String>> getEdgeCliqueCover(boolean isSequence, Mode mode, Graph inputG, Graph outputG) {
        Graph initGraph = isSequence ? GraphUtils.join(inputG, outputG) : inputG;
        List<Edge> optionalEdges = isSequence ? inputG.getEdges() : new ArrayList<>();
        switch (mode) {
        case SLOW_EXACT:
            return ExhaustiveSearch.getEdgeCliqueCover(initGraph, optionalEdges);
        case FAST_SEQ:
            return SequenceHeuristic.getEdgeCliqueCover(initGraph, optionalEdges);
        case FAST_MAX:
            return MaxMinHeuristic.getEdgeCliqueCover(initGraph, optionalEdges, true);
        case FAST_MIN:
            return MaxMinHeuristic.getEdgeCliqueCover(initGraph, optionalEdges, false);
        }
        return new ArrayList<>();
    }

    public static Map<String, HashSet<String>> initialiseNeighbours(Graph graph) {
        Map<String, HashSet<String>> allNeighbours = new HashMap<>();
        for (Edge edge : graph.getEdges()) {
            allNeighbours.putIfAbsent(edge.getFirstVertex(), new HashSet<>());
            allNeighbours.putIfAbsent(edge.getSecondVertex(), new HashSet<>());
            allNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            allNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
        }
        return allNeighbours;
    }

    public static boolean isCliqueRedundant(Map<String, Integer> edgeToNoOfCliquesItsContainedIn, List<String> cliqueAsEdges) {
        return cliqueAsEdges.stream()
                .allMatch(edge -> edgeToNoOfCliquesItsContainedIn.getOrDefault(edge, 0) > 1);
    }

    public static String argMin(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .min(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }

    public static String argMax(Map<String, Integer> uncoveredDegree, Set<String> uncoveredVertices) {
        return uncoveredVertices.stream()
                .max(Comparator.comparingInt(uncoveredDegree::get))
                .orElse(null);
    }
}
