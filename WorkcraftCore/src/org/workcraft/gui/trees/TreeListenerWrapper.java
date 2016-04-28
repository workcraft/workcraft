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
