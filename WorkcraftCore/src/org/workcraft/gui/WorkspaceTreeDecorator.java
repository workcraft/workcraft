package org.workcraft.gui;

import javax.swing.Icon;

import org.workcraft.gui.trees.TreeDecorator;
import org.workcraft.gui.workspace.Path;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

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
        final WorkspaceEntry openFile = workspace.getWork(node);
        String name = node.isEmpty() ? "!Workspace" : node.getNode();
        if (openFile != null && openFile.isChanged()) {
            name += " *";
        }
        return name;
    }
}
