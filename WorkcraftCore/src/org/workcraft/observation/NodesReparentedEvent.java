package org.workcraft.observation;

import org.workcraft.dom.Node;

import java.util.ArrayList;
import java.util.Collection;

public class NodesReparentedEvent implements HierarchyEvent {
    private final Node oldParentNode;
    private final Node newParentNode;
    private final Collection<Node> affectedNodes;

    public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Collection<? extends Node> affectedNodes) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = (Collection<Node>) affectedNodes;
    }

    public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Node affectedNode) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = new ArrayList<>();
        affectedNodes.add(affectedNode);
    }

    public Collection<Node> getAffectedNodes() {
        return affectedNodes;
    }

    public Node getOldParent() {
        return oldParentNode;
    }

    public Node getNewParent() {
        return newParentNode;
    }

    public Object getSender() {
        return newParentNode;
    }
}
