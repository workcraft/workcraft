package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class IntDeserialiser implements BasicXMLDeserialiser {
	public Object deserialise(Element element) throws DeserialisationException {
		return Integer.parseInt(element.getAttribute("value"));
	}

	public String getClassName() {
		return int.class.getName();
	}

}
