package org.workcraft.dom.math;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.GroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;

public class MathGroup extends MathNode implements ObservableHierarchy, Container {
	GroupImpl groupImpl = new GroupImpl(this);

	public void add(Node node) {
		groupImpl.add(node);
	}

	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	public Node getParent() {
		return groupImpl.getParent();
	}

	public void remove(Node node) {
		groupImpl.remove(node);
	}

	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}
}