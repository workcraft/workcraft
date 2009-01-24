package org.workcraft.dom.visual;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualTransformableNode {
	protected Component refComponent = null;
	protected VisualComponentGroup parent;
	protected Set<VisualConnection> connections = new HashSet<VisualConnection>();

	public VisualComponent(Component refComponent, VisualComponentGroup parent) {
		super(parent);
		this.refComponent = refComponent;
		this.parent = parent;
	}

	public VisualComponent(Component refComponent, Element xmlElement, VisualComponentGroup parent) {
		super(xmlElement, parent);
		this.refComponent = refComponent;
		this.parent = parent;
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
