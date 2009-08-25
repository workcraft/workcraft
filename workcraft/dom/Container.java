package org.workcraft.dom;


public interface Container extends HierarchyNode {
	void add(HierarchyNode node);
	void remove(HierarchyNode node);
}
