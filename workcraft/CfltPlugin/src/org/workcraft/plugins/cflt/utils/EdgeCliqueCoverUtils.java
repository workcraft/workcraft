package org.workcraft.plugins.cflt.utils;

import org.workcraft.plugins.cflt.algorithms.*;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

import java.util.*;

public final class EdgeCliqueCoverUtils {

    private EdgeCliqueCoverUtils() {
    }

    public static List<Clique> getEdgeCliqueCover(Graph inputGraph, Graph outputGraph, boolean isSequence, Mode mode) {
        Graph initialGraph = isSequence
                ? GraphUtils.join(inputGraph, outputGraph)
                : inputGraph;

        List<Edge> optionalEdges = isSequence
                ? inputGraph.getEdges()
                : new ArrayList<>();

        if (mode == Mode.SLOW_EXACT) {
            var exhaustiveSearch = new ExhaustiveSearch();
            return exhaustiveSearch.getEdgeCliqueCover(initialGraph, optionalEdges);
        }
        var heuristic = new EdgeCliqueCoverHeuristic();
        return switch (mode) {
        case FAST_SEQ -> heuristic.getEdgeCliqueCover(initialGraph, optionalEdges, HeuristicType.SEQUENCE);
        case FAST_MAX -> heuristic.getEdgeCliqueCover(initialGraph, optionalEdges, HeuristicType.MAXIMAL);
        case FAST_MIN -> heuristic.getEdgeCliqueCover(initialGraph, optionalEdges, HeuristicType.MINIMAL);
        default -> new ArrayList<>();
        };
    }
}
