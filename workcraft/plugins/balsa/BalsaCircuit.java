package org.workcraft.plugins.balsa;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@VisualClass ("org.workcraft.plugins.balsa.VisualBalsaCircuit")
@DisplayName ("Balsa circuit")
public final class BalsaCircuit extends MathModel{

	public BalsaCircuit() {
		super();
		// TODO Auto-generated constructor stub
		addComponentSupport(BreezeComponent.class);
	}

	@Override
	public void validate() throws ModelValidationException {
		// TODO Auto-generated method stub
	}

	@Override
	public void validateConnection(Connection connection)
			throws InvalidConnectionException {
		// TODO Auto-generated method stub
	}
}
