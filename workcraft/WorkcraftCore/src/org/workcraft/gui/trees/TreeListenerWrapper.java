package org.workcraft.gui.trees;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.workcraft.gui.workspace.Path;

class TreeListenerWrapper<T> implements TreeListener<T> {

    private final TreeModelListener l;

    TreeListenerWrapper(TreeModelListener l) {
        this.l = l;
    }

    @Override
    public void added(Path<T> path) {
        l.treeNodesInserted(tme(path));
    }

    @Override
    public void changed(Path<T> path) {
        l.treeNodesChanged(tme(path));
    }

    @Override
    public void removed(Path<T> path) {
        l.treeNodesRemoved(tme(path));
    }

    @Override
    public void restructured(Path<T> path) {
        l.treeStructureChanged(tme(path));
    }

    private TreeModelEvent tme(Path<T> path) {
        return new TreeModelEvent(this, path(path));
    }

    private Object[] path(Path<T> path) {
        return Path.getPath(path).toArray();
    }

}
