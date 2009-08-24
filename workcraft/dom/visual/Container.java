package org.workcraft.dom.visual;

public interface Container extends HierarchyNode {
	void add(FreeNode node);
	void remove(FreeNode node);
}
