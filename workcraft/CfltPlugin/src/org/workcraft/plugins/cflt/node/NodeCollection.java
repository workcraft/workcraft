package org.workcraft.plugins.cflt.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeCollection {
    private static NodeCollection instance;

    private final List<Node> nodes;
    private boolean isIterationPresent;
    private String singleTransition;

    private NodeCollection() {
        nodes = new ArrayList<>();
        isIterationPresent = false;
    }

    public static NodeCollection getInstance() {
        if (instance == null) {
            instance = new NodeCollection();
        }
        return instance;
    }

    public void setContainsIteration(boolean containsIteration) {
        this.isIterationPresent = containsIteration;
    }

    public boolean containsIteration() {
        return isIterationPresent;
    }

    public void setSingleTransition(String singleTransition) {
        this.singleTransition = singleTransition;
    }

    public String getSingleTransition() {
        return singleTransition;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public boolean removeNode(Node node) {
        return nodes.remove(node);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void clear() {
        nodes.clear();
        isIterationPresent = false;
        singleTransition = null;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public int size() {
        return nodes.size();
    }

    public NodeIterator getNodeIterator() {
        return new NodeIterator(nodes);
    }

    @Override
    public String toString() {
        return "NodeCollection{" +
                "nodes=" + nodes +
                '}';
    }
}
