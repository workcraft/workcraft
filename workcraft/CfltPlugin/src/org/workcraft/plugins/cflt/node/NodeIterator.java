package org.workcraft.plugins.cflt.node;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class NodeIterator implements Iterator<Node> {
    private final List<Node> nodes;
    private int position;

    public NodeIterator(List<Node> nodes) {
        this.nodes = nodes;
        this.position = 0;
    }

    @Override
    public boolean hasNext() {
        return position < nodes.size();
    }

    @Override
    public Node next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the collection.");
        }
        return nodes.get(position++);
    }

    @Override
    public void remove() {
        if (position <= 0) {
            throw new IllegalStateException("Remove can only be called after next().");
        }
        nodes.remove(--position);
    }

    public boolean isLastNode() {
        return position == nodes.size();
    }

    public int getCurrentPosition() {
        return position;
    }
}
