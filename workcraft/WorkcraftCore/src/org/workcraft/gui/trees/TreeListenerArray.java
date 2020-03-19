package org.workcraft.gui.trees;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.gui.workspace.Path;

public class TreeListenerArray<T> implements TreeListener<T> {

    private final List<TreeListener<T>> list = new ArrayList<>();

    @Override
    public void added(Path<T> path) {
        for (TreeListener<T> l : list) {
            l.added(path);
        }
    }

    @Override
    public void changed(Path<T> path) {
        for (TreeListener<T> l : list) {
            l.changed(path);
        }
    }

    @Override
    public void removed(Path<T> path) {
        for (TreeListener<T> l : list) {
            l.removed(path);
        }
    }

    @Override
    public void restructured(Path<T> path) {
        for (TreeListener<T> l : list) {
            l.restructured(path);
        }
    }

    public void add(TreeListener<T> listener) {
        list.add(listener);
    }

    public void remove(TreeListener<T> listener) {
        list.remove(listener);
    }
}
