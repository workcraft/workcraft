package org.workcraft.plugins.circuit;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;

@DisplayName ("Digital Circuit")
@VisualClass ("org.workcraft.plugins.circuit.VisualCircuit")

public class Circuit extends AbstractMathModel {

	public Circuit() {
		super(null);
	}

	public void validate() throws ModelValidationException {
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}
}
