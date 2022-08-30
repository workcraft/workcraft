package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;

import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.Node;
import org.workcraft.plugins.cflt.Operator;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.GraphUtils;

public class CotreeTool {

    public static ArrayList<Node> nodes;
    public static String singleTransition;
    public static boolean containsIteration;

    HashMap<String, Graph> entryGraph = new HashMap<>();
    HashMap<String, Graph> exitGraph = new HashMap<>();
    PetriDrawingTool pdt = new PetriDrawingTool();
    StgDrawingTool sdt = new StgDrawingTool();

    public void reset() {
        CotreeTool.nodes = new ArrayList<>();
        CotreeTool.singleTransition = null;
        CotreeTool.containsIteration = false;
    }
    public enum Model {
        PETRI_NET, STG, DEFAULT;
    }
    public void drawSingleTransition(Model model) {
        if (model == Model.PETRI_NET) {
            pdt.drawSingleTransition(singleTransition);
        } else if (model == Model.STG) {
            sdt.drawSingleTransition(singleTransition);
        }
    }
    public void drawInterpretedGraph(Mode mode, Model model) {

        int counter = 0;
        for (Node node : nodes) {

            String a = node.getLeft();
            String b = node.getRight();
            Operator o = node.getOperator();

            if (!entryGraph.containsKey(a)) {
                entryGraph.put(a, new Graph());
                entryGraph.get(a).addVertex(a);
            }
            if (!entryGraph.containsKey(b)) {
                entryGraph.put(b, new Graph());
                entryGraph.get(b).addVertex(b);
            }
            if (!exitGraph.containsKey(a)) {
                exitGraph.put(a, new Graph());
                exitGraph.get(a).addVertex(a);
            }
            if (!exitGraph.containsKey(b)) {
                exitGraph.put(b, new Graph());
                exitGraph.get(b).addVertex(b);
            }

            if (o == Operator.CONCURRENCY) {
                Graph eG =  GraphUtils.disjointUnion(entryGraph.get(a), entryGraph.get(b));
                entryGraph.replace(a, eG);
                Graph xG =  GraphUtils.disjointUnion(exitGraph.get(a), exitGraph.get(b));
                exitGraph.replace(a, xG);

            } else if (o == Operator.CHOICE) {
                Graph eG =  GraphUtils.join(entryGraph.get(a), entryGraph.get(b));
                entryGraph.replace(a, eG);
                Graph xG =  GraphUtils.join(exitGraph.get(a), exitGraph.get(b));
                exitGraph.replace(a, xG);

            } else if (o == Operator.SEQUENCE) {
                if (model == Model.PETRI_NET) {
                    pdt.drawPetri(exitGraph.get(a), entryGraph.get(b), true, false,  mode);
                } else if (model == Model.STG) {
                    sdt.drawStg(exitGraph.get(a), entryGraph.get(b), true, false, mode);
                }
                exitGraph.replace(a, exitGraph.get(b));

            } else if (o == Operator.ITERATION) {

                Graph clone = exitGraph.get(a).cloneGraph(counter);
                Graph eG =  GraphUtils.join(entryGraph.get(a), clone);

                //Graph xG = eG;
                //exitGraph.replace(a, xG);
                entryGraph.replace(a, eG);
            }
            //if the node is the root node
            if (counter == nodes.size() - 1) {
                if (model == Model.PETRI_NET) {
                    pdt.drawPetri(entryGraph.get(a), new Graph(), false, true, mode);
                } else if (model == Model.STG) {
                    sdt.drawStg(entryGraph.get(a), new Graph(), false, true, mode);
                }
            }
            counter++;
        }
    }
}
