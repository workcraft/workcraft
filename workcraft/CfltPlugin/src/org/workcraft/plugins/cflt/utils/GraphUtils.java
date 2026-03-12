package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GraphUtils {

    private GraphUtils() {
    }

    public static Graph disjointUnion(Graph firstGraph, Graph secondGraph) {
        Stream<Edge> es1 = firstGraph.getEdges().stream();
        Stream<Edge> es2 = secondGraph.getEdges().stream();
        List<Edge> newEdges = Stream.concat(es1, es2).collect(Collectors.toList());

        Stream<Vertex> vs1 = firstGraph.getVertices().stream();
        Stream<Vertex> vs2 = secondGraph.getVertices().stream();
        List<Vertex> newVertices = Stream.concat(vs1, vs2).collect(Collectors.toList());

        return new Graph(newEdges, newVertices);
    }

    public static Graph join(Graph firstGraph, Graph secondGraph) {
        Graph newGraph = disjointUnion(firstGraph, secondGraph);

        firstGraph.getVertices()
                .stream()
                .flatMap(firstVertex -> secondGraph.getVertices()
                        .stream()
                        .map(secondVertex -> new Edge(firstVertex, secondVertex)))
                .forEach(newGraph::addEdge);

        return newGraph;
    }
}
