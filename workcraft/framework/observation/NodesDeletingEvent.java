package org.workcraft.framework.observation;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;

public class NodesDeletingEvent implements HierarchyEvent {
	private Node parentNode;
	private Collection<Node> affectedNodes;

	public NodesDeletingEvent(Node parentNode, Collection<Node> affectedNodes) {
		this.parentNode = parentNode;
		this.affectedNodes = affectedNodes;
	}

	public NodesDeletingEvent(Node parentNode, Node affectedNode) {
		this.parentNode = parentNode;
		this.affectedNodes = new ArrayList<Node>();
		affectedNodes.add(affectedNode);
	}
	public Collection<Node> getAffectedNodes() {
		return affectedNodes;
	}

	public Object getSender() {
		return parentNode;
	}
}
