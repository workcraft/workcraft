package org.workcraft.workspace;
import org.workcraft.gui.trees.TreeListener;
import org.workcraft.gui.workspace.Path;

import java.util.EventListener;

public class WorkspaceListener implements EventListener {

    private final TreeListener<Path<String>> treeListener;
    private final Path<Path<String>> rootPath;

    public WorkspaceListener(WorkspaceTree workspaceTree, TreeListener<Path<String>> treeListener) {
        this.rootPath = Path.root(workspaceTree.getRoot());
        this.treeListener = treeListener;
    }

    public void entryAdded(WorkspaceEntry we) {
        treeListener.restructured(rootPath);
    }

    public void entryChanged(WorkspaceEntry we) {
        treeListener.restructured(rootPath);
    }

    public void entryRemoved(WorkspaceEntry we) {
        treeListener.restructured(rootPath);
    }

    public void workspaceSaved() {
        treeListener.restructured(rootPath);
    }

    public void workspaceLoaded() {
        treeListener.restructured(rootPath);
    }

}
