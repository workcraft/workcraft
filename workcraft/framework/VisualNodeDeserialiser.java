package org.workcraft.framework;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;

public interface VisualNodeDeserialiser {
	public VisualNode deserialise(Element e, VisualModel model);
}
