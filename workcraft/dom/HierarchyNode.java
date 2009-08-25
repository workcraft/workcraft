package org.workcraft.dom;
import java.util.Collection;

public interface HierarchyNode {
	public HierarchyNode getParent();
	public void setParent(HierarchyNode parent);

	public Collection<HierarchyNode> getChildren();
}
