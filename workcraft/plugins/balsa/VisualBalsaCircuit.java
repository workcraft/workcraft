package org.workcraft.plugins.balsa;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.framework.CustomTools;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

@CustomTools(VisualBalsaTools.class)
public final class VisualBalsaCircuit extends AbstractVisualModel {
	public VisualBalsaCircuit(BalsaCircuit model) throws VisualModelInstantiationException {
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
