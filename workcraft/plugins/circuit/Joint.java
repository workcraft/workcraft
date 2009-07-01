package org.workcraft.plugins.circuit;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;


@DisplayName("Joint")
@VisualClass("org.workcraft.plugins.circuit.VisualJoint")

public class Joint extends Component {
	public Joint(Element componentElement) {
		super(componentElement);

//		Element e = XmlUtil.getChildElement(Place.class.getSimpleName(), componentElement);
//		tokens = XmlUtil.readIntAttr(e, "tokens", 0);

		addXMLSerialisable();
	}

	public Joint() {
		super();

		addXMLSerialisable();
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){

			public String getTagName() {
				return Joint.class.getSimpleName();
			}

			public void serialise(Element element) {
				//XmlUtil.writeIntAttr(element, "tokens", tokens);
			}
		});
	}

}
