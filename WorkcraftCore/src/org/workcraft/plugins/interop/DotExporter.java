package org.workcraft.plugins.interop;

import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.layout.DotLayoutSettings;
import org.workcraft.util.Hierarchy;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DotExporter implements Exporter {

    private class ExportNode {
        public final String id;
        public final double width;
        public final double height;
        public final Collection<String> destinations;
        public final String comment;

        ExportNode(String id, double width, double height, Collection<String> destinations, String comment) {
            this.id = id;
            this.width = width;
            this.height = height;
            this.destinations = destinations;
            this.comment = comment;
        }
    }

    public static void export(Collection<ExportNode> nodes, OutputStream outStream) throws IOException {
        PrintStream out = new PrintStream(outStream);

        out.println("digraph work {");
        double nodesep = DotLayoutSettings.getNodesep();
        double ranksep = DotLayoutSettings.getRanksep();
        out.println("graph [overlap=\"false\", splines=\"true\", nodesep=\"" + nodesep + "\", ranksep=\"" + ranksep + "\"];");
        //out.println("graph [overlap=\"false\", splines=\"ortho\", nodesep=\"" + nodesep + "\", ranksep=\"" + ranksep + "\"];");
        out.println("rankdir=" + DotLayoutSettings.getRankdir().value);
        out.println("node [shape=box];");

        for (ExportNode node : nodes) {
            out.println("\"" + node.id + "\" [width=\"" + node.width + "\", height=\"" + node.height + "\", fixedsize=\"true\"];  // " + node.comment);
            for (String target : node.destinations) {
                out.println("\"" + node.id + "\" -> \"" + target + "\";");
            }
        }
        out.println("}");
    }

    @Override
    public void export(Model model, OutputStream outStream) throws IOException,
            ModelValidationException, SerialisationException {

        if (!(model instanceof VisualModel)) {
            throw new SerialisationException("Non-visual model cannot be exported to Graphviz DOT file.");
        }
        VisualModel visualModel = (VisualModel) model;
        final List<ExportNode> dotExportNodes = new ArrayList<>();
        Collection<VisualComponent> components = Hierarchy.getChildrenOfType(visualModel.getRoot(), VisualComponent.class);
        for (VisualComponent component: components) {
            if (!component.getChildren().isEmpty()) {
                throw new SerialisationException("Hierarchical model cannot be exported to Graphviz DOT file.");
            }
        }
        for (VisualComponent component: components) {
            String id = visualModel.getNodeReference(component);
            Rectangle2D bb = component.getBoundingBoxInLocalSpace();
            if ((id != null) && (bb != null)) {
                List<String> destinations = new ArrayList<>();
                Set<VisualNode> postset = visualModel.getPostset(component);
                for (VisualNode target : postset) {
                    String targetId = visualModel.getNodeReference(target);
                    if (targetId != null) {
                        destinations.add(targetId);
                    }
                }
                String comment = visualModel.getNodeMathReference(component);
                ExportNode dotExportNode = new ExportNode(id, bb.getWidth(), bb.getHeight(), destinations, comment);
                dotExportNodes.add(dotExportNode);
            }
        }
        export(dotExportNodes, outStream);
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
