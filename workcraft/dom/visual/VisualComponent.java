package org.workcraft.dom.visual;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public abstract class VisualComponent extends VisualTransformableNode {
	private Component refComponent = null;
	private HashSet<VisualConnection> connections = new HashSet<VisualConnection>();
	private HashSet<VisualComponent> preset = new HashSet<VisualComponent>();
	private HashSet<VisualComponent> postset = new HashSet<VisualComponent>();
	private String label = "";

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration("Label", "getLabel", "setLabel", String.class));
	}

	public VisualComponent(Component refComponent) {
		super();
		this.refComponent = refComponent;
		addPropertyDeclarations();
	}

	public VisualComponent(Component refComponent, Element xmlElement) {
		super(xmlElement);
		this.refComponent = refComponent;
		addPropertyDeclarations();
	}

	public Set<VisualConnection> getConnections() {
		return new HashSet<VisualConnection>(connections);

	}

	final public void addConnection(VisualConnection connection) {
		connections.add(connection);

		if (connection.getFirst() == this)
			postset.add(connection.getSecond());
		else
			preset.add(connection.getFirst());

	}

	final public void removeConnection(VisualConnection connection) {
		connections.remove(connection);

		if (connection.getFirst() == this)
			postset.remove(connection.getSecond());
		else
			preset.remove(connection.getFirst());
	}

	final public Component getReferencedComponent() {
		return refComponent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	final public Set<VisualComponent> getPreset() {
		return new HashSet<VisualComponent>(preset);
	}

	final public Set<VisualComponent> getPostset() {
		return new HashSet<VisualComponent>(postset);
	}

}
