package org.workcraft.plugins.circuit;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;

@DisplayName("Contact")
@VisualClass("org.workcraft.plugins.circuit.VisualContact")

public class Contact extends Component {

	public enum IOType {input, output};
	private IOType iotype;
	//private boolean invertSignal = false;

	public Contact(Element xmlElement) {
		super(xmlElement);
		addXMLSerialisable();
	}

	public Contact() {
		super();

		addXMLSerialisable();
	}

	public Contact(String label, IOType iot) {
		super();

		setLabel(label);
		setIOType(iot);
		addXMLSerialisable();
	}

	public void setIOType(IOType t) {
		this.iotype = t;
	}

	public IOType getIOType() {
		return iotype;
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return CircuitComponent.class.getSimpleName();
			}
			public void serialise(Element element) {

//				XmlUtil.writeIntAttr(element, "tokens", tokens);
			}
		});
	}

}
