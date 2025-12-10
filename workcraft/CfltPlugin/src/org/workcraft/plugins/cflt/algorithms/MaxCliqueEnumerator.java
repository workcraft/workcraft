package org.workcraft.plugins.cflt.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;

// TODO: Remove this once a SAT Solver (or any other exact, efficient solution) is used instead of the ExhaustiveSearch
public class MaxCliqueEnumerator {

    int nodeCount;
    List<Vertex> graph = new ArrayList<>();
    List<Clique> allMaxCliques = new ArrayList<>();
    Map<String, Integer> vertexNameToIndex = new HashMap<>();
    Map<Integer, String> vertexIndexToName = new HashMap<>();

    static class Vertex implements Comparable<Vertex> {
        int x;
        int degree;
        ArrayList<Vertex> neighbours = new ArrayList<>();

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getDegree() {
            return degree;
        }

        public void setDegree(int degree) {
            this.degree = degree;
        }

        public ArrayList<Vertex> getNeighbours() {
            return neighbours;
        }

        public void setNeighbours(ArrayList<Vertex> neighbours) {
            this.neighbours = neighbours;
        }

        public void addNeighbour(Vertex vertex) {
            this.neighbours.add(vertex);
            if (!vertex.getNeighbours().contains(vertex)) {
                vertex.getNeighbours().add(this);
                vertex.degree++;
            }
            this.degree++;

        }

        public void removeNeighbour(Vertex vertex) {
            this.neighbours.remove(vertex);
            if (vertex.getNeighbours().contains(vertex)) {
                vertex.getNeighbours().remove(this);
                vertex.degree--;
            }
            this.degree--;
        }

        @Override
        public int compareTo(Vertex o) {
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
            Vertex v = new Vertex();
            v.setX(i);
            graph.add(v);
        }
    }

    private void readNextGraph(Graph g) {
        nodeCount = g.getVertexNames().size();
        int edgesCount = g.getEdges().size();
        initGraph();

        for (int k = 0; k < edgesCount; k++) {
            String[] strArr = new String[2];
            strArr[0] = g.getEdges().get(k).firstVertexName();
            strArr[1] = g.getEdges().get(k).secondVertexName();
            int u = vertexNameToIndex.get(strArr[0]);
            int v = vertexNameToIndex.get(strArr[1]);
            Vertex vertexU = graph.get(u);
            Vertex vertexV = graph.get(v);
            vertexU.addNeighbour(vertexV);
        }
    }
    private ArrayList<Vertex> getNeighbours(Vertex v) {
        int i = v.getX();
        return graph.get(i).neighbours;
    }
    private List<Vertex> intersect(List<Vertex> arlFirst,
            List<Vertex> arlSecond) {
        List<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.retainAll(arlSecond);
        return arlHold;
    }
    private void bronKerboschWithoutPivot(List<Vertex> r, List<Vertex> p, List<Vertex> x, String pre) {
        if (p.isEmpty() && x.isEmpty()) {
            saveClique(r);
            return;
        }

        ArrayList<Vertex> p1 = new ArrayList<>(p);
        for (Vertex v : p) {
            r.add(v);
            bronKerboschWithoutPivot(r, intersect(p1, getNeighbours(v)),
                    intersect(x, getNeighbours(v)), pre + "\t");
            r.remove(v);
            p1.remove(v);
            x.add(v);
        }
    }
    private void bronKerboschPivotExecute() {
        List<Vertex> x = new ArrayList<>();
        List<Vertex> r = new ArrayList<>();
        List<Vertex> p = new ArrayList<>(graph);
        bronKerboschWithoutPivot(r, p, x, "");
    }
    private void saveClique(List<Vertex> r) {
        Clique maxClique = new Clique();
        for (Vertex v : r) {
            maxClique.addVertexName(vertexIndexToName.get(v.getX()));
        }
        allMaxCliques.add(maxClique);
    }
    private void initialiseMap(Graph graph) {
        for (int i = 0; i < graph.getVertexNames().size(); i++) {
            vertexNameToIndex.put(graph.getVertexNames().get(i), i);
            vertexIndexToName.put(i, graph.getVertexNames().get(i));
        }
    }
}
