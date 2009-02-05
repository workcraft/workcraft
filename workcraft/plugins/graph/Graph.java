package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Directed Graph")
@VisualClass("org.workcraft.plugins.graph.VisualGraph")
public class Graph extends MathModel {

	public Graph() {
	}

	public Graph(Element xmlElement) throws ModelLoadFailedException {
		super(xmlElement);
	}

	public void validate() throws ModelValidationException {
	}

	public void validateConnection(Connection connection)	throws InvalidConnectionException {
	}

	public Vertex createVertex(String label) {
		Vertex v = new Vertex();
		v.setLabel(label);
		try {
			addComponent(v);
		} catch (InvalidComponentException e) {

		}
		return v;
	}

	@Override
	protected void onComponentAdded(Component component) {
	}

	@Override
	protected void onComponentRemoved(Component component) {
	}

	@Override
	protected void onConnectionAdded(Connection connection) {
	}

	@Override
	protected void onConnectionRemoved(Connection connection) {
	}
}