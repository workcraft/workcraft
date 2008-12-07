package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.VisualClassConstructionException;

public class VisualGraph extends VisualModel {

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