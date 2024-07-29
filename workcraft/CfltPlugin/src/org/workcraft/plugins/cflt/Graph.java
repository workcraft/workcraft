package org.workcraft.plugins.cflt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

public class Graph {
    private List<Edge> edges = new ArrayList<>();

    // TODO: Consider using a custom class/ wrapper for Vertices rather than using a simple String to represent their names;
    private List<String> vertexNames = new ArrayList<>();

    public Graph(List<Edge> edges, List<String> vertices) {
        this.edges = edges;
        this.vertexNames = vertices;
    }
    public Graph() {
    }

    public void addVertex(String vertex) {
        this.vertexNames.add(vertex);
    }

    public void removeVertex(String vertex) {
        this.vertexNames.remove(vertex);
    }

    public void addEdge(Edge edge) {
        this.getEdges().add(edge);
    }

    public void removeEdge(Edge edge) {
        this.getEdges().remove(edge);
    }

    public List<String> getIsolatedVertices() {
        if (getEdges().isEmpty()) return new ArrayList<>(this.vertexNames);

        Set<String> connectedVertices = edges.stream()
                .flatMap(edge -> Stream.of(edge.getFirstVertexName(), edge.getSecondVertexName()))
                .collect(Collectors.toSet());

        return vertexNames.stream()
                .filter(vertexName -> !connectedVertices.contains(vertexName))
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public List<String> getVertexNames() {
        return vertexNames;
    }

    public void setVertexNames(ArrayList<String> vertices) {
        this.vertexNames = vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public Graph cloneGraph(int counter) {
        String suffix = SPECIAL_CLONE_CHARACTER + counter;

        ArrayList<String> vertices = this.vertexNames.stream().map(vertexName -> {
            return vertexName + suffix;
        }).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Edge> edges = this.getEdges().stream().map(edgeName -> {
            return new Edge(edgeName.getFirstVertexName() + suffix, edgeName.getSecondVertexName() + suffix);
        }).collect(Collectors.toCollection(ArrayList::new));

        return new Graph(edges, vertices);
    }
}
