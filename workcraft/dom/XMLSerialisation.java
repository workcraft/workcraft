package org.workcraft.dom;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;

public class XMLSerialisation {
	private LinkedList<XMLSerialiser> serialisers = new LinkedList<XMLSerialiser>();

	final public void addSerialiser (XMLSerialiser serialiser) {
		serialisers.add(serialiser);
	}

	final public void serialise(Element componentElement, ExternalReferenceResolver refResolver) {
		for (XMLSerialiser s: serialisers) {
			Element e = XmlUtil.createChildElement(s.getTagName(), componentElement);
			s.serialise(e, refResolver);
		}
	}

	final public void deserialise(Element componentElement, ReferenceResolver refResolver) throws ImportException {
		for (XMLSerialiser s: serialisers) {
			Element e = XmlUtil.getChildElement(s.getTagName(), componentElement);
			s.deserialise(e, refResolver);
		}
	}
}
