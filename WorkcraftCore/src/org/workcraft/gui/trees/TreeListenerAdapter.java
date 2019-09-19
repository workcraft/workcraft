package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

public class TreeListenerAdapter<T> implements TreeListener<T> {

    private final TreeListener<T> chain;

    public TreeListenerAdapter(TreeListener<T> chain) {
        this.chain = chain;
    }

    @Override
    public void added(Path<T> path) {
        chain.added(path);
    }

    @Override
    public void changed(Path<T> path) {
        chain.changed(path);
    }

    @Override
    public void removed(Path<T> path) {
        chain.removed(path);
    }

    @Override
    public void restructured(Path<T> path) {
        chain.restructured(path);
    }

}
