package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dom.MathNode;

public interface DependentNode extends HierarchyNode {
	public Collection<MathNode> getMathReferences();
}
