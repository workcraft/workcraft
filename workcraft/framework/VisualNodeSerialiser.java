package org.workcraft.framework;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualNode;

public interface VisualNodeSerialiser {
	public void serialise(VisualNode node, Element element);
}
