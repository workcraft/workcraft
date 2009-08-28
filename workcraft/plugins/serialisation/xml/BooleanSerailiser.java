package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.xml.BasicXMLSerialiser;

public class BooleanSerailiser implements BasicXMLSerialiser {
	public void serialise(Element element, Object object)
			throws SerialisationException {
		element.setAttribute("value", Boolean.toString((Boolean)object));
	}

	public String getClassName() {
		return boolean.class.getName();
	}
}
