package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An undirected and unweighted graph as a set of edges
 */
public class Graph {

    public static final String SPECIAL_CLONE_CHARACTER = "$";
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<String> vertices = new ArrayList<>();
    boolean[][] connections;

    public Graph(ArrayList<Edge> edgeList, ArrayList<String> vertices, boolean[][] connections) {
        this.edges = edgeList;
        this.connections = connections;
        this.vertices = vertices;
    }

    public Graph() {
    }

    public void initialiseConnections() {
        this.connections = new boolean[getVertices().size()][getVertices().size()];
        for (int x = 0; x < connections.length; x++) {
            for (int y = 0; y < connections.length; y++) {
                this.connections[x][y] = false;
            }
        }
        for (Edge e : getEdges()) {
            this.connections[getVertices().indexOf(e.getFirstVertex())][getVertices().indexOf(e.getSecondVertex())] = true;
            this.connections[getVertices().indexOf(e.getSecondVertex())][getVertices().indexOf(e.getFirstVertex())] = true;
        }
    }

    public void addVertex(String v) {
        this.vertices.add(v);
    }

    public void removeVertex(String v) {
        this.vertices.remove(v);
    }

    public void addEdge(Edge e) {
        this.getEdges().add(e);
    }

    public void removeEdge(Edge e) {
        this.getEdges().remove(e);
    }

    public ArrayList<String> getIsolatedVertices() {
        if (getEdges().isEmpty()) {
            return getVertices();
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> vertexList = (ArrayList<String>) vertices.clone();
        for (Edge e : getEdges()) {
            String v1 = e.getFirstVertex();
            String v2 = e.getSecondVertex();
            if (vertexList.contains(v1)) {
                vertexList.remove(v1);
            }
            if (vertexList.contains(v2)) {
                vertexList.remove(v2);
            }
        }
        return vertexList;

    }

    public void removeEdges(Graph sequenceInputGraph) {
        for (Edge edge : sequenceInputGraph.getEdges()) {
            this.edges.remove(edge);
        }
    }

    public void setEdges(List<Edge> edges) {
        this.edges = (ArrayList<Edge>) edges;
    }

    public void setConnections(boolean[][] cons) {
        this.connections = cons;
    }

    public ArrayList<String> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<String> vertices) {
        this.vertices = vertices;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public Graph cloneGraph(int counter) {
        String suffix = SPECIAL_CLONE_CHARACTER + counter;

        ArrayList<String> vertices = this.vertices.stream().map(vertexName -> {
            return vertexName + suffix;
        }).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Edge> edges = this.getEdges().stream().map(edgeName -> {
            return new Edge(edgeName.getFirstVertex() + suffix, edgeName.getSecondVertex() + suffix);
        }).collect(Collectors.toCollection(ArrayList::new));

        return new Graph(edges, vertices, null);
    }
}
