package org.workcraft.plugins.cflt.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverHeuristic;
import org.workcraft.plugins.cflt.algorithms.HeuristicType;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.utils.GraphUtils;
import org.workcraft.plugins.cflt.algorithms.ExhaustiveSearch;

class EdgeCliqueCoverTests {

    @Test
    void doesCliqueCoverGraph() {
        EdgeCliqueCoverHeuristic heuristic = new EdgeCliqueCoverHeuristic();
        ExhaustiveSearch exhaustiveSearch = new ExhaustiveSearch();
        Graph graph = getGraph();

        List<Clique> seqEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.SEQUENCE);
        List<Clique> maxEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.MAXIMAL);
        List<Clique> minEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.MINIMAL);
        List<Clique> exactEcc = exhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<>());

        Assertions.assertTrue(doesCover(seqEcc, graph));
        Assertions.assertTrue(doesCover(maxEcc, graph));
        Assertions.assertTrue(doesCover(minEcc, graph));
        Assertions.assertTrue(doesCover(exactEcc, graph));
    }

    @Test
    void areCliquesMaximal() {
        EdgeCliqueCoverHeuristic heuristic = new EdgeCliqueCoverHeuristic();
        ExhaustiveSearch exhaustiveSearch = new ExhaustiveSearch();
        Graph graph = getGraph();

        List<Clique> seqEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.SEQUENCE);
        List<Clique> maxEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.MAXIMAL);
        List<Clique> minEcc = heuristic.getEdgeCliqueCover(graph, new ArrayList<>(), HeuristicType.MINIMAL);
        List<Clique> exactEcc = exhaustiveSearch.getEdgeCliqueCover(graph, new ArrayList<>());

        Assertions.assertTrue(areCliquesMaxSize(seqEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(maxEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(minEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(exactEcc, 4));
    }

    private boolean areCliquesMaxSize(List<Clique> edgeCliqueCover, int requiredSize) {
        return edgeCliqueCover
                .stream()
                .allMatch(clique -> clique.getVertices().size() == requiredSize);
    }

    private boolean doesCover(List<Clique> edgeCliqueCover, Graph graph) {
        Set<Vertex> vertices = edgeCliqueCover
                .stream()
                .flatMap(clique -> clique.getVertices()
                        .stream())
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
