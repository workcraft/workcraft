package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;

public interface AbstractXMLDeserialiser extends XMLDeserialiser {
	public void deserialise (Element element, Object instance) throws DeserialisationException;
}
