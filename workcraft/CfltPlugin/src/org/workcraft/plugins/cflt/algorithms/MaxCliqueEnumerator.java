package org.workcraft.plugins.cflt.algorithms;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Remove this once a SAT Solver (or any other exact, efficient solution) is used instead of the ExhaustiveSearch
public class MaxCliqueEnumerator {

    int nodeCount;
    List<EnumeratorVertex> graph = new ArrayList<>();
    List<Clique> allMaxCliques = new ArrayList<>();
    Map<Vertex, Integer> vertexToIndex = new HashMap<>();
    Map<Integer, Vertex> indexToVertex = new HashMap<>();

    static class EnumeratorVertex implements Comparable<EnumeratorVertex> {
        int x;
        int degree;
        ArrayList<EnumeratorVertex> neighbours = new ArrayList<>();

        int getX() {
            return x;
        }

        void setX(int x) {
            this.x = x;
        }

        ArrayList<EnumeratorVertex> getNeighbours() {
            return neighbours;
        }

        void addNeighbour(EnumeratorVertex vertex) {
            this.neighbours.add(vertex);
            if (!vertex.getNeighbours().contains(vertex)) {
                vertex.getNeighbours().add(this);
                vertex.degree++;
            }
            this.degree++;

        }

        void removeNeighbour(EnumeratorVertex vertex) {
            this.neighbours.remove(vertex);
            if (vertex.getNeighbours().contains(vertex)) {
                vertex.getNeighbours().remove(this);
                vertex.degree--;
            }
            this.degree--;
        }

        @Override
        public int compareTo(EnumeratorVertex o) {
            return Integer.compare(this.degree, o.degree);
        }
    }

    public static List<Clique> getAllMaxCliques(Graph graph) {
        MaxCliqueEnumerator enumerator = new MaxCliqueEnumerator();

        enumerator.initialiseMap(graph);
        enumerator.readNextGraph(graph);
        enumerator.bronKerboschPivotExecute();
        return enumerator.allMaxCliques;
    }

    private void initGraph() {
        graph.clear();
        for (int i = 0; i < nodeCount; i++) {
            EnumeratorVertex v = new EnumeratorVertex();
            v.setX(i);
            graph.add(v);
        }
    }

    private void readNextGraph(Graph g) {
        nodeCount = g.getVertices().size();
        int edgesCount = g.getEdges().size();
        initGraph();

        for (int k = 0; k < edgesCount; k++) {
            Vertex[] vertexArray = new Vertex[2];
            vertexArray[0] = g.getEdges().get(k).firstVertex();
            vertexArray[1] = g.getEdges().get(k).secondVertex();
            int u = vertexToIndex.get(vertexArray[0]);
            int v = vertexToIndex.get(vertexArray[1]);
            EnumeratorVertex vertexU = graph.get(u);
            EnumeratorVertex vertexV = graph.get(v);
            vertexU.addNeighbour(vertexV);
        }
    }
    private ArrayList<EnumeratorVertex> getNeighbours(EnumeratorVertex v) {
        int i = v.getX();
        return graph.get(i).neighbours;
    }
    private List<EnumeratorVertex> intersect(List<EnumeratorVertex> arlFirst,
            List<EnumeratorVertex> arlSecond) {
        List<EnumeratorVertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.retainAll(arlSecond);
        return arlHold;
    }
    private void bronKerboschWithoutPivot(List<EnumeratorVertex> r, List<EnumeratorVertex> p, List<EnumeratorVertex> x, String pre) {
        if (p.isEmpty() && x.isEmpty()) {
            saveClique(r);
            return;
        }

        ArrayList<EnumeratorVertex> p1 = new ArrayList<>(p);
        for (EnumeratorVertex v : p) {
            r.add(v);
            bronKerboschWithoutPivot(r, intersect(p1, getNeighbours(v)),
                    intersect(x, getNeighbours(v)), pre + "\t");
            r.remove(v);
            p1.remove(v);
            x.add(v);
        }
    }
    private void bronKerboschPivotExecute() {
        List<EnumeratorVertex> x = new ArrayList<>();
        List<EnumeratorVertex> r = new ArrayList<>();
        List<EnumeratorVertex> p = new ArrayList<>(graph);
        bronKerboschWithoutPivot(r, p, x, "");
    }
    private void saveClique(List<EnumeratorVertex> r) {
        Clique maxClique = new Clique();
        for (EnumeratorVertex v : r) {
            maxClique.addVertex(indexToVertex.get(v.getX()));
        }
        allMaxCliques.add(maxClique);
    }
    private void initialiseMap(Graph graph) {
        for (int i = 0; i < graph.getVertices().size(); i++) {
            vertexToIndex.put(graph.getVertices().get(i), i);
            indexToVertex.put(i, graph.getVertices().get(i));
        }
    }
}
