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
		addPropertyDeclaration(new PropertyDeclaration<VisualControlConnection, Boolean>(
				this, "Inverting", Boolean.class) {
			public void setter(VisualControlConnection object, Boolean value) {
				ControlConnection ref = getReferencedControlConnection();
				// check if ref is not null to trick the order of node creation in deserialiser
				if (ref != null) {
					ref.setInverting(value);
				}
				setBubble(value);
			}
			public Boolean getter(VisualControlConnection object) {
				return object.getReferencedControlConnection().isInverting();
			}
		});
	}

	public 	ControlConnection getReferencedControlConnection() {
		return (ControlConnection)getReferencedConnection();
	}

}
