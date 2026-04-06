package org.workcraft.plugins.cflt.test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.EdgeCliqueCoverTool;
import org.workcraft.plugins.cflt.tools.GraphInterpreterTool;
import org.workcraft.plugins.cflt.tools.RenderGraphRequest;
import org.workcraft.workspace.WorkspaceEntry;

class GraphInterpreterToolTests {

    private EdgeCliqueCoverTool edgeCliqueCoverTool;
    private GraphInterpreterTool tool;
    private WorkspaceEntry we;

    private static final Vertex A = new Vertex("A");
    private static final Vertex B = new Vertex("B");
    private static final Vertex C = new Vertex("C");
    private static final Vertex D = new Vertex("D");

    private static final List<Vertex> INPUT_VERTICES = List.of(A, B);
    private static final List<Vertex> OUTPUT_VERTICES = List.of(C, D);

    private static final List<Clique> EXPECTED_CLIQUES = List.of(new Clique());

    @BeforeEach
    void setup() {
        edgeCliqueCoverTool = Mockito.mock(EdgeCliqueCoverTool.class);
        we = Mockito.mock(WorkspaceEntry.class);

        tool = new GraphInterpreterTool(edgeCliqueCoverTool);

        Mockito.when(edgeCliqueCoverTool.getEdgeCliqueCover(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
        )).thenReturn(EXPECTED_CLIQUES);
    }

    @ParameterizedTest(name = "seq={0}, root={1}")
    @MethodSource("testCases")
    void buildsRenderRequestCorrectly(
            boolean isSequence,
            boolean isRoot,
            Set<Vertex> expectedInputVertices,
            Graph expectedGraph
    ) {

        Graph inputGraph = new Graph(List.of(), INPUT_VERTICES);
        Graph outputGraph = new Graph(List.of(), OUTPUT_VERTICES);

        RenderGraphRequest request = tool.buildRenderRequest(
                inputGraph,
                outputGraph,
                isSequence,
                isRoot,
                Mode.FAST_SEQ,
                we
        );

        assertEquals(isRoot, request.isRoot());
        assertEquals(expectedInputVertices, request.inputVertices());
        assertEquals(INPUT_VERTICES, request.isolatedVertices());
        assertEquals(EXPECTED_CLIQUES, request.cliques());

        Mockito.verify(edgeCliqueCoverTool).getEdgeCliqueCover(
                Mockito.argThat(graph ->
                        graph.getVertices().equals(expectedGraph.getVertices()) &&
                        graph.getEdges().equals(expectedGraph.getEdges())
                ),
                Mockito.eq(Set.of()),
                Mockito.eq(Mode.FAST_SEQ)
        );

        Mockito.clearInvocations(edgeCliqueCoverTool);
    }

    static Stream<Arguments> testCases() {

        Graph inputGraph = new Graph(List.of(), INPUT_VERTICES);

        List<Edge> joinEdges = List.of(
                new Edge(A, C),
                new Edge(A, D),
                new Edge(B, C),
                new Edge(B, D)
        );

        Graph joinedGraph = new Graph(
                joinEdges,
                List.of(A, B, C, D)
        );

        return Stream.of(
                Arguments.of(false, false, Set.of(), inputGraph),
                Arguments.of(false, true,  Set.of(), inputGraph),
                Arguments.of(true,  false, Set.of(A, B), joinedGraph),
                Arguments.of(true,  true,  Set.of(A, B), joinedGraph)
        );
    }
}