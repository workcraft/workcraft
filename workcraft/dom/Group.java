package org.workcraft.dom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Group implements HierarchyNode, Container {
	private Set<HierarchyNode> children = new HashSet<HierarchyNode> ();
	private HierarchyNode parent = null;

	public Collection<HierarchyNode> getChildren() {
		return new HashSet<HierarchyNode>(children);
	}

	public HierarchyNode getParent() {
		return parent;
	}

	public void setParent(HierarchyNode parent) {
		this.parent = parent;
	}

	public void add(HierarchyNode node) {
		children.add(node);
	}

	public void remove (HierarchyNode node) {
		children.remove(node);
	}
}
