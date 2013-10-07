package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.Getter;
import org.workcraft.gui.propertyeditor.SafePropertyDeclaration;
import org.workcraft.gui.propertyeditor.Setter;

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
		addPropertyDeclaration(new SafePropertyDeclaration<VisualControlConnection, Boolean>(
				this, "Inverting",
				new Getter<VisualControlConnection, Boolean>() {
					@Override
					public Boolean eval(VisualControlConnection object) {
						return object.getReferencedControlConnection().isInverting();
					}
				},
				new Setter<VisualControlConnection, Boolean>() {
					@Override
					public void eval(VisualControlConnection object, Boolean value) {
						ControlConnection ref = getReferencedControlConnection();
						// check if ref is not null to trick the order of node creation in deserialiser
						if (ref != null) {
							ref.setInverting(value);
						}
						setBubble(value);
					}
				},
				Boolean.class));
	}

	public 	ControlConnection getReferencedControlConnection() {
		return (ControlConnection)getReferencedConnection();
	}

}
