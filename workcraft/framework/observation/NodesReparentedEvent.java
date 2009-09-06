package org.workcraft.framework.observation;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;

public class NodesReparentedEvent implements HierarchyEvent {
	private Node oldParentNode;
	private Node newParentNode;
	private Collection<Node> affectedNodes;

	public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Collection<Node> affectedNodes) {
		this.oldParentNode = oldParentNode;
		this.newParentNode = newParentNode;
		this.affectedNodes = affectedNodes;
	}

	public NodesReparentedEvent(Node oldParentNode, Node newParentNode, Node affectedNode) {
		this.oldParentNode = oldParentNode;
		this.newParentNode = newParentNode;
		this.affectedNodes = new ArrayList<Node>();
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
