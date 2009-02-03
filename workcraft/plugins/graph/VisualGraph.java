package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public class VisualGraph extends VisualModel {

	public VisualGraph(Graph model)
	throws VisualModelInstantiationException {
		super(model);
	}

	public VisualGraph(Graph model, Element visualElement)
	throws VisualModelInstantiationException {
		super(model, visualElement);
	}
}