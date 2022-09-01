package org.workcraft.plugins.cflt.ecc;

import java.util.ArrayList;
import java.util.HashMap;

import org.workcraft.plugins.cflt.Graph;

public class MaxCliqueEnumerator {

    int nodesCount;
    ArrayList<Vertex> graph = new ArrayList<>();
    ArrayList<ArrayList<String>> allMaxCliques = new ArrayList<>();
    HashMap<String, Integer> vertexNameIndex = new HashMap<>();
    HashMap<Integer, String> vertexIndexName = new HashMap<>();

    static class Vertex implements Comparable<Vertex> {
        int x;

        int degree;
        ArrayList<Vertex> nbrs = new ArrayList<>();

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

        public ArrayList<Vertex> getNbrs() {
            return nbrs;
        }

        public void setNbrs(ArrayList<Vertex> nbrs) {
            this.nbrs = nbrs;
        }

        public void addNbr(Vertex y) {
            this.nbrs.add(y);
            if (!y.getNbrs().contains(y)) {
                y.getNbrs().add(this);
                y.degree++;
            }
            this.degree++;

        }

        public void removeNbr(Vertex y) {
            this.nbrs.remove(y);
            if (y.getNbrs().contains(y)) {
                y.getNbrs().remove(this);
                y.degree--;
            }
            this.degree--;

        }

        @Override
        public int compareTo(Vertex o) {
            return Integer.compare(this.degree, o.degree);
        }
    }

    void initGraph() {
        graph.clear();
        for (int i = 0; i < nodesCount; i++) {
            Vertex v = new Vertex();
            v.setX(i);
            graph.add(v);
        }
    }

    void readNextGraph(Graph g) {
        nodesCount = g.getVertices().size();
        int edgesCount = g.getEdges().size();
        initGraph();

        for (int k = 0; k < edgesCount; k++) {
            String[] strArr = new String[2];
            strArr[0] = g.getEdges().get(k).getFirstVertex();
            strArr[1] = g.getEdges().get(k).getSecondVertex();
            int u = vertexNameIndex.get(strArr[0]);
            int v = vertexNameIndex.get(strArr[1]);
            Vertex vertU = graph.get(u);
            Vertex vertv = graph.get(v);
            vertU.addNbr(vertv);
        }
    }

    // Finds nbr of vertex i
    ArrayList<Vertex> getNbrs(Vertex v) {
        int i = v.getX();
        return graph.get(i).nbrs;
    }

    // Intersection of two sets
    ArrayList<Vertex> intersect(ArrayList<Vertex> arlFirst,
            ArrayList<Vertex> arlSecond) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.retainAll(arlSecond);
        return arlHold;
    }

    // Union of two sets
    ArrayList<Vertex> union(ArrayList<Vertex> arlFirst,
            ArrayList<Vertex> arlSecond) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.addAll(arlSecond);
        return arlHold;
    }

    // removes the neighbours
    ArrayList<Vertex> removeNbrs(ArrayList<Vertex> arlFirst, Vertex v) {
        ArrayList<Vertex> arlHold = new ArrayList<>(arlFirst);
        arlHold.removeAll(v.getNbrs());
        return arlHold;
    }

    // Version without a pivot
    void bronKerboschWithoutPivot(ArrayList<Vertex> r, ArrayList<Vertex> p, ArrayList<Vertex> x, String pre) {
        if ((p.isEmpty()) && (x.isEmpty())) {
            saveClique(r);
            return;
        }

        ArrayList<Vertex> p1 = new ArrayList<>(p);
        for (Vertex v : p) {
            r.add(v);
            bronKerboschWithoutPivot(r, intersect(p1, getNbrs(v)),
                    intersect(x, getNbrs(v)), pre + "\t");
            r.remove(v);
            p1.remove(v);
            x.add(v);
        }
    }

    void bronKerboschPivotExecute() {
        ArrayList<Vertex> x = new ArrayList<>();
        ArrayList<Vertex> r = new ArrayList<>();
        ArrayList<Vertex> p = new ArrayList<>(graph);
        bronKerboschWithoutPivot(r, p, x, "");
    }

    void saveClique(ArrayList<Vertex> r) {
        ArrayList<String> maxClique = new ArrayList<>();
        for (Vertex v : r) {
            maxClique.add(vertexIndexName.get(v.getX()));
        }
        allMaxCliques.add(maxClique);
    }

    private void initialiseMap(Graph g) {
        for (int x = 0; x < g.getVertices().size(); x++) {
            vertexNameIndex.put(g.getVertices().get(x), x);
            vertexIndexName.put(x, g.getVertices().get(x));
        }
    }

    public static ArrayList<ArrayList<String>> getAllMaxCliques(Graph g) {

        MaxCliqueEnumerator ff = new MaxCliqueEnumerator();

        ff.initialiseMap(g);
        ff.readNextGraph(g);
        ff.bronKerboschPivotExecute();
        return ff.allMaxCliques;
    }

}
