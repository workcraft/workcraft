package org.workcraft.observation;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;

public class NodesDeletedEvent implements HierarchyEvent {

    private final Node parentNode;
    private final Collection<Node> affectedNodes;

    public NodesDeletedEvent(Node parentNode, Collection<Node> affectedNodes) {
        this.parentNode = parentNode;
        this.affectedNodes = affectedNodes;
    }

    public NodesDeletedEvent(Node parentNode, Node affectedNode) {
        this.parentNode = parentNode;
        this.affectedNodes = new ArrayList<>();
        affectedNodes.add(affectedNode);
    }

    @Override
    public Collection<Node> getAffectedNodes() {
        return affectedNodes;
    }

    @Override
    public Object getSender() {
        return parentNode;
    }

}
