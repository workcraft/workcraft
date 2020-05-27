package org.workcraft.plugins.builtin.interop;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.builtin.settings.DotLayoutSettings;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;

public class DotExporter implements Exporter {

    private static final String INDENT = "  ";

    @Override
    public void export(Model model, OutputStream outStream) throws SerialisationException {
        if (!(model instanceof VisualModel)) {
            throw new SerialisationException("Non-visual model cannot be exported to Graphviz DOT file.");
        }
        for (VisualNode node : Hierarchy.getDescendantsOfType(model.getRoot(), VisualNode.class)) {
            if ((node instanceof Container) && !(node instanceof VisualGroup) && !(node instanceof VisualPage)) {
                throw new SerialisationException("Models with ports cannot be exported to Graphviz DOT file.");
            }
        }
        exportGraph((VisualModel) model, new PrintStream(outStream));
    }

    private void exportGraph(VisualModel model, PrintStream out) {
        double nodesep = DotLayoutSettings.getNodesep();
        double ranksep = DotLayoutSettings.getRanksep();
        String rankdir = DotLayoutSettings.getRankdir().value;
        out.println("digraph work {");
        out.println(INDENT + "graph [overlap=false, splines=true, " +
                "nodesep=" + nodesep + ", ranksep=" + ranksep + ", " + "rankdir=" + rankdir + "];");

        out.println(INDENT + "node [shape=box, fixedsize=true];");
        out.println();
        exportNodes(model, model.getRoot().getChildren(), out, INDENT);
        out.println("}");
    }

    private void exportNodes(VisualModel model, Collection<Node> nodes, PrintStream out, String indent) {
        for (Node node : nodes) {
            if ((node instanceof VisualGroup) || (node instanceof VisualPage)) {
                exportSubgraph(model, node, out, indent);
            } else if (node instanceof VisualComponent) {
                exportVertex(model, (VisualComponent) node, out, indent);
            } else if (node instanceof VisualConnection) {
                exportArc(model, (VisualConnection) node, out, indent);
            }
        }
    }

    private void exportSubgraph(VisualModel model, Node parent, PrintStream out, String indent) {
        out.println(indent + "subgraph cluster_" + model.getName(parent) + " {");
        exportNodes(model, parent.getChildren(), out, indent + INDENT);
        out.println(indent + "}");
    }

    private void exportVertex(VisualModel model, VisualComponent component, PrintStream out, String indent) {
        String ref = model.getNodeReference(component);
        Rectangle2D bb = component.getBoundingBoxInLocalSpace();
        String w = String.format("%.3f", bb.getWidth());
        String h = String.format("%.3f", bb.getHeight());
        String comment = model.getMathReference(component);
        out.println(indent + ref + " [width=" + w + ", height=" + h + "];  // " + comment);
    }

    private void exportArc(VisualModel model, VisualConnection connection, PrintStream out, String indent) {
        VisualNode first = connection.getFirst();
        VisualNode second = connection.getSecond();
        String firstRef = model.getNodeReference(first);
        String secondRef = model.getNodeReference(second);
        String comment = "(" + model.getMathReference(first) + ", " + model.getMathReference(second) + ")";
        out.println(indent + firstRef + " -> " + secondRef + "; // " + comment);
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof VisualModel;
    }

    @Override
    public DotFormat getFormat() {
        return DotFormat.getInstance();
    }

}
