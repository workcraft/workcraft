package org.workcraft.plugins.cflt.utils;

import java.util.ArrayList;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

public class GraphUtils {

    public static Graph disjointUnion(Graph g1, Graph g2) {
        ArrayList<Edge> newEdges = new ArrayList<>();
        ArrayList<String> newVertices = new ArrayList<>();

        //adding the already existing edges
        for (Edge e: g1.getEdges()) {
            newEdges.add(e);
        }
        for (Edge e : g2.getEdges()) {
            newEdges.add(e);
        }
        //adding the already existing vertices
        for (String s : g1.getVertices()) {
            newVertices.add(s);
        }
        for (String s : g2.getVertices()) {
            newVertices.add(s);
        }
        return new Graph(newEdges, newVertices, null);
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
    public static void printECC(ArrayList<ArrayList<String>> ecc) {
        if (ecc.isEmpty()) {
            System.out.println("The ECC is empty");
        }
        System.out.println("No of cliques:" + ecc.size());
        for (ArrayList<String> clique : ecc) {
            System.out.println("Clique:");
            for (String s : clique) {
                //System.out.println(ExpressionUtils.labelNameMap.get(s));
                System.out.println(s);
            }
        }
    }
}
