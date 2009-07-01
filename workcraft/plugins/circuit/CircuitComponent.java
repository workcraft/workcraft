package org.workcraft.plugins.circuit;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;

@DisplayName("Component")
@VisualClass("org.workcraft.plugins.circuit.VisualCircuitComponent")

public class CircuitComponent extends Component {
	public CircuitComponent(Element xmlElement) {
		super(xmlElement);
		addXMLSerialisable();
	}

	public CircuitComponent() {
		super();

		addXMLSerialisable();
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
			public void serialise(Element element) {

//				XmlUtil.writeIntAttr(element, "tokens", tokens);
			}
		});
	}

}
