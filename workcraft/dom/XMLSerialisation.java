package org.workcraft.dom;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.util.XmlUtil;

public class XMLSerialisation {
	private LinkedList<XMLSerialiser> serialisers = new LinkedList<XMLSerialiser>();
	private LinkedList<XMLDeserialiser> deserialisers = new LinkedList<XMLDeserialiser>();

	final public void addSerialiser (XMLSerialiser serialiser) {
		serialisers.add(serialiser);
	}

	final public void addDeserialiser (XMLDeserialiser serialiser) {
		deserialisers.add(serialiser);
	}

	final public void serialise(Element componentElement) {
		for (XMLSerialiser s: serialisers) {
			Element e = XmlUtil.createChildElement(s.getTagName(), componentElement);
			s.serialise(e);
		}
	}

	final public void deserialise(Element componentElement) throws LoadFromXMLException {
		for (XMLDeserialiser s: deserialisers) {
			Element e = XmlUtil.getChildElement(s.getTagName(), componentElement);
			s.deserialise(e);
		}
	}
}
