package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

import java.util.List;

public class TreeSourceAdapter<T> implements TreeSource<T> {

    private final TreeSource<T> source;
    private final TreeListenerArray<T> ls = new TreeListenerArray<>();

    public TreeSourceAdapter(TreeSource<T> source) {
        this.source = source;
        source.addListener(getListener(ls));
    }

    protected TreeListener<T> getListener(final TreeListener<T> chain) {
        return chain;
    }

    public TreeListener<T> getListener() {
        return getListener(ls);
    }

    @Override
    public void addListener(TreeListener<T> listener) {
        ls.add(listener);
    }

    @Override
    public List<T> getChildren(T node) {
        return source.getChildren(node);
    }

    @Override
    public T getRoot() {
        return source.getRoot();
    }

    @Override
    public boolean isLeaf(T node) {
        return source.isLeaf(node);
    }

    @Override
    public void removeListener(TreeListener<T> listener) {
        ls.remove(listener);
    }

    @Override
    public Path<T> getPath(T node) {
        return source.getPath(node);
    }

}
