package org.workcraft.dom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.observation.ObservableHierarchy;

public class ArbitraryInsertionGroupImpl extends AbstractGroup implements ObservableHierarchy, Container {

    private final LinkedList<Node> children = new LinkedList<>();

    public ArbitraryInsertionGroupImpl(Container groupRef) {
        super(groupRef);
    }

    @Override
    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void add(int index, Node node) {
        preAdd(node, true);
        children.add(index, node);
        postAdd(node, true);
    }

    @Override
    protected void addInternal(Node node) {
        children.add(node);
    }

    @Override
    protected void removeInternal(Node node) {
        children.remove(node);
    }

}
