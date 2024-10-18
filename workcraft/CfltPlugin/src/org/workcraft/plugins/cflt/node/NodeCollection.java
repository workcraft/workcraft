package org.workcraft.plugins.cflt.node;

import java.util.*;

public class NodeCollection {

    private final List<Node> nodes;
    private final Map<String, NodeDetails> nameToNodeDetailsMap;

    private boolean isIterationPresent;
    private String singleTransition;

    public NodeCollection() {
        nodes = new ArrayList<>();
        isIterationPresent = false;
        nameToNodeDetailsMap = new HashMap<>();
        singleTransition = null;
    }

    public void addNodeDetails(NodeDetails nodeDetails) {
        nameToNodeDetailsMap.put(nodeDetails.getName(), nodeDetails);
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

    public NodeDetails getNodeDetails(String name) {
        return nameToNodeDetailsMap.get(name);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
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
