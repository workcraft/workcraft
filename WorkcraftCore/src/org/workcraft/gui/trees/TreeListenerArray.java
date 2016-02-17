package org.workcraft.gui.trees;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.gui.workspace.Path;

public class TreeListenerArray<Node> implements TreeListener<Node> {
    List<TreeListener<Node>> list = new ArrayList<TreeListener<Node>>();

    @Override
    public void added(Path<Node> path) {
        for (TreeListener<Node> l : list) {
            l.added(path);
        }
    }

    @Override
    public void changed(Path<Node> path) {
        for (TreeListener<Node> l : list) {
            l.changed(path);
        }
    }

    @Override
    public void removed(Path<Node> path) {
        for (TreeListener<Node> l : list) {
            l.removed(path);
        }
    }

    @Override
    public void restructured(Path<Node> path) {
        for (TreeListener<Node> l : list) {
            l.restructured(path);
        }
    }

    public void add(TreeListener<Node> listener) {
        list.add(listener);
    }

    public void remove(TreeListener<Node> listener) {
        list.remove(listener);
    }
}
