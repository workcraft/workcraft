package org.workcraft.gui.workspace;

import org.workcraft.gui.trees.TreeDecorator;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;

public class WorkspaceTreeDecorator implements TreeDecorator<Path<String>> {
    private final Workspace workspace;

    public WorkspaceTreeDecorator(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Icon getIcon(Path<String> node) {
        return null;
    }

    @Override
    public String getName(Path<String> node) {
        String result = "";
        if (node.isEmpty()) {
            result = "!Workspace";
        } else {
            WorkspaceEntry we = workspace.getWork(node);
            if ((we != null) && we.isChanged()) {
                result = "*";
            }
            result += node.getNode();
        }
        return result;
    }
}
