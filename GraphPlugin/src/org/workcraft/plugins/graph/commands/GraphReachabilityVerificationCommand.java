package org.workcraft.plugins.graph.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class GraphReachabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Graph.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Graph graph = WorkspaceUtils.getAs(we, Graph.class);
        HashSet<Vertex> unreachable = checkReachability(graph);
        if (unreachable.isEmpty()) {
            DialogUtils.showInfo("The graph does not have unreachable vertices.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(graph, (Collection) unreachable, 50);
            String msg = "The graph has unreachable vertices:\n" + refStr + "\n\nSelect unreachable vertices?";
            if (DialogUtils.showConfirm(msg, TITLE)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualModel visualGraph = we.getModelEntry().getVisualModel();
                SelectionHelper.selectByReferencedComponents(visualGraph, (HashSet) unreachable);
            }
        }
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
