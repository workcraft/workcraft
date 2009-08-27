package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.xml.BasicXMLSerialiser;

public class EnumSerialiser implements BasicXMLSerialiser {
	public void serialise(Element element, Object object)
			throws SerialisationException {
		element.setAttribute("enum-class", object.getClass().getName());
		element.setAttribute("value", object.toString());

	}

	public String getClassName() {
		return Enum.class.getName();
	}
}
