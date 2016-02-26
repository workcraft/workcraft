/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.gui.trees;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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

    @Override public void changed(Path<Node> path) {
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

    @Override public void addTreeModelListener(final TreeModelListener l) {
        source.addListener(wrap(l));
    }

    private TreeListenerWrapper<Node> wrap(final TreeModelListener l) {
        TreeListenerWrapper<Node> result = listeners.get(l);
        if (result == null) {
            listeners.put(l, result = new TreeListenerWrapper<Node>(l));
        }
        return result;
    }

    @Override public Object getChild(Object parent, int index) {
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

    @Override public int getIndexOfChild(Object parent, Object child) {
        return getChildren(parent).indexOf(child);
    }

    @Override public Object getRoot() {
        return source.getRoot();
    }

    @Override public boolean isLeaf(Object node) {
        return source.isLeaf(cast(node));
    }

    @SuppressWarnings("unchecked")
    private Node cast(Object node) {
        return (Node) node;
    }

    @Override public void removeTreeModelListener(TreeModelListener l) {
        source.removeListener(wrap(l));
    }

    @Override public void valueForPathChanged(TreePath path, Object newValue) {
        throw new org.workcraft.exceptions.NotSupportedException();
    }

}
