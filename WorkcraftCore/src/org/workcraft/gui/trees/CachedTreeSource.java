package org.workcraft.gui.trees;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.gui.workspace.Path;


public class CachedTreeSource<Node> implements TreeSource<Node> {
    private TreeSourceCache<Node> cache = new TreeSourceCache<Node>();
    private final TreeSource<Node> source;
    private final LinkedList<TreeListener<Node>> listeners = new LinkedList<TreeListener<Node>>();

    public CachedTreeSource(final TreeSource<Node> source) {
        this.source = source;

        cache.update(source.getRoot(), source);

        source.addListener(new TreeListener<Node>() {

            @Override
            public void added(Path<Node> path) {
                cache.update(path.getNode(), source);

                for (TreeListener<Node> listener : listeners)
                    listener.added(path);
            }

            @Override
            public void changed(Path<Node> path) {
                cache.update(path.getNode(), source);

                for (TreeListener<Node> listener : listeners)
                    listener.changed(path);
            }

            @Override
            public void removed(Path<Node> path) {
                cache.clear(path.getNode(), source);

                for (TreeListener<Node> listener : listeners)
                    listener.removed(path);

            }

            @Override
            public void restructured(Path<Node> path) {
                cache.update(path.getNode(), source);

                for (TreeListener<Node> listener : listeners)
                    listener.restructured(path);
            }
        });
    }

    @Override
    public void addListener(TreeListener<Node> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(TreeListener<Node> listener) {
        listeners.remove(listener);
    }


    @Override
    public List<Node> getChildren(Node node) {
        return cache.getChildren(node);
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
    public Path<Node> getPath(Node node) {
        return source.getPath(node);
    }
}
