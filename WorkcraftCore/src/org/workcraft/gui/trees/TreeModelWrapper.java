package org.workcraft.gui.trees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.workcraft.gui.workspace.Path;

final class TreeModelWrapper<Node> implements TreeModel {

    private final TreeSource<Node> source;
    Map<TreeModelListener, TreeListenerWrapper<Node>> listeners = new HashMap<>();

    public void update(Path<Node> path) {
        for (TreeListenerWrapper<Node> l : listeners.values()) {
            l.restructured(path);
        }
    }

    TreeModelWrapper(TreeSource<Node> source) {
        this.source = source;
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        source.addListener(wrap(l));
    }

    private TreeListenerWrapper<Node> wrap(final TreeModelListener l) {
        TreeListenerWrapper<Node> result = listeners.get(l);
        if (result == null) {
            listeners.put(l, result = new TreeListenerWrapper<Node>(l));
        }
        return result;
    }

    @Override
    public Object getChild(Object parent, int index) {
        Object result = null;
        List<Node> children = source.getChildren(cast(parent));
        if (index < children.size()) {
            result = children.get(index);
        }
        return result;
    }

    @Override
    public int getChildCount(Object parent) {
        return getChildren(parent).size();
    }

    private List<Node> getChildren(Object parent) {
        return source.getChildren(cast(parent));
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getChildren(parent).indexOf(child);
    }

    @Override
    public Object getRoot() {
        return source.getRoot();
    }

    @Override
    public boolean isLeaf(Object node) {
        return source.isLeaf(cast(node));
    }

    @SuppressWarnings("unchecked")
    private Node cast(Object node) {
        return (Node) node;
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        source.removeListener(wrap(l));
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new org.workcraft.exceptions.NotSupportedException();
    }

}
