package org.workcraft.dom;

import org.w3c.dom.Element;

public class MathNode {
	private XMLSerialisation serialisation = new XMLSerialisation();

	final public void addXMLSerialiser(XMLSerialiser serialisable) {
		serialisation.addSerialiser(serialisable);
	}

	final public void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}
}
