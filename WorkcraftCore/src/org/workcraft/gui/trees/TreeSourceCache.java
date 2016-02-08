package org.workcraft.gui.trees;

import java.util.HashMap;
import java.util.List;

public class TreeSourceCache<Node> {
    private HashMap<Node, List<Node>> cache = new HashMap<Node, List<Node>>();

    public void clear(Node node, TreeSource<Node> source) {
        if (source.isLeaf(node))
            return;

        final List<Node> children = getChildren(node);

        for (Node n : children)
            clear(n, source);

        cache.remove(node);
    }

    public void update(Node node, TreeSource<Node> source) {
        if (source.isLeaf(node))
            return;

        if (cache.containsKey(node))
            clear(node, source);

        final List<Node> children = source.getChildren(node);
        cache.put(node, children);

        for (Node n : children)
            update(n, source);
    }

    public List<Node> getChildren(Node node) {
        return cache.get(node);
    }

    public boolean isCached(Node node) {
        return cache.containsKey(node);
    }
}