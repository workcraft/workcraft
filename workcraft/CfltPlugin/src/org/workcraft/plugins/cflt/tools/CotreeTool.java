package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.cflt.Node;
import org.workcraft.plugins.cflt.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.GraphUtils;

public final class CotreeTool {

    // TODO: Make this class a singleton, or find a different way to avoid using static variables to avoid using static variables
    public static ArrayList<Node> nodes;
    public static String singleTransition;
    public static boolean containsIteration;

    HashMap<String, Graph> entryGraph = new HashMap<>();
    HashMap<String, Graph> exitGraph = new HashMap<>();
    PetriDrawingTool petriDrawingTool = new PetriDrawingTool();
    StgDrawingTool stgDrawingTool = new StgDrawingTool();

    public void reset() {
        CotreeTool.nodes = new ArrayList<>();
        CotreeTool.singleTransition = null;
        CotreeTool.containsIteration = false;
    }

    public void drawSingleTransition(Model model) {
        switch (model) {
        case PETRI_NET:
            petriDrawingTool.drawSingleTransition(singleTransition);
            break;
        case STG:
            stgDrawingTool.drawSingleTransition(singleTransition);
            break;
        }
    }

    public void drawInterpretedGraph(Mode mode, Model model) {
        int nodeCounter = 0;

        for (Node node : nodes) {
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
                this.handleSequence(leftChildName, rightChildName, model, mode);
                break;
            case ITERATION:
                this.handleIteration(leftChildName, nodeCounter);
                break;
            }

            if (nodeCounter == nodes.size() - 1) {
                switch (model) {
                case PETRI_NET:
                    petriDrawingTool.drawPetri(entryGraph.get(leftChildName), new Graph(), false, true, mode);
                    break;
                case STG:
                    stgDrawingTool.drawStg(entryGraph.get(leftChildName), new Graph(), false, true, mode);
                    break;
                }
            }
            nodeCounter++;
        }
    }

    private void ensureGraphContainsVertex(Map<String, Graph> graphNameToGraph, String vertexName) {
        if (!graphNameToGraph.containsKey(vertexName)) {
            graphNameToGraph.put(vertexName, new Graph());
            graphNameToGraph.get(vertexName).addVertex(vertexName);
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

    private void handleSequence(String leftChildName, String rightChildName, Model model, Mode mode) {
        switch (model) {
        case PETRI_NET:
            petriDrawingTool.drawPetri(exitGraph.get(leftChildName), entryGraph.get(rightChildName), true, false,  mode);
            break;
        case STG:
            stgDrawingTool.drawStg(exitGraph.get(leftChildName), entryGraph.get(rightChildName), true, false, mode);
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
