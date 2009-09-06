package org.workcraft.plugins.sdfs;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Static Data Flow Structure")
@VisualClass ("org.workcraft.plugins.sdfs.VisualSDFS")
public class SDFS extends AbstractMathModel {

	public SDFS() {
		super();

		new DefaultHangingConnectionRemover(this).attach(getRoot());
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	final public Register createRegister() {
		Register newRegister = new Register();
		add(newRegister);
		return newRegister;
	}

	final public Logic createLogic() {
		Logic newLogic = new Logic();
		add(newLogic);
		return newLogic;
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first == second)
			throw new InvalidConnectionException ("Self-loops are not allowed");
	}
}
