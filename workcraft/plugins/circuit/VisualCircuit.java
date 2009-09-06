package org.workcraft.plugins.circuit;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.NodeCreationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;


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
