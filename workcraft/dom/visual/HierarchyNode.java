package org.workcraft.dom.visual;

import java.util.Collection;

public interface HierarchyNode {
	HierarchyNode getParent();
	Collection<HierarchyNode> getChildren();
}
