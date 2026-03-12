package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Vertex;

class EdgeTests {

    @Test
    void edgesWithSameVertiesAreEqual() {
        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");

        Edge e1 = new Edge(a, b);
        Edge e2 = new Edge(b, a);

        Assertions.assertEquals(e1, e2);
        Assertions.assertEquals(e1, e1);
    }

    @Test
    void edgesWithDifferentVerticesAreNotEqual() {
        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");
        Vertex c = new Vertex("C");

        Assertions.assertNotEquals(new Edge(a, b), new Edge(a, c));
    }

    @Test
    void reversedEdgesHaveSameHashCode() {
        Vertex a = new Vertex("A");
        Vertex b = new Vertex("B");

        Assertions.assertEquals(
            new Edge(a, b).hashCode(),
            new Edge(b, a).hashCode()
        );
    }
}