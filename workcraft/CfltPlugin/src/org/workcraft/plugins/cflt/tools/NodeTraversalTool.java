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

    PetriDrawingTool petriDrawingTool;
    StgDrawingTool stgDrawingTool;

    public NodeTraversalTool(NodeCollection nodeCollection) {
        this.nodeCollection = nodeCollection;
        petriDrawingTool = new PetriDrawingTool(nodeCollection);
        stgDrawingTool = new StgDrawingTool(nodeCollection);
    }

    public void drawSingleTransition(Model model, WorkspaceEntry we) {
        switch (model) {
        case PETRI_NET -> petriDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
        case STG -> stgDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
        }
    }

    public void drawInterpretedGraph(Mode mode, Model model, WorkspaceEntry we) {
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
            case SEQUENCE -> this.handleSequence(leftChildName, rightChildName, model, mode, we);
            case ITERATION -> this.handleIteration(leftChildName, nodeIterator.getCurrentPosition());
            }

            if (nodeIterator.isLastNode()) {
                switch (model) {
                case PETRI_NET -> petriDrawingTool.drawPetri(entryGraph.get(leftChildName), new Graph(), false, true, mode, we);
                case STG -> stgDrawingTool.drawStg(entryGraph.get(leftChildName), new Graph(), false, true, mode, we);
                }
            }
        }
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

    private void handleSequence(String leftChildName, String rightChildName, Model model, Mode mode, WorkspaceEntry we) {
        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph righEntryGraph = entryGraph.get(rightChildName);
        switch (model) {
        case PETRI_NET -> petriDrawingTool.drawPetri(leftExitGraph, righEntryGraph, true, false, mode, we);
        case STG -> stgDrawingTool.drawStg(leftExitGraph, righEntryGraph, true, false, mode, we);
        }
        exitGraph.replace(leftChildName, exitGraph.get(rightChildName));
    }

    private void handleIteration(String leftChildName, int nodeCounter) {
        Graph leftExitGraph = exitGraph.get(leftChildName);
        Graph leftExitGraphClone = leftExitGraph.cloneGraph(nodeCounter);
        Graph newEntryGraph =  GraphUtils.join(leftExitGraph, leftExitGraphClone);
        entryGraph.replace(leftChildName, newEntryGraph);
    }
}
