package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.AdvancedGraph;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * This algorithm is not as efficient as it should be, it is merely a temporary placeholder until a SAT solver
 * is used to solve the problem and replace it.
 */
public class ExhaustiveSearch {

    public static List<List<String>> getEdgeCliqueCover(Graph initialGraph, List<Edge> optionalEdges) {
        List<List<String>> edgeCliqueCover = new ArrayList<>();
        List<List<String>> allMaxCliques = null;

        try {
            allMaxCliques = MaxCliqueEnumerator.getAllMaxCliques(initialGraph);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AdvancedGraph graph = null;
        if (!initialGraph.getEdges().isEmpty()) {
            int treeDepth = 0;
            while (edgeCliqueCover.isEmpty()) {
                graph = new AdvancedGraph(initialGraph, allMaxCliques);
                edgeCliqueCover = branch(graph, treeDepth, new ArrayList<>(), optionalEdges);
                treeDepth++;
            }
        }
        return edgeCliqueCover;
    }

    private static List<List<String>> branch(AdvancedGraph graph, int treeDepth,
            List<List<String>> edgeCliqueCover, List<Edge> optionalEdges) {

        if (graph.isCovered(edgeCliqueCover, optionalEdges)) {
            return edgeCliqueCover;
        }
        if (treeDepth < 0) {
            return new ArrayList<>();
        }
        Edge selectedEdge = graph.getNextEdge();
        if (selectedEdge == null) {
            return edgeCliqueCover;
        }

        for (List<String> maxClique : graph.getMaximalCliques(selectedEdge)) {
            List<List<String>> newEdgeCliqueCover = new ArrayList<>(edgeCliqueCover);
            newEdgeCliqueCover.add(maxClique);

            List<List<String>> edgeCliqueCoverPrime = branch(graph, treeDepth - 1, newEdgeCliqueCover, optionalEdges);
            if (!edgeCliqueCoverPrime.isEmpty()) {
                return edgeCliqueCoverPrime;
            }
        }
        return new ArrayList<>();
    }

}
