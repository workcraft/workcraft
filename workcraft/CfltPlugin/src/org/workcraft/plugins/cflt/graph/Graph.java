package org.workcraft.plugins.cflt.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public List<Vertex> getIsolatedVertices() {
        if (vertices.isEmpty()) return new ArrayList<>();

        Map<Vertex, Set<Vertex>> neighbours = getVertexToAllNeighbours();

        return vertices.stream()
                .filter(v -> !neighbours.containsKey(v) || neighbours.get(v).isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Graph deepClone(int cloneGeneration) {

        List<Vertex> newVertices = vertices
                .stream()
                .map(vertex -> vertex.clone(cloneGeneration))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Edge> newEdges = edges
                .stream()
                .map(edge -> new Edge(
                        edge.firstVertex().clone(cloneGeneration),
                        edge.secondVertex().clone(cloneGeneration)
                ))
                .collect(Collectors.toCollection(ArrayList::new));

        return new Graph(newEdges, newVertices);
    }

    public Map<Vertex, Set<Vertex>> getVertexToAllNeighbours() {

        Map<Vertex, Set<Vertex>> vertexToAllNeighbours = new HashMap<>();

        for (Edge edge : edges) {
            vertexToAllNeighbours
                    .computeIfAbsent(edge.firstVertex(), k -> new HashSet<>())
                    .add(edge.secondVertex());
            vertexToAllNeighbours
                    .computeIfAbsent(edge.secondVertex(), k -> new HashSet<>())
                    .add(edge.firstVertex());
        }

        return vertexToAllNeighbours;
    }
}
