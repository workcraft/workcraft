package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Vertex;

import java.util.stream.Stream;

class EdgeTests {

    @ParameterizedTest
    @MethodSource("equalEdges")
    void edgesAreEqual(Vertex a, Vertex b) {
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

    static Stream<Arguments> equalEdges() {
        return Stream.of(
            Arguments.of(new Vertex("A"), new Vertex("B")),
            Arguments.of(new Vertex("X"), new Vertex("Y"))
        );
    }
}