package org.workcraft.plugins.cflt.test;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.ecc.ExhaustiveSearch;
import org.workcraft.plugins.cflt.ecc.MaxMinHeuristic;
import org.workcraft.plugins.cflt.ecc.SequenceHeuristic;
import org.workcraft.plugins.cflt.utils.GraphUtils;

class EccTests {

    @Test
    void doesCliqueCoverGraph() {
        Graph g = getGraph();
        ArrayList<ArrayList<String>> seqEcc = SequenceHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>());
        ArrayList<ArrayList<String>> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>(), true);
        ArrayList<ArrayList<String>> minEcc = MaxMinHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>(), false);
        ArrayList<ArrayList<String>> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(g, new ArrayList<Edge>());

        HashSet<String> seqEccAsEdges = getEccAsEdges(seqEcc);
        HashSet<String> maxEccAsEdges = getEccAsEdges(maxEcc);
        HashSet<String> minEccAsEdges = getEccAsEdges(minEcc);
        HashSet<String> exactEccAsEdges = getEccAsEdges(exactEcc);

        Assertions.assertTrue(doesCover(seqEccAsEdges, g));
        Assertions.assertTrue(doesCover(maxEccAsEdges, g));
        Assertions.assertTrue(doesCover(minEccAsEdges, g));
        Assertions.assertTrue(doesCover(exactEccAsEdges, g));
    }
    @Test
    void areCliquesMaximal() {
        Graph g = getGraph();
        ArrayList<ArrayList<String>> seqEcc = SequenceHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>());
        ArrayList<ArrayList<String>> maxEcc = MaxMinHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>(), true);
        ArrayList<ArrayList<String>> minEcc = MaxMinHeuristic.getEdgeCliqueCover(g, new ArrayList<Edge>(), false);
        ArrayList<ArrayList<String>> exactEcc = ExhaustiveSearch.getEdgeCliqueCover(g, new ArrayList<Edge>());

        Assertions.assertTrue(areCliquesMaxSize(seqEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(maxEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(minEcc, 4));
        Assertions.assertTrue(areCliquesMaxSize(exactEcc, 4));
    }
    private boolean areCliquesMaxSize(ArrayList<ArrayList<String>> ecc, int requiredSize) {
        for (ArrayList<String> clique : ecc) {
            if (clique.size() < requiredSize) {
                return false;
            }
        }
        return true;
    }
    private HashSet<String> getEccAsEdges(ArrayList<ArrayList<String>> ecc) {
        HashSet<String> eccAsEdges = new HashSet<>();
        for (ArrayList<String> clique : ecc) {
            for (String v1 : clique) {
                for (String v2 : clique) {
                    if (!v1.equals(v2)) {
                        eccAsEdges.add(v1 + v2);
                        eccAsEdges.add(v2 + v1);
                    }
                }
            }
        }
        return eccAsEdges;
    }
    private boolean doesCover(HashSet<String> eccAsEdges, Graph g) {

        for (Edge e : g.getEdges()) {
            if (!eccAsEdges.contains(e.getFirstVertex() + e.getSecondVertex())) {
                return false;
            }
        }
        return true;
    }
    /**
     *
     * @return a complete graph with 8 vertices, and the edge A-B, C-D, E-F, G-H missing
     * All the maximal cliques in this graph are of size 4
     */
    private Graph getGraph() {

        Graph g = new Graph();
        g.addVertex("A");
        g.addVertex("B");

        Graph g2 = new Graph();
        g2.addVertex("C");
        g2.addVertex("D");

        Graph g3 = new Graph();
        g3.addVertex("E");
        g3.addVertex("F");

        Graph g4 = new Graph();
        g4.addVertex("G");
        g4.addVertex("H");

        Graph finalG = GraphUtils.join(g, g2);
        finalG = GraphUtils.join(finalG, g3);
        finalG = GraphUtils.join(finalG, g4);

        return finalG;
    }
}
