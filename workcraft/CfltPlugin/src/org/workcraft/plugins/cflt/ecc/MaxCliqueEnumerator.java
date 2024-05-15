package org.workcraft.plugins.cflt.ecc;

import java.util.ArrayList;
import java.util.HashMap;

import org.workcraft.plugins.cflt.Graph;

public class MaxCliqueEnumerator {

    int nodeCount;
    ArrayList<Vertex> graph = new ArrayList<>();
    ArrayList<ArrayList<String>> allMaxCliques = new ArrayList<>();
    HashMap<String, Integer> vertexNameToIndex = new HashMap<>();
    HashMap<Integer, String> vertexIndexToName = new HashMap<>();

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

    public static ArrayList<ArrayList<String>> getAllMaxCliques(Graph graph) {
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
        nodeCount = g.getVertices().size();
        int edgesCount = g.getEdges().size();
        initGraph();

        for (int k = 0; k < edgesCount; k++) {
            String[] strArr = new String[2];
            strArr[0] = g.getEdges().get(k).getFirstVertex();
            strArr[1] = g.getEdges().get(k).getSecondVertex();
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
    private ArrayList<Vertex> intersect(ArrayList<Vertex> arlFirst,
            ArrayList<Vertex> arlSecond) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.retainAll(arlSecond);
        return arlHold;
    }
    private ArrayList<Vertex> union(ArrayList<Vertex> arlFirst,
            ArrayList<Vertex> arlSecond) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.addAll(arlSecond);
        return arlHold;
    }
    private ArrayList<Vertex> removeNeighbours(ArrayList<Vertex> arlFirst, Vertex v) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.removeAll(v.getNeighbours());
        return arlHold;
    }

    private void bronKerboschWithoutPivot(ArrayList<Vertex> r, ArrayList<Vertex> p, ArrayList<Vertex> x, String pre) {
        if ((p.isEmpty()) && (x.isEmpty())) {
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
        ArrayList<Vertex> x = new ArrayList<>();
        ArrayList<Vertex> r = new ArrayList<>();
        ArrayList<Vertex> p = new ArrayList<>(graph);
        bronKerboschWithoutPivot(r, p, x, "");
    }
    private void saveClique(ArrayList<Vertex> r) {
        ArrayList<String> maxClique = new ArrayList<>();
        for (Vertex v : r) {
            maxClique.add(vertexIndexToName.get(v.getX()));
        }
        allMaxCliques.add(maxClique);
    }
    private void initialiseMap(Graph graph) {
        for (int x = 0; x < graph.getVertices().size(); x++) {
            vertexNameToIndex.put(graph.getVertices().get(x), x);
            vertexIndexToName.put(x, graph.getVertices().get(x));
        }
    }
}
