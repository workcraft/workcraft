package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GraphUtils {

    private GraphUtils() {
    }

    public static final String SPECIAL_CLONE_CHARACTER = "$";

    public static Graph disjointUnion(Graph firstGraph, Graph secondGraph) {
        Stream<Edge> es1 = firstGraph.getEdges().stream();
        Stream<Edge> es2;
        es2 = secondGraph.getEdges().stream();
        List<Edge> newEdges = Stream.concat(es1, es2).collect(Collectors.toList());

        Stream<String> vs1 = firstGraph.getVertexNames().stream();
        Stream<String> vs2 = secondGraph.getVertexNames().stream();
        List<String> newVertices = Stream.concat(vs1, vs2).collect(Collectors.toList());

        return new Graph(newEdges, newVertices);
    }

    public static Graph join(Graph firstGraph, Graph secondGraph) {
        Graph newGraph = disjointUnion(firstGraph, secondGraph);

        firstGraph.getVertexNames()
                .stream()
                .flatMap(firstVertex -> secondGraph.getVertexNames()
                        .stream()
                        .map(secondVertex -> new Edge(firstVertex, secondVertex)))
                .forEach(newGraph::addEdge);

        return newGraph;
    }

    public static GetCleanVertexNameResponse getCleanVertexName(String vertexName) {
        int index = vertexName.indexOf(SPECIAL_CLONE_CHARACTER);
        boolean isClone = index != -1;
        String cleanVertexName = isClone
                ? vertexName.substring(0, index)
                : vertexName;
        return new GetCleanVertexNameResponse(cleanVertexName, isClone);
    }

    public record GetCleanVertexNameResponse(String vertexName, boolean isClone) {
    }
}
