package org.workcraft.plugins.cflt.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;
import org.workcraft.plugins.cflt.algorithms.MaxMinHeuristic;
import org.workcraft.plugins.cflt.algorithms.SequenceHeuristic;
import org.workcraft.plugins.cflt.utils.GraphUtils;

class EdgeCliqueCoverTests {

    @Test
    void doesCliqueCoverGraph() {
        Graph graph = getGraph();
        List<List<String>> seqEcc = SequenceHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>());
        List<List<String>> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), true);
        List<List<String>> minEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), false);
        List<List<String>> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<Edge>());

        Set<String> seqEccAsEdges = getEdgeCliqueCoverAsEdgeNames(seqEcc);
        Set<String> maxEccAsEdges = getEdgeCliqueCoverAsEdgeNames(maxEcc);
        Set<String> minEccAsEdges = getEdgeCliqueCoverAsEdgeNames(minEcc);
        Set<String> exactEccAsEdges = getEdgeCliqueCoverAsEdgeNames(exactEcc);

        Assertions.assertTrue(doesCover(seqEccAsEdges, graph));
        Assertions.assertTrue(doesCover(maxEccAsEdges, graph));
        Assertions.assertTrue(doesCover(minEccAsEdges, graph));
        Assertions.assertTrue(doesCover(exactEccAsEdges, graph));
    }

    @Test
    void areCliquesMaximal() {
        Graph graph = getGraph();
        List<List<String>> seqEcc = SequenceHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>());
        List<List<String>> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), true);
        List<List<String>> minEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), false);
        List<List<String>> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<Edge>());

        Assertions.assertTrue(areCliquesMaxSize(seqEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(maxEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(minEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(exactEcc, 4));
    }

    private boolean areCliquesMaxSize(List<List<String>> edgeCliqueCover, int requiredSize) {
        return edgeCliqueCover.stream()
                .allMatch(clique -> clique.size() == requiredSize);
    }

    private Set<String> getEdgeCliqueCoverAsEdgeNames(List<List<String>> edgeCliqueCover) {
        Set<String> edgeCliqueCoverAsEdgeNames = new HashSet<>();
        for (List<String> clique : edgeCliqueCover) {
            for (String firstVertex : clique) {
                for (String secondVertex : clique) {
                    if (!firstVertex.equals(secondVertex)) {
                        edgeCliqueCoverAsEdgeNames.add(firstVertex + secondVertex);
                        edgeCliqueCoverAsEdgeNames.add(secondVertex + firstVertex);
                    }
                }
            }
        }
        return edgeCliqueCoverAsEdgeNames;
    }

    private boolean doesCover(Set<String> edgeCliqueCoverAsEdgeNames, Graph graph) {
        return graph.getEdges().stream()
                .allMatch(edge -> edgeCliqueCoverAsEdgeNames.contains(edge.getFirstVertex() + edge.getSecondVertex()));
    }

    /**
     * @return a complete graph with 8 vertices, and the edges A-B, C-D, E-F, G-H missing
     * All the maximal cliques in this graph are of size 4
     */
    private Graph getGraph() {
        Graph graph = new Graph();
        graph.addVertex("A");
        graph.addVertex("B");

        Graph secondGraph = new Graph();
        secondGraph.addVertex("C");
        secondGraph.addVertex("D");

        Graph thirdGraph = new Graph();
        thirdGraph.addVertex("E");
        thirdGraph.addVertex("F");

        Graph fourthGraph = new Graph();
        fourthGraph.addVertex("G");
        fourthGraph.addVertex("H");

        Graph finalGraph = GraphUtils.join(graph, secondGraph);
        finalGraph = GraphUtils.join(finalGraph, thirdGraph);
        finalGraph = GraphUtils.join(finalGraph, fourthGraph);

        return finalGraph;
    }

}
