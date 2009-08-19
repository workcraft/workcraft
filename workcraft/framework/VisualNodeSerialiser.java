package org.workcraft.framework;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.HierarchyNode;

public interface VisualNodeSerialiser {
	public void serialise(HierarchyNode node, Element element);
}
