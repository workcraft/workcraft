package org.workcraft.plugins.cflt.graph;

import java.util.ArrayList;
import java.util.List;

public class Clique {
    List<Vertex> vertices = new ArrayList<>();
    List<Edge> edges = new ArrayList<>();

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }
}
