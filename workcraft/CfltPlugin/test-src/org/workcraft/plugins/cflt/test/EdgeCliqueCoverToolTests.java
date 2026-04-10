package org.workcraft.plugins.cflt.test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverHeuristic;
import org.workcraft.plugins.cflt.algorithms.EdgeCliqueCoverSolver;
import org.workcraft.plugins.cflt.algorithms.HeuristicType;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.EdgeCliqueCoverTool;

class EdgeCliqueCoverToolTests {

    private EdgeCliqueCoverHeuristic heuristic;
    private EdgeCliqueCoverSolver solver;
    private EdgeCliqueCoverTool tool;

    private static final List<Clique> EXPECTED_CLIQUES = List.of(new Clique());

    @BeforeEach
    void setup() {
        heuristic = Mockito.mock(EdgeCliqueCoverHeuristic.class);
        solver = Mockito.mock(EdgeCliqueCoverSolver.class);

        tool = new EdgeCliqueCoverTool(heuristic, solver);

        Mockito.when(heuristic.getEdgeCliqueCover(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(EXPECTED_CLIQUES);

        Mockito.when(solver.getEdgeCliqueCover(
                Mockito.any(),
                Mockito.any()
        )).thenReturn(EXPECTED_CLIQUES);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("heuristicModes")
    void usesHeuristicCorrectly(Mode mode, HeuristicType expectedType) {

        Graph graph = new Graph();
        Set<Edge> edges = Set.of();

        List<Clique> result = tool.getEdgeCliqueCover(graph, edges, mode);

        assertEquals(EXPECTED_CLIQUES, result);

        Mockito.verify(heuristic).getEdgeCliqueCover(
                graph,
                edges,
                expectedType
        );

        Mockito.verifyNoInteractions(solver);
    }

    static Stream<Arguments> heuristicModes() {
        return Stream.of(
                Arguments.of(Mode.FAST_SEQ, HeuristicType.SEQUENCE),
                Arguments.of(Mode.FAST_MAX, HeuristicType.MAXIMAL),
                Arguments.of(Mode.FAST_MIN, HeuristicType.MINIMAL)
        );
    }

    @Test
    void usesSolverCorrectly() {

        Graph graph = new Graph();
        Set<Edge> edges = Set.of();

        List<Clique> result = tool.getEdgeCliqueCover(graph, edges, Mode.SLOW_EXACT);

        assertEquals(EXPECTED_CLIQUES, result);

        Mockito.verify(solver).getEdgeCliqueCover(
                graph,
                edges
        );

        Mockito.verifyNoInteractions(heuristic);
    }
}
