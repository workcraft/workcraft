package org.workcraft.plugins.graph;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Directed Graph")
@VisualClass("org.workcraft.plugins.graph.VisualGraph")
public class Graph extends AbstractMathModel {

	public Graph() {
	}

	public void validate() throws ModelValidationException {
	}

	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	public Vertex createVertex(String label) {
		Vertex v = new Vertex();
		v.setLabel(label);
		try {
			add(v);
		} catch (InvalidComponentException e) {

		}
		return v;
	}

	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}
}