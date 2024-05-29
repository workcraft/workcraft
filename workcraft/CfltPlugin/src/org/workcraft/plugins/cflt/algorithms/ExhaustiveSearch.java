package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.AdvancedGraph;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

import java.util.ArrayList;

/**
 * This algorithm is not as efficient as it should be, it is merely a temporary placeholder until a SAT solver
 * is used to solve the problem and replace it.
 */
public class ExhaustiveSearch {

    public static ArrayList<ArrayList<String>> getEdgeCliqueCover(Graph initialGraph, ArrayList<Edge> optionalEdges) {
        ArrayList<ArrayList<String>> edgeCliqueCover = new ArrayList<>();
        ArrayList<ArrayList<String>> allMaxCliques = null;

        try {
            allMaxCliques = MaxCliqueEnumerator.getAllMaxCliques(initialGraph);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AdvancedGraph graph = null;
        if (!initialGraph.getEdges().isEmpty()) {
            // Max number of cliques to be used in the final edge clique cover (i.e. the depth of the tree to be traversed)
            int k = 0;
            while (edgeCliqueCover.isEmpty()) {
                graph = new AdvancedGraph(initialGraph, allMaxCliques);
                edgeCliqueCover = branch(graph, k, new ArrayList<>(), optionalEdges);
                k++;
            }
        }
        return edgeCliqueCover;
    }

    private static ArrayList<ArrayList<String>> branch(AdvancedGraph graph, int k,
            ArrayList<ArrayList<String>> edgeCliqueCover, ArrayList<Edge> optionalEdges) {

        if (graph.isCovered(edgeCliqueCover, optionalEdges)) {
            return edgeCliqueCover;
        }
        if (k < 0) {
            return new ArrayList<>();
        }
        Edge selectedEdge = graph.selectEdge();
        if (selectedEdge == null) {
            return edgeCliqueCover;
        }

        for (ArrayList<String> maxClique : graph.getMaximalCliques(selectedEdge)) {
            ArrayList<ArrayList<String>> newEcc = new ArrayList<>(edgeCliqueCover);
            newEcc.add(maxClique);

            ArrayList<ArrayList<String>> edgeCliqueCoverPrime = branch(graph, k - 1, newEcc, optionalEdges);
            if (!edgeCliqueCoverPrime.isEmpty()) {
                return edgeCliqueCoverPrime;
            }
        }
        return new ArrayList<>();
    }

}
