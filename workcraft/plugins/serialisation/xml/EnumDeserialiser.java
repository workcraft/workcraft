package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.xml.BasicXMLDeserialiser;

public class EnumDeserialiser implements BasicXMLDeserialiser {
	@SuppressWarnings("unchecked")
	public Object deserialise(Element element) throws DeserialisationException {
		try {
			Class<? extends Enum> cls = Class.forName(element.getAttribute("enum-class")).asSubclass(Enum.class);
			return Enum.valueOf(cls, element.getAttribute("value"));
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);
		}
	}

	public String getClassName() {
		return Enum.class.getName();
	}
}
