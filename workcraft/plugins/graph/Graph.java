package org.workcraft.plugins.graph;

import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.Connection;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

public class Graph extends AbstractGraphModel {

	public Graph(Framework framework) {
		super(framework);
	}

	public Graph(Framework framework, Element xmlElement, String sourcePath) {
		super(framework);
	}


	@Override
	public Set<Class<?>> getSupportedComponents() {
		return null;
	}

	@Override
	public void validate() throws ModelValidationException {
	}

	@Override
	protected void validateConnection(Connection connection)
			throws InvalidConnectionException {
	}

}