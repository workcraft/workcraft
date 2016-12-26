package org.workcraft.gui.trees;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.workcraft.gui.workspace.Path;

class TreeListenerWrapper<Node> implements TreeListener<Node> {

    private final TreeModelListener l;

    TreeListenerWrapper(TreeModelListener l) {
        this.l = l;
    }

    @Override
    public void added(Path<Node> path) {
        l.treeNodesInserted(tme(path));
    }

    private TreeModelEvent tme(Path<Node> path) {
        return new TreeModelEvent(this, path(path));
    }

    private Object[] path(Path<Node> path) {
        return Path.getPath(path).toArray();
    }

    @Override
    public void changed(Path<Node> path) {
        l.treeNodesChanged(tme(path));
    }

    @Override
    public void removed(Path<Node> path) {
        l.treeNodesRemoved(tme(path));
    }

    @Override
    public void restructured(Path<Node> path) {
        l.treeStructureChanged(tme(path));
    }

}
