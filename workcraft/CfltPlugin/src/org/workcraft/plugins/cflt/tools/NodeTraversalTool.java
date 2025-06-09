package org.workcraft.plugins.cflt.tools;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.cflt.node.Node;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.NodeIterator;
import org.workcraft.plugins.cflt.node.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.GraphUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NodeTraversalTool {
    NodeCollection nodeCollection;

    HashMap<String, Graph> entryGraph = new HashMap<>();
    HashMap<String, Graph> exitGraph = new HashMap<>();

    VisualModelDrawingTool visualModelDrawingTool;

    public NodeTraversalTool(NodeCollection nodeCollection, Model model) {
        this.nodeCollection = nodeCollection;
        if (model == Model.PETRI_NET) visualModelDrawingTool = new PetriDrawingTool(nodeCollection);
        if (model == Model.STG) visualModelDrawingTool = new StgDrawingTool(nodeCollection);
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

            ensureGraphContainsVertex(entryGraph, leftChildName);
            ensureGraphContainsVertex(entryGraph, rightChildName);
            ensureGraphContainsVertex(exitGraph, leftChildName);
            ensureGraphContainsVertex(exitGraph, rightChildName);

            switch (operator) {
                case CONCURRENCY -> this.handleConcurrency(leftChildName, rightChildName);
                case CHOICE -> this.handleChoice(leftChildName, rightChildName);
                case SEQUENCE -> this.handleSequence(leftChildName, rightChildName, mode, we);
                case ITERATION -> this.handleIteration(leftChildName, nodeIterator.getCurrentPosition());
            }

            if (nodeIterator.isLastNode()) {
                visualModelDrawingTool.drawVisualObjects(entryGraph.get(leftChildName), new Graph(), false, true, mode, we);
            }
        }
    }

    private void drawSingleTransition(WorkspaceEntry we) {
        visualModelDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
    }

    private void ensureGraphContainsVertex(Map<String, Graph> graphNameToGraph, String vertexName) {
        if (!graphNameToGraph.containsKey(vertexName)) {
            graphNameToGraph.put(vertexName, new Graph());
            graphNameToGraph.get(vertexName).addVertexName(vertexName);
        }
    }

    private void handleConcurrency(String leftChildName, String rightChildName) {
        Graph leftEntryGraph = entryGraph.get(leftChildName);
        Graph rightEntryGraph = entryGraph.get(rightChildName);
        Graph newEntryGraph =  GraphUtils.disjointUnion(leftEntryGraph, rightEntryGraph);
        entryGraph.replace(leftChildName, newEntryGraph);

        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph rightExitGraph = exitGraph.get(rightChildName);
        Graph newExitGraph =  GraphUtils.disjointUnion(leftExitGraph, rightExitGraph);
        exitGraph.replace(leftChildName, newExitGraph);
    }

    private void handleChoice(String leftChildName, String rightChildName) {
        Graph leftEntryGraph = entryGraph.get(leftChildName);
        Graph rightEntryGraph = entryGraph.get(rightChildName);
        Graph newEntryGraph =  GraphUtils.join(leftEntryGraph, rightEntryGraph);
        entryGraph.replace(leftChildName, newEntryGraph);

        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph rightExitGraph = exitGraph.get(rightChildName);
        Graph newExitGraph =  GraphUtils.join(leftExitGraph, rightExitGraph);
        exitGraph.replace(leftChildName, newExitGraph);
    }

    private void handleSequence(String leftChildName, String rightChildName, Mode mode, WorkspaceEntry we) {
        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph righEntryGraph = entryGraph.get(rightChildName);
        exitGraph.replace(leftChildName, exitGraph.get(rightChildName));
        visualModelDrawingTool.drawVisualObjects(leftExitGraph, righEntryGraph, true, false, mode, we);
    }

    private void handleIteration(String leftChildName, int nodeCounter) {
        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph leftExitGraphClone = leftExitGraph.cloneGraph(nodeCounter);
        Graph newEntryGraph =  GraphUtils.join(leftExitGraph, leftExitGraphClone);
        entryGraph.replace(leftChildName, newEntryGraph);
    }
}
