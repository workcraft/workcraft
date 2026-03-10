package org.workcraft.plugins.cflt.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph {

    private List<Edge> edges = new ArrayList<>();
    private List<Vertex> vertices = new ArrayList<>();

    public Graph(List<Edge> edges, List<Vertex> vertices) {
        this.edges = edges;
        this.vertices = vertices;
    }

    public Graph() {
    }

    public void addVertex(Vertex vertex) {
        this.vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        this.getEdges().add(edge);
    }

    public List<Vertex> getIsolatedVertices() {
        if (getEdges().isEmpty()) return new ArrayList<>(this.vertices);

        Set<Vertex> connectedVertices = edges
                .stream()
                .flatMap(edge -> Stream.of(edge.firstVertex(), edge.secondVertex()))
                .collect(Collectors.toSet());

        return vertices
            .stream()
            .filter(vertex -> !connectedVertices.contains(vertex))
            .collect(Collectors.toCollection(ArrayList::new));
    }
    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Graph deepClone(int cloneGeneration) {

        List<Vertex> vertices = this.vertices
                .stream()
                .map(vertex -> vertex.clone(cloneGeneration))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Edge> edges = this.getEdges()
                .stream()
                .map(edge -> new Edge(
                        edge.firstVertex().clone(cloneGeneration),
                        edge.secondVertex().clone(cloneGeneration)
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Graph(edges, vertices);
    }
}
