package org.workcraft.dom;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class XMLSerialiser {
	private LinkedList<XMLSerialisable> serialisables = new LinkedList<XMLSerialisable>();

	final public void addXMLSerialisable (XMLSerialisable serialisable) {
		serialisables.add(serialisable);
	}

	final public void serialiseToXML(Element componentElement) {
		for (XMLSerialisable s: serialisables) {
			Element e = XmlUtil.createChildElement(s.getTagName(), componentElement);
			s.serialise(e);
		}
	}
}
