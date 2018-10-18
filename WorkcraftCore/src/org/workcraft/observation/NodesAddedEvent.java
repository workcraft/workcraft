package org.workcraft.observation;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;

public class NodesAddedEvent implements HierarchyEvent {
    private final Node parentNode;
    private final Collection<Node> affectedNodes;

    public NodesAddedEvent(Node parentNode, Collection<? extends Node> affectedNodes) {
        this.parentNode = parentNode;
        this.affectedNodes = (Collection<Node>) affectedNodes;
    }

    public NodesAddedEvent(Node parentNode, Node affectedNode) {
        this.parentNode = parentNode;
        this.affectedNodes = new ArrayList<>();
        affectedNodes.add(affectedNode);
    }

    public Collection<Node> getAffectedNodes() {
        return affectedNodes;
    }

    public Object getSender() {
        return parentNode;
    }
}
