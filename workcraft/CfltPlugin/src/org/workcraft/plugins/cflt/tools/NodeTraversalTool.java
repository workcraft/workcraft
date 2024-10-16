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

    private final NodeCollection nodeCollection = NodeCollection.getInstance();

    HashMap<String, Graph> entryGraph = new HashMap<>();
    HashMap<String, Graph> exitGraph = new HashMap<>();

    PetriDrawingTool petriDrawingTool = new PetriDrawingTool();
    StgDrawingTool stgDrawingTool = new StgDrawingTool();

    public void drawSingleTransition(Model model, WorkspaceEntry we) {
        switch (model) {
        case PETRI_NET:
            petriDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
            break;
        case STG:
            stgDrawingTool.drawSingleTransition(nodeCollection.getSingleTransition(), we);
            break;
        }
    }

    public void drawInterpretedGraph(Mode mode, Model model, WorkspaceEntry we) {
        NodeIterator nodeIterator = nodeCollection.getNodeIterator();
        while (nodeIterator.hasNext()) {
            Node node = nodeIterator.next();

            String leftChildName = node.getLeftChildName();
            String rightChildName = node.getRightChildName();
            Operator operator = node.getOperator();

            ensureGraphContainsVertex(entryGraph, leftChildName);
            ensureGraphContainsVertex(entryGraph, rightChildName);
            ensureGraphContainsVertex(exitGraph, leftChildName);
            ensureGraphContainsVertex(exitGraph, rightChildName);

            switch (operator) {
            case CONCURRENCY:
                this.handleConcurrency(leftChildName, rightChildName);
                break;
            case CHOICE:
                this.handleChoice(leftChildName, rightChildName);
                break;
            case SEQUENCE:
                this.handleSequence(leftChildName, rightChildName, model, mode, we);
                break;
            case ITERATION:
                this.handleIteration(leftChildName, nodeIterator.getCurrentPosition());
                break;
            }

            if (nodeIterator.isLastNode()) {
                switch (model) {
                case PETRI_NET:
                    petriDrawingTool.drawPetri(entryGraph.get(leftChildName), new Graph(),
                            false, true, mode, we);
                    break;
                case STG:
                    stgDrawingTool.drawStg(entryGraph.get(leftChildName), new Graph(),
                            false, true, mode, we);
                    break;
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
        Graph eG =  GraphUtils.disjointUnion(entryGraph.get(leftChildName), entryGraph.get(rightChildName));
        entryGraph.replace(leftChildName, eG);
        Graph xG =  GraphUtils.disjointUnion(exitGraph.get(leftChildName), exitGraph.get(rightChildName));
        exitGraph.replace(leftChildName, xG);
    }

    private void handleChoice(String leftChildName, String rightChildName) {
        Graph eG =  GraphUtils.join(entryGraph.get(leftChildName), entryGraph.get(rightChildName));
        entryGraph.replace(leftChildName, eG);
        Graph xG =  GraphUtils.join(exitGraph.get(leftChildName), exitGraph.get(rightChildName));
        exitGraph.replace(leftChildName, xG);
    }

    private void handleSequence(String leftChildName, String rightChildName, Model model, Mode mode, WorkspaceEntry we) {
        switch (model) {
        case PETRI_NET:
            petriDrawingTool.drawPetri(exitGraph.get(leftChildName), entryGraph.get(rightChildName),
                    true, false, mode, we);
            break;
        case STG:
            stgDrawingTool.drawStg(exitGraph.get(leftChildName), entryGraph.get(rightChildName),
                    true, false, mode, we);
            break;
        }
        exitGraph.replace(leftChildName, exitGraph.get(rightChildName));
    }

    private void handleIteration(String leftChildName, int nodeCounter) {
        Graph clone = exitGraph.get(leftChildName).cloneGraph(nodeCounter);
        Graph eG =  GraphUtils.join(entryGraph.get(leftChildName), clone);
        entryGraph.replace(leftChildName, eG);
    }
}
