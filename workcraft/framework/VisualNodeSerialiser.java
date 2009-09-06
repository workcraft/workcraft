package org.workcraft.framework;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.framework.serialisation.ReferenceProducer;

public interface VisualNodeSerialiser {
	public void serialise(Node node, Element element, ReferenceProducer referenceResolver);
}
