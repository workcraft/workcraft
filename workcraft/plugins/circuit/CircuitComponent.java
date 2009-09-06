package org.workcraft.plugins.circuit;

import java.util.HashSet;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.Contact.IOType;

@DisplayName("Component")
@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

public class CircuitComponent extends MathNode {

	private HashSet<Contact> inputs = new HashSet<Contact>();
	private HashSet<Contact> outputs = new HashSet<Contact>();

	public CircuitComponent() {
	}

	public Contact addInput(String label) {
		Contact c = new Contact(label, IOType.input);
		inputs.add(c);
		return c;
	}

	public Contact addOutput(String label) {
		Contact c = new Contact(label, IOType.output);
		outputs.add(c);
		return c;
	}
}
