package org.workcraft.plugins.cflt.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverHeuristic;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverSolver;
import org.workcraft.plugins.cflt.algorithms.HeuristicType;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.utils.GraphUtils;

class EdgeCliqueCoverTests {

    @ParameterizedTest
    @EnumSource(HeuristicType.class)
    void doesCliqueCoverGraphWithMaximalCliques(HeuristicType heuristicType) {
        EdgeCliqueCoverHeuristic heuristic = new EdgeCliqueCoverHeuristic();
        Graph graph = getGraph();

        List<Clique> ecc = heuristic.getEdgeCliqueCover(graph, new HashSet<>(), heuristicType);

        Assertions.assertTrue(doesCover(ecc, graph));
        Assertions.assertTrue(areCliquesMaxSize(ecc, 4));
    }

    @Test
    void doesCliqueCoverGraphWithMaximalCliques() throws Exception {
        Graph graph = getGraph();

        List<Clique> ecc = new EdgeCliqueCoverSolver().getEdgeCliqueCover(graph, new HashSet<>());

        Assertions.assertTrue(doesCover(ecc, graph));
        Assertions.assertTrue(areCliquesMaxSize(ecc, 4));
    }

    private boolean areCliquesMaxSize(List<Clique> edgeCliqueCover, int requiredSize) {
        return edgeCliqueCover
                .stream()
                .allMatch(clique -> clique.getVertices().size() == requiredSize);
    }

    private boolean doesCover(List<Clique> edgeCliqueCover, Graph graph) {
        Set<Vertex> vertices = edgeCliqueCover
                .stream()
                .flatMap(clique -> clique.getVertices().stream())
                .collect(Collectors.toSet());

        return vertices.containsAll(graph.getVertices());
    }

    /**
     * @return a complete graph with 8 vertices, and the edges A-B, C-D, E-F, G-H missing
     * All the maximal cliques in this graph are of size 4
     */
    private Graph getGraph() {
        Graph graph = new Graph();
        graph.addVertex(new Vertex("A"));
        graph.addVertex(new Vertex("B"));

        Graph secondGraph = new Graph();
        secondGraph.addVertex(new Vertex("C"));
        secondGraph.addVertex(new Vertex("D"));

        Graph thirdGraph = new Graph();
        thirdGraph.addVertex(new Vertex("E"));
        thirdGraph.addVertex(new Vertex("F"));

        Graph fourthGraph = new Graph();
        fourthGraph.addVertex(new Vertex("G"));
        fourthGraph.addVertex(new Vertex("H"));

        Graph result = GraphUtils.join(graph, secondGraph);
        result = GraphUtils.join(result, thirdGraph);
        result = GraphUtils.join(result, fourthGraph);

        return result;
    }
}