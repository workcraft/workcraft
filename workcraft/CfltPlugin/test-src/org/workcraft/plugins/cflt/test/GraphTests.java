package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphTests {

    @Test
    void addVertexAddsVertex() {
        Graph graph = new Graph();

        Vertex vertex = new Vertex("A");
        graph.addVertex(vertex);

        Assertions.assertEquals(List.of(vertex), graph.getVertices());
    }

    @Test
    void addEdgeAddsEdge() {
        Graph graph = new Graph();

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");

        graph.addVertex(a);
        graph.addVertex(b);

        Edge edge = new Edge(a, b);

        graph.addEdge(edge);

        Assertions.assertEquals(List.of(edge), graph.getEdges());
    }

    @Test
    void getVertexToAllNeighboursReturnsCorrectSetOfNeighbours() {
        Graph graph = new Graph();

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");

        Edge ab = new Edge(a, b);
        Edge bc = new Edge(b, c);

        graph.addEdge(ab);
        graph.addEdge(bc);

        Map<Vertex, Set<Vertex>> neighbours = graph.getVertexToAllNeighbours();

        Assertions.assertEquals(Set.of(b), neighbours.get(a));
        Assertions.assertEquals(Set.of(a, c), neighbours.get(b));
        Assertions.assertEquals(Set.of(b), neighbours.get(c));
    }

    @Test
    void cloneGraphCreatesRenamedCopy() {
        Graph graph = new Graph();

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");

        graph.addVertex(a);
        graph.addVertex(b);

        graph.addEdge(new Edge(a, b));

        Graph clone = graph.deepClone(1);

        Vertex aClone = new Vertex("A", true, 1);
        Vertex bClone = new Vertex("B", true, 1);

        Assertions.assertEquals(
                List.of(aClone, bClone),
                clone.getVertices()
        );

        Assertions.assertEquals(
                List.of(new Edge(aClone, bClone)),
                clone.getEdges()
        );
    }

    @Test
    void getIsolatedVerticesReturnsVerticesWithoutEdges() {
        Graph graph = new Graph();

        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");

        graph.addVertex(a);
        graph.addVertex(b);
        graph.addVertex(c);

        graph.addEdge(new Edge(a, b));

        List<Vertex> isolated = graph.getIsolatedVertices();

        Assertions.assertEquals(List.of(c), isolated);
    }
}