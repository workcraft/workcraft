package org.workcraft.plugins.circuit;

import org.w3c.dom.Element;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;


@DisplayName("Joint")
@VisualClass("org.workcraft.plugins.circuit.VisualJoint")

public class Joint extends CircuitComponent {

	public Joint() {
		addXMLSerialisable();
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){

			public String getTagName() {
				return Joint.class.getSimpleName();
			}

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws DeserialisationException {
				// TODO Auto-generated method stub

			}

			public void serialise(Element element,
					ExternalReferenceResolver refResolver) {
				// TODO Auto-generated method stub
			}
		});
	}

}
