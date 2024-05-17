package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

import java.util.ArrayList;

public final class GraphUtils {

    private GraphUtils() {
    }

    public static final String SPECIAL_CLONE_CHARACTER = "$";

    public static Graph disjointUnion(Graph g1, Graph g2) {
        ArrayList<Edge> newEdges = new ArrayList<>();
        newEdges.addAll(g1.getEdges());
        newEdges.addAll(g2.getEdges());
        ArrayList<String> newVertices = new ArrayList<>();
        newVertices.addAll(g1.getVertices());
        newVertices.addAll(g2.getVertices());
        return new Graph(newEdges, newVertices);
    }

    public static Graph join(Graph g1, Graph g2) {
        Graph newGraph = disjointUnion(g1, g2);
        for (String v1 : g1.getVertices()) {
            for (String v2 : g2.getVertices()) {
                newGraph.addEdge(new Edge(v1, v2));
            }
        }
        return newGraph;
    }

}
