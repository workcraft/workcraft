package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.plugins.cflt.utils.GraphUtils;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.NodeIterator;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.workspace.WorkspaceEntry;

public class NodeTraversalTool {
    NodeCollection nodeCollection;

    HashMap<String, Graph> entryGraphs = new HashMap<>();
    HashMap<String, Graph> exitGraphs = new HashMap<>();

    VisualModelDrawingTool visualModelDrawingTool;

    public NodeTraversalTool(NodeCollection nodeCollection, Model model) {
        this.nodeCollection = nodeCollection;
        this.visualModelDrawingTool = this.getDrawingTool(model, nodeCollection);
    }

    private VisualModelDrawingTool getDrawingTool(Model model, NodeCollection nodeCollection) {
        return switch (model) {
            case PETRI_NET -> new PetriDrawingTool(nodeCollection);
            case STG -> new StgDrawingTool(nodeCollection);
        };
    }

    public void drawInterpretedGraph(Mode mode, Model model, WorkspaceEntry we) {
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
                visualModelDrawingTool.drawVisualObjects(entryGraphs.get(leftChildName), new Graph(), false, true, mode, we);
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
        Graph leftEntryGraph = entryGraphs.get(leftChildName);
        Graph rightEntryGraph = entryGraphs.get(rightChildName);
        Graph newEntryGraph = GraphUtils.disjointUnion(leftEntryGraph, rightEntryGraph);
        entryGraphs.replace(leftChildName, newEntryGraph);

        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph rightExitGraph = exitGraphs.get(rightChildName);
        Graph newExitGraph = GraphUtils.disjointUnion(leftExitGraph, rightExitGraph);
        exitGraphs.replace(leftChildName, newExitGraph);
    }

    private void handleChoice(String leftChildName, String rightChildName) {
        Graph leftEntryGraph = entryGraphs.get(leftChildName);
        Graph rightEntryGraph = entryGraphs.get(rightChildName);
        Graph newEntryGraph = GraphUtils.join(leftEntryGraph, rightEntryGraph);
        entryGraphs.replace(leftChildName, newEntryGraph);

        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph rightExitGraph = exitGraphs.get(rightChildName);
        Graph newExitGraph = GraphUtils.join(leftExitGraph, rightExitGraph);
        exitGraphs.replace(leftChildName, newExitGraph);
    }

    private void handleSequence(String leftChildName, String rightChildName, Mode mode, WorkspaceEntry we) {
        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph righEntryGraph = entryGraphs.get(rightChildName);
        exitGraphs.replace(leftChildName, exitGraphs.get(rightChildName));
        visualModelDrawingTool.drawVisualObjects(leftExitGraph, righEntryGraph, true, false, mode, we);
    }

    private void handleIteration(String leftChildName, int nodeCounter) {
        Graph leftExitGraph = exitGraphs.get(leftChildName);
        Graph leftExitGraphClone = leftExitGraph.deepClone(nodeCounter);
        Graph newEntryGraph = GraphUtils.join(leftExitGraph, leftExitGraphClone);
        entryGraphs.replace(leftChildName, newEntryGraph);
    }
}
