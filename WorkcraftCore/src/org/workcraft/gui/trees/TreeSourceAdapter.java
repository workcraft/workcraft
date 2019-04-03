package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

import java.util.List;

public class TreeSourceAdapter<Node> implements TreeSource<Node> {

    private final TreeSource<Node> source;
    private final TreeListenerArray<Node> ls = new TreeListenerArray<>();

    public TreeSourceAdapter(TreeSource<Node> source) {
        this.source = source;
        source.addListener(getListener(ls));
    }

    protected TreeListener<Node> getListener(final TreeListener<Node> chain) {
        return chain;
    }

    public TreeListener<Node> getListener() {
        return getListener(ls);
    }

    @Override
    public void addListener(TreeListener<Node> listener) {
        ls.add(listener);
    }

    @Override
    public List<Node> getChildren(Node node) {
        return source.getChildren(node);
    }

    @Override
    public Node getRoot() {
        return source.getRoot();
    }

    @Override
    public boolean isLeaf(Node node) {
        return source.isLeaf(node);
    }

    @Override
    public void removeListener(TreeListener<Node> listener) {
        ls.remove(listener);
    }

    @Override
    public Path<Node> getPath(Node node) {
        return source.getPath(node);
    }

}
