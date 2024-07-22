package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

/**
 * An undirected and unweighted graph as a list of edges and vertices
 */
public class Graph {
    private List<Edge> edges = new ArrayList<>();
    private List<String> vertices = new ArrayList<>();

    public Graph(List<Edge> edges, List<String> vertices) {
        this.edges = edges;
        this.vertices = vertices;
    }
    public Graph() {
    }

    public void addVertex(String vertex) {
        this.vertices.add(vertex);
    }

    public void removeVertex(String vertex) {
        this.vertices.remove(vertex);
    }

    public void addEdge(Edge edge) {
        this.getEdges().add(edge);
    }

    public void removeEdge(Edge edge) {
        this.getEdges().remove(edge);
    }

    public List<String> getIsolatedVertices() {
        if (getEdges().isEmpty()) return new ArrayList<>(this.vertices);

        Set<String> connectedVertices = edges.stream()
                .flatMap(e -> Stream.of(e.getFirstVertex(), e.getSecondVertex()))
                .collect(Collectors.toSet());

        return vertices.stream()
                .filter(v -> !connectedVertices.contains(v))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public List<String> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<String> vertices) {
        this.vertices = vertices;
    }

    public List<Edge> getEdges() {
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
