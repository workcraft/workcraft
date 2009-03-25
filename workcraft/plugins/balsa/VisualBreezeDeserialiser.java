package org.workcraft.plugins.balsa;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.VisualNodeDeserialiser;

public class VisualBreezeDeserialiser implements VisualNodeDeserialiser {

	public VisualNode deserialise(Element element, VisualModel model) {
		try {
			return new VisualBreezeComponent(element, model);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
