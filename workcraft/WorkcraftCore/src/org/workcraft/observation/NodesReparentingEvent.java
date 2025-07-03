package org.workcraft.observation;

import org.workcraft.dom.Node;

import java.util.ArrayList;
import java.util.Collection;

public class NodesReparentingEvent implements HierarchyEvent {

    private final Node oldParentNode;
    private final Node newParentNode;
    private final Collection<Node> affectedNodes;

    @SuppressWarnings("unchecked")
    public NodesReparentingEvent(Node oldParentNode, Node newParentNode, Collection<? extends Node> affectedNodes) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = (Collection<Node>) affectedNodes;
    }

    public NodesReparentingEvent(Node oldParentNode, Node newParentNode, Node affectedNode) {
        this.oldParentNode = oldParentNode;
        this.newParentNode = newParentNode;
        this.affectedNodes = new ArrayList<>();
        affectedNodes.add(affectedNode);
    }

    @Override
    public Collection<Node> getAffectedNodes() {
        return affectedNodes;
    }

    public Node getOldParent() {
        return oldParentNode;
    }

    public Node getNewParent() {
        return newParentNode;
    }

    @Override
    public Object getSender() {
        return oldParentNode;
    }

}
