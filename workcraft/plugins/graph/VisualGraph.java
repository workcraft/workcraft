package org.workcraft.plugins.graph;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public class VisualGraph extends AbstractVisualModel {

	public VisualGraph(Graph model) throws VisualModelInstantiationException {
		super(model);
	}

	@Override
	public void validate() throws ModelValidationException {
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}

}