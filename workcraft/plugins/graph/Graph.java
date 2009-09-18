package org.workcraft.plugins.graph;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidComponentException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;

@DisplayName ("Directed Graph")
@VisualClass("org.workcraft.plugins.graph.VisualGraph")
public class Graph extends AbstractMathModel {

	public Graph() {
		super(null);
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