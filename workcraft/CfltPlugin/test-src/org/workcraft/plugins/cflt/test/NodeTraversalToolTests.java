package org.workcraft.plugins.cflt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.GraphInterpreterTool;
import org.workcraft.plugins.cflt.tools.NodeTraversalTool;
import org.workcraft.plugins.cflt.tools.RenderGraphRequest;
import org.workcraft.plugins.cflt.tools.VisualModelDrawingTool;
import org.workcraft.workspace.WorkspaceEntry;

class NodeTraversalToolTests {

    private VisualModelDrawingTool drawingTool;
    private GraphInterpreterTool interpreterTool;
    private WorkspaceEntry we;
    private RenderGraphRequest mockRequest;

    private ArgumentCaptor<Graph> inputCaptor;
    private ArgumentCaptor<Graph> outputCaptor;
    private ArgumentCaptor<Boolean> isSequenceCaptor;
    private ArgumentCaptor<Boolean> isRootCaptor;

    @BeforeEach
    void setup() {
        drawingTool = Mockito.mock(VisualModelDrawingTool.class);
        interpreterTool = Mockito.mock(GraphInterpreterTool.class);
        we = Mockito.mock(WorkspaceEntry.class);
        mockRequest = Mockito.mock(RenderGraphRequest.class);

        inputCaptor = ArgumentCaptor.forClass(Graph.class);
        outputCaptor = ArgumentCaptor.forClass(Graph.class);
        isSequenceCaptor = ArgumentCaptor.forClass(Boolean.class);
        isRootCaptor = ArgumentCaptor.forClass(Boolean.class);

        Mockito.when(interpreterTool.buildRenderRequest(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.anyBoolean(),
                ArgumentMatchers.anyBoolean(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
        )).thenReturn(mockRequest);
    }

    @Test
    void drawInterpretedGraphCreatesCorrectSequenceGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.SEQUENCE));

        NodeTraversalTool tool = new NodeTraversalTool(collection, drawingTool, interpreterTool);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        Mockito.verify(interpreterTool, Mockito.times(2)).buildRenderRequest(
                inputCaptor.capture(),
                outputCaptor.capture(),
                isSequenceCaptor.capture(),
                isRootCaptor.capture(),
                ArgumentMatchers.eq(Mode.FAST_SEQ),
                ArgumentMatchers.eq(we)
        );

        Graph sequenceInput = inputCaptor.getAllValues().get(0);
        Graph sequenceOutput = outputCaptor.getAllValues().get(0);

        // Verify sequence call
        assertTrue(isSequenceCaptor.getAllValues().get(0));
        assertFalse(isRootCaptor.getAllValues().get(0));

        // Verify input graph
        assertEquals(1, sequenceInput.getVertices().size());
        assertEquals(new Vertex("A"), sequenceInput.getVertices().get(0));
        assertTrue(sequenceInput.getEdges().isEmpty());

        // Verify output graph
        assertEquals(1, sequenceOutput.getVertices().size());
        assertEquals(new Vertex("B"), sequenceOutput.getVertices().get(0));
        assertTrue(sequenceOutput.getEdges().isEmpty());

        // Verify root call
        assertFalse(isSequenceCaptor.getAllValues().get(1));
        assertTrue(isRootCaptor.getAllValues().get(1));

        Mockito.verify(drawingTool, Mockito.times(2)).renderGraph(mockRequest);
    }

    @Test
    void drawInterpretedGraphCreatesCorrectConcurrencyGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.CONCURRENCY));

        NodeTraversalTool tool = new NodeTraversalTool(collection, drawingTool, interpreterTool);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        Mockito.verify(interpreterTool).buildRenderRequest(
                inputCaptor.capture(),
                outputCaptor.capture(),
                isSequenceCaptor.capture(),
                isRootCaptor.capture(),
                ArgumentMatchers.eq(Mode.FAST_SEQ),
                ArgumentMatchers.eq(we)
        );

        Graph inputGraph = inputCaptor.getValue();
        Graph outputGraph = outputCaptor.getValue();

        assertFalse(isSequenceCaptor.getValue());
        assertTrue(isRootCaptor.getValue());

        // Verify input graph
        assertEquals(2, inputGraph.getVertices().size());
        assertEquals(new Vertex("A"), inputGraph.getVertices().get(0));
        assertEquals(new Vertex("B"), inputGraph.getVertices().get(1));
        assertTrue(inputGraph.getEdges().isEmpty());

        // Verify output graph
        assertTrue(outputGraph.getVertices().isEmpty());
        assertTrue(outputGraph.getEdges().isEmpty());

        Mockito.verify(drawingTool).renderGraph(mockRequest);
    }

    @Test
    void drawInterpretedGraphCreatesCorrectChoiceGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.CHOICE));

        NodeTraversalTool tool = new NodeTraversalTool(collection, drawingTool, interpreterTool);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        Mockito.verify(interpreterTool).buildRenderRequest(
                inputCaptor.capture(),
                outputCaptor.capture(),
                isSequenceCaptor.capture(),
                isRootCaptor.capture(),
                ArgumentMatchers.eq(Mode.FAST_SEQ),
                ArgumentMatchers.eq(we)
        );

        Graph inputGraph = inputCaptor.getValue();
        Graph outputGraph = outputCaptor.getValue();

        assertFalse(isSequenceCaptor.getValue());
        assertTrue(isRootCaptor.getValue());

        Vertex vertexA = new Vertex("A");
        Vertex vertexB = new Vertex("B");

        // Verify input graph
        assertEquals(2, inputGraph.getVertices().size());
        assertEquals(vertexA, inputGraph.getVertices().get(0));
        assertEquals(vertexB, inputGraph.getVertices().get(1));
        assertEquals(new Edge(vertexA, vertexB), inputGraph.getEdges().get(0));

        // Verify output graph
        assertTrue(outputGraph.getVertices().isEmpty());
        assertTrue(outputGraph.getEdges().isEmpty());

        Mockito.verify(drawingTool).renderGraph(mockRequest);
    }

    @Test
    void drawInterpretedGraphCreatesCorrectIterationGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", null, Operator.ITERATION));

        NodeTraversalTool tool = new NodeTraversalTool(collection, drawingTool, interpreterTool);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        Mockito.verify(interpreterTool).buildRenderRequest(
                inputCaptor.capture(),
                outputCaptor.capture(),
                isSequenceCaptor.capture(),
                isRootCaptor.capture(),
                ArgumentMatchers.eq(Mode.FAST_SEQ),
                ArgumentMatchers.eq(we)
        );

        Graph inputGraph = inputCaptor.getValue();
        Graph outputGraph = outputCaptor.getValue();

        assertFalse(isSequenceCaptor.getValue());
        assertTrue(isRootCaptor.getValue());

        // Verify input graph
        assertEquals(2, inputGraph.getVertices().size());
        assertEquals(new Vertex("A"), inputGraph.getVertices().get(0));
        assertEquals(new Vertex("A", true, 1), inputGraph.getVertices().get(1));

        // Verify output graph
        assertTrue(outputGraph.getVertices().isEmpty());
        assertTrue(outputGraph.getEdges().isEmpty());

        Mockito.verify(drawingTool).renderGraph(mockRequest);
    }
}