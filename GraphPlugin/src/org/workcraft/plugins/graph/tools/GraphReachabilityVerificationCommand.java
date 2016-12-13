package org.workcraft.plugins.graph.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.AbstractVerificationCommand;
import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphReachabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof Graph;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final Graph graph = (Graph) me.getMathModel();
        HashSet<Vertex> unreachable = checkReachability(graph);
        if (unreachable.isEmpty()) {
            LogUtils.logInfoLine("The graph does not have unreachable vertices.");
        } else {
            String refStr = ReferenceHelper.getNodesAsString(graph, (Collection) unreachable);
            LogUtils.logWarningLine("The graph has unreachable vertices:\n" + refStr);
        }
        return me; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Graph graph = (Graph) we.getModelEntry().getMathModel();
        HashSet<Vertex> unreachable = checkReachability(graph);
        if (unreachable.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The graph does not have unreachable vertices.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(graph, (Collection) unreachable);
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "The graph has unreachable vertices:\n" + refStr + "\n\nSelect unreachable vertices?",
                    TITLE, JOptionPane.WARNING_MESSAGE + JOptionPane.YES_NO_OPTION) == 0) {

                VisualModel visualGraph = we.getModelEntry().getVisualModel();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualGraph, (HashSet) unreachable);
            }
        }
        return we;
    }

    private HashSet<Vertex> checkReachability(final Graph graph) {
        Queue<Vertex> queue = new LinkedList<>();
        for (Vertex vertex: graph.getVertices()) {
            if (graph.getPreset(vertex).isEmpty()) {
                queue.add(vertex);
            }
        }

        HashSet<Vertex> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Vertex vertex = queue.remove();
            if (visited.contains(vertex)) continue;
            visited.add(vertex);
            for (Node node: graph.getPostset(vertex)) {
                if ((node instanceof Vertex) && visited.containsAll(graph.getPreset(node))) {
                    queue.add((Vertex) node);
                }
            }
        }

        HashSet<Vertex> unreachable = new HashSet<>(graph.getVertices());
        unreachable.removeAll(visited);
        return unreachable;
    }

}
