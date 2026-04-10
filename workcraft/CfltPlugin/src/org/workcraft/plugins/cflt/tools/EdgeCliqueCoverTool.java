package org.workcraft.plugins.cflt.tools;

import java.util.List;
import java.util.Set;

import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverHeuristic;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverSolver;
import org.workcraft.plugins.cflt.algorithms.HeuristicType;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;

public class EdgeCliqueCoverTool {

    private final EdgeCliqueCoverHeuristic heuristic;
    private final EdgeCliqueCoverSolver solver;

    public EdgeCliqueCoverTool(
            EdgeCliqueCoverHeuristic heuristic,
            EdgeCliqueCoverSolver solver
    ) {
        this.heuristic = heuristic;
        this.solver = solver;
    }

    public List<Clique> getEdgeCliqueCover(
            Graph graph,
            Set<Edge> optionalEdges,
            Mode mode
    ) {
        return switch (mode) {
            case FAST_SEQ -> heuristic.getEdgeCliqueCover(graph, optionalEdges, HeuristicType.SEQUENCE);
            case FAST_MAX -> heuristic.getEdgeCliqueCover(graph, optionalEdges, HeuristicType.MAXIMAL);
            case FAST_MIN -> heuristic.getEdgeCliqueCover(graph, optionalEdges, HeuristicType.MINIMAL);
            case SLOW_EXACT -> solver.getEdgeCliqueCover(graph, optionalEdges);
            default -> throw new IllegalArgumentException("Unexpected value: " + mode);
        };
    }
}