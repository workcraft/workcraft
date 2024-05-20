package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

/**
 * An undirected and unweighted graph as a set of edges
 */
public class Graph {
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<String> vertices = new ArrayList<>();

    public Graph(ArrayList<Edge> edges, ArrayList<String> vertices) {
        this.edges = edges;
        this.vertices = vertices;
    }
    public Graph() {
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
        if (getEdges().isEmpty()) return this.vertices;
        ArrayList<String> vertexList = this.vertices.stream().collect(Collectors.toCollection(ArrayList::new));
        for (Edge e : this.edges) {
            if (vertexList.contains(e.getFirstVertex())) {
                vertexList.remove(e.getFirstVertex());
            }
            if (vertexList.contains(e.getSecondVertex())) {
                vertexList.remove(e.getSecondVertex());
            }
        }
        return vertexList;

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

        return new Graph(edges, vertices);
    }
}
