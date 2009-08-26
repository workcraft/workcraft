package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;

public interface BasicXMLDeserialiser extends XMLDeserialiser {
	public Object deserialise (Element element) throws DeserialisationException;
}