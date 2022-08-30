package org.workcraft.plugins.cflt.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.ecc.ExhaustiveSearch;
import org.workcraft.plugins.cflt.ecc.MaxMinHeuristic;
import org.workcraft.plugins.cflt.ecc.SequenceHeuristic;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

public class EccUtils {

    public static ArrayList<ArrayList<String>> getEcc(boolean isSequence, String mode, Graph inputG, Graph outputG) {
        ArrayList<ArrayList<String>> edgeCliqueCover = new ArrayList<>();
        if (!isSequence) {
            if (mode.equals(Mode.SLOW_EXACT.toString())) {
                edgeCliqueCover = ExhaustiveSearch.getEdgeCliqueCover(inputG, null);
            } else if (mode.equals(Mode.FAST_SEQ.toString())) {
                edgeCliqueCover = SequenceHeuristic.getEdgeCliqueCover(inputG, new ArrayList<Edge>());
            } else if (mode.equals(Mode.FAST_MAX.toString())) {
                edgeCliqueCover = MaxMinHeuristic.getEdgeCliqueCover(inputG, new ArrayList<Edge>(), true);
            } else if (mode.equals(Mode.FAST_MIN.toString())) {
                edgeCliqueCover = MaxMinHeuristic.getEdgeCliqueCover(inputG, new ArrayList<Edge>(), false);
            }

        } else if (isSequence) {
            if (mode.equals(Mode.SLOW_EXACT.toString())) {
                edgeCliqueCover = ExhaustiveSearch.getEdgeCliqueCover(GraphUtils.join(inputG, outputG), inputG.getEdges());
            } else if (mode.equals(Mode.FAST_SEQ.toString())) {
                edgeCliqueCover = SequenceHeuristic.getEdgeCliqueCover(GraphUtils.join(inputG, outputG), inputG.getEdges());
            } else if (mode.equals(Mode.FAST_MAX.toString())) {
                edgeCliqueCover = MaxMinHeuristic.getEdgeCliqueCover(GraphUtils.join(inputG, outputG), inputG.getEdges(), true);
            } else if (mode.equals(Mode.FAST_MIN.toString())) {
                edgeCliqueCover = MaxMinHeuristic.getEdgeCliqueCover(GraphUtils.join(inputG, outputG), inputG.getEdges(), false);
            }
        }
        return edgeCliqueCover;
    }
    public static HashMap<String, HashSet<String>> initialiseNeighbours(Graph g) {

        HashMap<String, HashSet<String>> allNeighbours = new HashMap<>();
        //initialising neighbours
        for (Edge e : g.getEdges()) {
            if (allNeighbours.containsKey(e.getFirstVertex())) {
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

            } else {
                allNeighbours.put(e.getFirstVertex(), new HashSet<String>());
                allNeighbours.get(e.getFirstVertex()).add(e.getSecondVertex());

            }
            if (allNeighbours.containsKey(e.getSecondVertex())) {
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());

            } else {
                allNeighbours.put(e.getSecondVertex(), new HashSet<String>());
                allNeighbours.get(e.getSecondVertex()).add(e.getFirstVertex());
            }
        }
        return allNeighbours;
    }
    /**
     * This function eliminates redundant cliques in an edge clique cover
     * by checking if the clique consists only of edges contained in other cliques.
     * @param noOfCliqueAnEdgeIsContainedIn, the number of cliques an edge is contained in
     * @param a clique
     * @return null if clique is redundant, or the clique
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
    public static String argmin(HashMap<String, Integer> uncoveredDegree, HashSet<String> uncoveredVertices) {
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
     * @param localUncoveredDegree
     * @param localNeighbourhood
     * @return a vertex 'u' with the highest uncovered degree in the local neighbourhood
     */
    public static String argmax(HashMap<String, Integer> localUncoveredDegree, HashSet<String> localNeighbourhood) {
        int arg = -1;
        String u = "";

        for (String str : localNeighbourhood) {
            if (localUncoveredDegree.get(str) > arg) {
                arg = localUncoveredDegree.get(str);
                u = str;
            }
        }
        return u;
    }

}
