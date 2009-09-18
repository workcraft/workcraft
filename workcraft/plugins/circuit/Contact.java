package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;

@DisplayName("Contact")
@VisualClass("org.workcraft.plugins.circuit.VisualContact")

public class Contact extends MathNode {

	public enum IOType {input, output};
	private IOType iotype;
	//private boolean invertSignal = false;

	public Contact() {
	}

	public Contact(String label, IOType iot) {
		super();

		setLabel(label);
		setIOType(iot);
	}

	public void setIOType(IOType t) {
		this.iotype = t;
	}

	public IOType getIOType() {
		return iotype;
	}
}
