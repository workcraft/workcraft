package org.workcraft.plugins.cflt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.Clique;
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
        List<Clique> seqEcc = SequenceHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>());
        List<Clique> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), true);
        List<Clique> minEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), false);
//        List<Clique> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<Edge>());

        Assertions.assertTrue(doesCover(seqEcc, graph));
        Assertions.assertTrue(doesCover(maxEcc, graph));
        Assertions.assertTrue(doesCover(minEcc, graph));
//        Assertions.assertTrue(doesCover(exactEcc, graph));
    }

    @Test
    void areCliquesMaximal() {
        Graph graph = getGraph();
        List<Clique> seqEcc = SequenceHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>());
        List<Clique> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), true);
        List<Clique> minEcc = MaxMinHeuristic.getEdgeCliqueCover(graph, new ArrayList<Edge>(), false);
        List<Clique> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<Edge>());

        Assertions.assertTrue(areCliquesMaxSize(seqEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(maxEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(minEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(exactEcc, 4));
    }

    private boolean areCliquesMaxSize(List<Clique> edgeCliqueCover, int requiredSize) {
        return edgeCliqueCover.stream()
                .allMatch(clique -> clique.getVertexNames().size() == requiredSize);
    }

    private boolean doesCover(List<Clique> edgeCliqueCover, Graph graph) {
        Set<String> vertexNames = edgeCliqueCover.stream()
                .flatMap(clique -> clique.getVertexNames().stream())
                .collect(Collectors.toSet());

        return vertexNames.containsAll(graph.getVertexNames());
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
