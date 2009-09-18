package org.workcraft.plugins.circuit;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;


public class VisualCircuit extends AbstractVisualModel {

	public VisualCircuit(Circuit model)
	throws VisualModelInstantiationException {
		super(model);
		try {
			createDefaultFlatStructure();
		} catch (NodeCreationException e) {
			throw new VisualModelInstantiationException(e);
		}
	}

	@Override
	public void validate() throws ModelValidationException {
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}
}
