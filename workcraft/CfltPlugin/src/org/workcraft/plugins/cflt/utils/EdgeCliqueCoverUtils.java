package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;
import org.workcraft.plugins.cflt.algorithms.MaxMinHeuristic;
import org.workcraft.plugins.cflt.algorithms.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class EdgeCliqueCoverUtils {

    private EdgeCliqueCoverUtils() {
    }

    public static ArrayList<ArrayList<String>> getEdgeCliqueCover(boolean isSequence, Mode mode, Graph inputG, Graph outputG) {
        Graph initGraph = isSequence ? GraphUtils.join(inputG, outputG) : inputG;
        ArrayList<Edge> optionalEdges = isSequence ? inputG.getEdges() : new ArrayList<>();
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

    public static HashMap<String, HashSet<String>> initialiseNeighbours(Graph graph) {
        HashMap<String, HashSet<String>> allNeighbours = new HashMap<>();
        for (Edge edge : graph.getEdges()) {
            if (!allNeighbours.containsKey(edge.getFirstVertex())) {
                allNeighbours.put(edge.getFirstVertex(), new HashSet<>());
            }
            if (!allNeighbours.containsKey(edge.getSecondVertex())) {
                allNeighbours.put(edge.getSecondVertex(), new HashSet<>());
            }
            allNeighbours.get(edge.getFirstVertex()).add(edge.getSecondVertex());
            allNeighbours.get(edge.getSecondVertex()).add(edge.getFirstVertex());
        }
        return allNeighbours;
    }

    /**
     * This function eliminates redundant cliques in an edge clique cover
     * by checking if the clique consists only of edges contained in other cliques.
     */
    public static boolean checkRedundancy(HashMap<String, Integer> edgeToNoOfCliquesItsContainedIn,
                            ArrayList<String> cliqueAsEdges) {
        for (String edge : cliqueAsEdges) {
            if (edgeToNoOfCliquesItsContainedIn.get(edge) == 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return a vertex 'u' with the lowest uncovered degree in the local neighbourhood
     */
    public static String argMin(HashMap<String, Integer> uncoveredDegree, HashSet<String> uncoveredVertices) {
        int arg = 2147483646;
        String u = "";
        for (String str : uncoveredVertices) {
            if (uncoveredDegree.get(str) < arg) {
                arg = uncoveredDegree.get(str);
                u = str;
            }
        }
        return u;
    }

    /**
     * @return a vertex 'u' with the highest uncovered degree in the local neighbourhood
     */
    public static String argMax(HashMap<String, Integer> uncoveredDegree, HashSet<String> uncoveredVertices) {
        int arg = -1;
        String u = "";
        for (String str : uncoveredVertices) {
            if (uncoveredDegree.get(str) > arg) {
                arg = uncoveredDegree.get(str);
                u = str;
            }
        }
        return u;
    }

}
