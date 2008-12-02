package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.framework.exceptions.VisualModelConstructionException;

public class VisualGraph extends VisualAbstractGraphModel {

	public VisualGraph(AbstractGraphModel model)
			throws VisualModelConstructionException {
		super(model);
		// TODO Auto-generated constructor stub
	}

	public VisualGraph(AbstractGraphModel model, Element visualElement)
			throws VisualModelConstructionException {
		super(model, visualElement);
		// TODO Auto-generated constructor stub
	}

}
