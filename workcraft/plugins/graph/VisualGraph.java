package org.workcraft.plugins.graph;

import org.workcraft.dom.visual.VisualAbstractGraphModel;
import org.workcraft.framework.exceptions.VisualModelConstructionException;

public class VisualGraph extends VisualAbstractGraphModel {

	public VisualGraph(Graph graph) throws VisualModelConstructionException {
		super(graph);
	}

}
