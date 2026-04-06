package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.NodeIterator;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.GraphUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NodeTraversalTool {
    NodeCollection nodeCollection;

    HashMap<String, Graph> entryGraphs = new HashMap<>();
    HashMap<String, Graph> exitGraphs = new HashMap<>();

    VisualModelDrawingTool visualModelDrawingTool;

    public NodeTraversalTool(VisualModelDrawingTool drawingTool, NodeCollection nodeCollection) {
        this.visualModelDrawingTool = drawingTool;
        this.nodeCollection = nodeCollection;
    }

    public void drawInterpretedGraph(Mode mode, WorkspaceEntry we) {
        if (nodeCollection.isEmpty() && nodeCollection.getSingleTransition() != null) {
            drawSingleTransition(we);
        }

        NodeIterator nodeIterator = nodeCollection.getNodeIterator();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();

            String leftChildName = node.leftChildName();
            String rightChildName = node.rightChildName();
            Operator operator = node.operator();

            ensureGraphExists(entryGraphs, leftChildName);
            ensureGraphExists(entryGraphs, rightChildName);
            ensureGraphExists(exitGraphs, leftChildName);
            ensureGraphExists(exitGraphs, rightChildName);

            switch (operator) {
                case CONCURRENCY -> this.handleConcurrency(leftChildName, rightChildName);
                case CHOICE -> this.handleChoice(leftChildName, rightChildName);
                case SEQUENCE -> this.handleSequence(leftChildName, rightChildName, mode, we);
                case ITERATION -> this.handleIteration(leftChildName, nodeIterator.getCurrentPosition());
            }

            if (nodeIterator.isLastNode()) {
                Graph inputGraph = entryGraphs.get(leftChildName);
                Graph outputGraph = new Graph();

                DrawVisualObjectsRequest request = new DrawVisualObjectsRequest(
                        inputGraph,
                        outputGraph,
                        false,
                        true,
                        mode,
                        we
                );

                visualModelDrawingTool.drawVisualObjects(request);
            }
        }
    }

    private void drawSingleTransition(WorkspaceEntry we) {
        visualModelDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
    }

    private void ensureGraphExists(Map<String, Graph> graphs, String key) {
        graphs.computeIfAbsent(
                key,
                vertexName -> new Graph(
                        new ArrayList<>(),
                        new ArrayList<>(List.of(new Vertex(vertexName)))
                )
        );
    }

    private void handleConcurrency(String leftChildName, String rightChildName) {
        handleBinaryGraphOp(leftChildName, rightChildName, GraphUtils::disjointUnion);
    }

    private void handleChoice(String leftChildName, String rightChildName) {
        handleBinaryGraphOp(leftChildName, rightChildName, GraphUtils::join);
    }

    private void handleBinaryGraphOp(
            String leftChildName, 
            String rightChildName,
            BiFunction<Graph, Graph, Graph> operation) {

        Graph leftEntryGraph = entryGraphs.get(leftChildName);
        Graph rightEntryGraph = entryGraphs.get(rightChildName);
        Graph newEntryGraph = operation.apply(leftEntryGraph, rightEntryGraph);
        entryGraphs.replace(leftChildName, newEntryGraph);

        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph rightExitGraph = exitGraphs.get(rightChildName);
        Graph newExitGraph = operation.apply(leftExitGraph, rightExitGraph);
        exitGraphs.replace(leftChildName, newExitGraph);
    }

    private void handleSequence(String leftChildName, String rightChildName, Mode mode, WorkspaceEntry we) {
        Graph inputGraph = exitGraphs.get(leftChildName);
        Graph outputGraph = entryGraphs.get(rightChildName);

        exitGraphs.replace(leftChildName, exitGraphs.get(rightChildName));

        DrawVisualObjectsRequest request = new DrawVisualObjectsRequest(
                inputGraph,
                outputGraph,
                true,
                false,
                mode,
                we
        );

        visualModelDrawingTool.drawVisualObjects(request);
    }

    private void handleIteration(String leftChildName, int nodeCounter) {
        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph leftExitGraphClone = leftExitGraph.deepClone(nodeCounter);
        Graph newEntryGraph = GraphUtils.join(leftExitGraph, leftExitGraphClone);
        entryGraphs.replace(leftChildName, newEntryGraph);
    }
}
