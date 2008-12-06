package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.framework.exceptions.VisualClassConstructionException;

public class VisualGraph extends VisualAbstractGraphModel {

	public VisualGraph(Graph model)
			throws VisualClassConstructionException {
		super(model);
		// TODO Auto-generated constructor stub
	}

	public VisualGraph(Graph model, Element visualElement)
			throws VisualClassConstructionException {
		super(model, visualElement);
		// TODO Auto-generated constructor stub
	}
}