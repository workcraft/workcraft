package org.workcraft.plugins.circuit;

import java.util.HashSet;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.plugins.circuit.Contact.IOType;

@DisplayName("Component")
@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

public class CircuitComponent extends Component {

	private HashSet<Contact> inputs = new HashSet<Contact>();
	private HashSet<Contact> outputs = new HashSet<Contact>();

	public CircuitComponent() {
		addXMLSerialisable();
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

//	public int getTokens() {
//		return tokens;
//	}
//
//	public void setTokens(int tokens) {
//		this.tokens = tokens;
//	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return CircuitComponent.class.getSimpleName();
			}

			public void serialise(Element element, ReferenceProducer refResolver) {
			}

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws DeserialisationException {
			}
		});
	}
}
