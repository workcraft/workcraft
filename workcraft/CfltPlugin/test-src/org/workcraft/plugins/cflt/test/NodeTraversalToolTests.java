package org.workcraft.plugins.cflt.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.workcraft.plugins.cflt.graph.Edge;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.DrawVisualObjectsRequest;
import org.workcraft.plugins.cflt.tools.NodeTraversalTool;
import org.workcraft.plugins.cflt.tools.VisualModelDrawingTool;
import org.workcraft.workspace.WorkspaceEntry;

class NodeTraversalToolTests {

    private VisualModelDrawingTool drawingTool;
    private WorkspaceEntry we;

    @BeforeEach
    void setup() {
        drawingTool = mock(VisualModelDrawingTool.class);
        we = mock(WorkspaceEntry.class);
    }

    @Test
    void drawInterpretedGraphCreatesCorrectSequenceGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.SEQUENCE));

        NodeTraversalTool tool = new NodeTraversalTool(drawingTool, collection);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        ArgumentCaptor<DrawVisualObjectsRequest> captor =
                ArgumentCaptor.forClass(DrawVisualObjectsRequest.class);

        verify(drawingTool, times(2)).drawVisualObjects(captor.capture());

        DrawVisualObjectsRequest sequenceRequest = captor.getAllValues().get(0);
        Graph inputGraph = sequenceRequest.inputGraph();
        Graph outputGraph = sequenceRequest.outputGraph();

        assertTrue(sequenceRequest.isSequence());
        assertFalse(sequenceRequest.isRoot());

        // Verify input graph
        assertEquals(1, inputGraph.getVertices().size());
        assertEquals(new Vertex("A"), inputGraph.getVertices().get(0));
        assertTrue(inputGraph.getEdges().isEmpty());

        // Verify output graph
        assertEquals(1, outputGraph.getVertices().size());
        assertEquals(new Vertex("B"), outputGraph.getVertices().get(0));
        assertTrue(inputGraph.getEdges().isEmpty());
    }

    @Test
    void drawInterpretedGraphCreatesCorrectConcurrencyGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.CONCURRENCY));

        NodeTraversalTool tool = new NodeTraversalTool(drawingTool, collection);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        ArgumentCaptor<DrawVisualObjectsRequest> captor =
                ArgumentCaptor.forClass(DrawVisualObjectsRequest.class);

        verify(drawingTool).drawVisualObjects(captor.capture());

        DrawVisualObjectsRequest request = captor.getValue();
        Graph inputGraph = request.inputGraph();
        Graph outputGraph = request.outputGraph();

        assertTrue(request.isRoot());

        // Verify input graph
        assertEquals(2, inputGraph.getVertices().size());
        assertEquals(new Vertex("A"), inputGraph.getVertices().get(0));
        assertEquals(new Vertex("B"), inputGraph.getVertices().get(1));
        assertTrue(inputGraph.getEdges().isEmpty());

        // Verify output graph
        assertTrue(outputGraph.getVertices().isEmpty());
        assertTrue(outputGraph.getEdges().isEmpty());
    }

    @Test
    void drawInterpretedGraphCreatesCorrectChoiceGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", "B", Operator.CHOICE));

        NodeTraversalTool tool = new NodeTraversalTool(drawingTool, collection);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        ArgumentCaptor<DrawVisualObjectsRequest> captor =
                ArgumentCaptor.forClass(DrawVisualObjectsRequest.class);

        verify(drawingTool).drawVisualObjects(captor.capture());

        DrawVisualObjectsRequest request = captor.getValue();
        Graph inputGraph = request.inputGraph();
        Graph outputGraph = request.outputGraph();

        assertTrue(request.isRoot());

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
    }

    @Test
    void drawInterpretedGraphCreatesCorrectIterationGraphs() {

        NodeCollection collection = new NodeCollection();
        collection.addNode(new Node("A", null, Operator.ITERATION));

        NodeTraversalTool tool = new NodeTraversalTool(drawingTool, collection);

        tool.drawInterpretedGraph(Mode.FAST_SEQ, we);

        ArgumentCaptor<DrawVisualObjectsRequest> captor =
                ArgumentCaptor.forClass(DrawVisualObjectsRequest.class);

        verify(drawingTool).drawVisualObjects(captor.capture());

        DrawVisualObjectsRequest request = captor.getValue();
        Graph inputGraph = request.inputGraph();
        Graph outputGraph = request.outputGraph();

        assertTrue(request.isRoot());

        // Verify input graph
        assertEquals(2, inputGraph.getVertices().size());
        assertEquals(new Vertex("A"), inputGraph.getVertices().get(0));
        assertEquals(new Vertex("A", true, 1), inputGraph.getVertices().get(1));

        // Verify output graph
        assertTrue(outputGraph.getVertices().isEmpty());
        assertTrue(outputGraph.getEdges().isEmpty());
    }
}