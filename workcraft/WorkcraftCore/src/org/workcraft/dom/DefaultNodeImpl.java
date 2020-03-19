package org.workcraft.dom;

import java.util.Collection;
import java.util.Collections;

public class DefaultNodeImpl implements Node {
    private final Node parent;

    public DefaultNodeImpl(Node parent) {
        this.parent = parent;
    }

    @Override
    public Collection<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node parent) {
        throw new RuntimeException("Node does not support reparenting.");
    }
}
