package org.workcraft.dom;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Group implements HierarchyNode, Container {
	private Set<HierarchyNode> children = new LinkedHashSet<HierarchyNode> ();
	private HierarchyNode parent = null;

	public Collection<HierarchyNode> getChildren() {
		return Collections.unmodifiableCollection(children);
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
