package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.ecc.ExhaustiveSearch;
import org.workcraft.plugins.cflt.ecc.MaxMinHeuristic;
import org.workcraft.plugins.cflt.ecc.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class EccUtils {

    private EccUtils() {
    }

    public static ArrayList<ArrayList<String>> getEcc(boolean isSequence, Mode mode, Graph inputG, Graph outputG) {
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

    public static HashMap<String, HashSet<String>> initialiseNeighbours(Graph g) {
        HashMap<String, HashSet<String>> allNeighbours = new HashMap<>();
        // Initialising neighbours
        for (Edge e : g.getEdges()) {
            if (allNeighbours.containsKey(e.getFirstVertex())) {
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

            } else {
                allNeighbours.put(e.getFirstVertex(), new HashSet<>());
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

            }
            if (allNeighbours.containsKey(e.getSecondVertex())) {
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());

            } else {
                allNeighbours.put(e.getSecondVertex(), new HashSet<>());
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());
            }
        }
        return allNeighbours;
    }

    /**
     * This function eliminates redundant cliques in an edge clique cover
     * by checking if the clique consists only of edges contained in other cliques.
     * @param noOfCliqueAnEdgeIsContainedIn, the number of cliques an edge is contained in
     * @param cliqueAsEdges clique
     * @return clique is redundancy
     */
    public static boolean checkRedundancy(HashMap<String, Integer> noOfCliqueAnEdgeIsContainedIn,
                            ArrayList<String> cliqueAsEdges) {

        for (String edge : cliqueAsEdges) {
            //if the edge is only contained in this clique
            if (noOfCliqueAnEdgeIsContainedIn.get(edge) == 1) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param uncoveredDegree uncovered degree
     * @param uncoveredVertices uncovered vertices
     * @return a vertex 'u' with the lowest uncovered degree in the local neighbourhood
     */
    public static String argMin(HashMap<String, Integer> uncoveredDegree, HashSet<String> uncoveredVertices) {
        int arg = 1000;
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
     *
     * @param uncoveredDegree uncovered degree
     * @param uncoveredVertices uncovered vertices
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
