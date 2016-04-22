package org.workcraft.plugins.pog.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.pog.Pog;
import org.workcraft.plugins.pog.Vertex;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityCheckerTool extends VerificationTool {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable state";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Pog;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Pog pog = (Pog) we.getModelEntry().getMathModel();
        HashSet<Vertex> unreachable = checkReachability(pog);
        if (unreachable.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The graph does not have unreachable vertices.",
                    TITLE, JOptionPane.INFORMATION_MESSAGE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(pog, (Collection) unreachable);
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "The graph has unreachable vertices:\n" + refStr + "\n\nSelect unreachable vertices?",
                    TITLE, JOptionPane.WARNING_MESSAGE + JOptionPane.YES_NO_OPTION) == 0) {

                VisualModel visualPog = we.getModelEntry().getVisualModel();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualPog, (HashSet) unreachable);
            }
        }
    }

    private HashSet<Vertex> checkReachability(final Pog pog) {
        Queue<Vertex> queue = new LinkedList<>();
        for (Vertex vertex: pog.getVertices()) {
            if (pog.getPreset(vertex).isEmpty()) {
                queue.add(vertex);
            }
        }

        HashSet<Vertex> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            Vertex vertex = queue.remove();
            if (visited.contains(vertex)) continue;
            visited.add(vertex);
            for (Node node: pog.getPostset(vertex)) {
                if ((node instanceof Vertex) && visited.containsAll(pog.getPreset(node))) {
                    queue.add((Vertex) node);
                }
            }
        }

        HashSet<Vertex> unreachable = new HashSet<>(pog.getVertices());
        unreachable.removeAll(visited);
        return unreachable;
    }

}
