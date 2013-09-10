package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualControlConnection extends VisualConnection {

	public VisualControlConnection () {
	}

	public VisualControlConnection(ControlConnection refConnection) {
	}

	public VisualControlConnection(ControlConnection refConnection, VisualComponent first, VisualComponent second) {
		super(refConnection, first, second);
	}

	@Override
	protected void initialise() {
		super.initialise();
		addPropertyDeclaration(new PropertyDeclaration (this, "Inverting", "isInverting", "setInverting", boolean.class));
	}

	public 	ControlConnection getReferencedControlConnection() {
		return (ControlConnection)getReferencedConnection();
	}

	public void setInverting(boolean value) {
		ControlConnection ref = getReferencedControlConnection();
		// check if ref is not null to trick the order of node creation in deserialiser
		if (ref != null) {
			ref.setInverting(value);
		}
		setBubble(value);
	}

	public boolean isInverting() {
		return getReferencedControlConnection().isInverting();
	}

}
