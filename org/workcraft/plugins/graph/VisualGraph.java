package org.workcraft.plugins.graph;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.VisualModelInstantiationException;

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