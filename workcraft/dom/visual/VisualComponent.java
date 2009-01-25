package org.workcraft.dom.visual;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualTransformableNode {
	protected Component refComponent = null;
	protected VisualGroup parent;
	protected Set<VisualConnection> connections = new HashSet<VisualConnection>();

	public VisualComponent(Component refComponent) {
		super();
		this.refComponent = refComponent;
	}

	public VisualComponent(Component refComponent, Element xmlElement) {
		super(xmlElement);
		this.refComponent = refComponent;
	}

	public Set<VisualConnection> getConnections() {
		return new HashSet<VisualConnection>(connections);

	}

	public void addConnection(VisualConnection connection) {
		connections.add(connection);
	}

	public void removeConnection(VisualConnection connection) {
		connections.remove(connection);
	}

	public Component getReferencedComponent() {
		return refComponent;
	}
}
